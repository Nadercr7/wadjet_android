package com.wadjet.core.common.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared audio playback manager for WAV TTS output.
 * Single MediaPlayer for the whole app (H-06): starting any playback stops the
 * previous one, so overlapping TTS from different screens is impossible.
 * Requests transient audio focus for every playback and stops on focus loss.
 * Also owns the on-device TextToSpeech fallback (H-05).
 */
@Singleton
class AudioPlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private var mediaPlayer: MediaPlayer? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val playbackAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        if (change == AudioManager.AUDIOFOCUS_LOSS ||
            change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
        ) {
            stop()
        }
    }

    private var focusRequest: AudioFocusRequest? = null

    private fun requestFocus(): Boolean {
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(playbackAttributes)
            .setOnAudioFocusChangeListener(focusListener)
            .build()
        focusRequest = request
        return audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonFocus() {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        focusRequest = null
    }

    val isPlaying: Boolean
        get() = try { mediaPlayer?.isPlaying == true } catch (_: Exception) { false }

    fun playWavBytes(
        bytes: ByteArray,
        prefix: String = "tts_",
        speed: Float = 1.0f,
        onCompletion: () -> Unit = {},
        onError: () -> Unit = {},
    ) {
        stop()
        try {
            val tempFile = File.createTempFile(prefix, ".wav", context.cacheDir)
            tempFile.writeBytes(bytes)
            tempFile.deleteOnExit()
            requestFocus()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(playbackAttributes)
                setDataSource(tempFile.absolutePath)
                prepare()
                if (speed != 1.0f) {
                    playbackParams = playbackParams.setSpeed(speed)
                }
                setOnCompletionListener {
                    release()
                    mediaPlayer = null
                    tempFile.delete()
                    abandonFocus()
                    onCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    release()
                    mediaPlayer = null
                    tempFile.delete()
                    abandonFocus()
                    onError()
                    true
                }
                start()
            }
        } catch (e: Exception) {
            Timber.e(e, "MediaPlayer failed")
            abandonFocus()
            onError()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        stopLocalTts()
        abandonFocus()
    }

    // ── On-device TTS fallback (H-05) ──

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingUtterance: (() -> Unit)? = null

    /**
     * Speaks [text] with the device TTS engine, picking the voice from the
     * text's script (ar-EG → ar → en-US). Used when server TTS is unavailable
     * (network error or 204) — web parity: always degrade to local synthesis.
     *
     * @param onDone invoked when the utterance finishes, errors, or cannot be
     * spoken at all (no engine / no voice for the language).
     */
    fun speakLocal(text: String, speed: Float = 1.0f, onDone: () -> Unit = {}) {
        stop()
        val engine = tts
        if (engine != null && ttsReady) {
            speakWith(engine, text, speed, onDone)
            return
        }
        if (engine == null) {
            pendingUtterance = null
            var created: TextToSpeech? = null
            created = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    ttsReady = true
                    pendingUtterance?.invoke()
                } else {
                    Timber.w("Local TTS init failed (%d)", status)
                    tts = null
                    onDone()
                }
                pendingUtterance = null
            }
            tts = created
            pendingUtterance = { speakWith(created, text, speed, onDone) }
        } else {
            // init still in flight — replace the pending utterance
            pendingUtterance = { speakWith(engine, text, speed, onDone) }
        }
    }

    private fun speakWith(engine: TextToSpeech, text: String, speed: Float, onDone: () -> Unit) {
        if (!engine.trySetLanguageFor(text)) {
            Timber.w("No local TTS voice for text language; skipping")
            onDone()
            return
        }
        requestFocus()
        engine.setSpeechRate(speed)
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                abandonFocus()
                onDone()
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                abandonFocus()
                onDone()
            }
            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                abandonFocus()
                onDone()
            }
        })
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "wadjet_local_tts")
    }

    private fun stopLocalTts() {
        try { tts?.stop() } catch (_: Exception) {}
    }
}
