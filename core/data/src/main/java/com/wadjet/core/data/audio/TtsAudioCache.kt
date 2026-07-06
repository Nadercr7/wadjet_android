package com.wadjet.core.data.audio

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * H-07: keyed on-disk cache for server TTS audio. The backend caches generated
 * WAVs (Cache-Control: max-age=86400) but OkHttp never caches POST responses,
 * so replaying the same sign/paragraph re-billed a full TTS generation. Web
 * keeps an in-memory blob cache; this is the persistent Android equivalent.
 *
 * Lives in cacheDir/tts_cache so the system (and Settings "Clear cache") can
 * reclaim it; trimmed LRU-by-mtime to [MAX_BYTES].
 */
@Singleton
class TtsAudioCache @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dir = File(context.cacheDir, "tts_cache").apply { mkdirs() }

    fun get(text: String, lang: String, voiceContext: String): ByteArray? = runCatching {
        val file = keyFile(text, lang, voiceContext)
        if (!file.exists()) return null
        file.setLastModified(System.currentTimeMillis())
        file.readBytes()
    }.getOrNull()

    fun put(text: String, lang: String, voiceContext: String, bytes: ByteArray) {
        runCatching {
            keyFile(text, lang, voiceContext).writeBytes(bytes)
            trim()
        }.onFailure { Timber.w(it, "TTS cache write failed") }
    }

    private fun keyFile(text: String, lang: String, voiceContext: String): File {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$lang|$voiceContext|$text".toByteArray())
            .joinToString("") { "%02x".format(it) }
        return File(dir, "$digest.wav")
    }

    private fun trim() {
        val files = dir.listFiles()?.sortedBy { it.lastModified() } ?: return
        var total = files.sumOf { it.length() }
        for (file in files) {
            if (total <= MAX_BYTES) break
            total -= file.length()
            file.delete()
        }
    }

    private companion object {
        const val MAX_BYTES = 30L * 1024 * 1024
    }
}
