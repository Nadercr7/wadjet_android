package com.wadjet.core.common.audio

import android.media.MediaPlayer
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared audio playback manager for WAV TTS output.
 * Prevents MediaPlayer duplication across ViewModels.
 */
@Singleton
class AudioPlaybackManager @Inject constructor() {

    private var mediaPlayer: MediaPlayer? = null

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
            val tempFile = File.createTempFile(prefix, ".wav")
            tempFile.writeBytes(bytes)
            tempFile.deleteOnExit()

            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                if (speed != 1.0f) {
                    playbackParams = playbackParams.setSpeed(speed)
                }
                setOnCompletionListener {
                    release()
                    mediaPlayer = null
                    tempFile.delete()
                    onCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    release()
                    mediaPlayer = null
                    tempFile.delete()
                    onError()
                    true
                }
                start()
            }
        } catch (e: Exception) {
            Timber.e(e, "MediaPlayer failed")
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
    }
}
