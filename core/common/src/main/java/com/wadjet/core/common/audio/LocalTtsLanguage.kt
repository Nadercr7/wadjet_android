package com.wadjet.core.common.audio

import android.speech.tts.TextToSpeech
import java.util.Locale

/** True when the text contains Arabic-script characters. */
fun isArabicText(text: String): Boolean =
    text.any { it in '؀'..'ۿ' || it in 'ݐ'..'ݿ' }

/**
 * H-04: select the on-device TTS voice for the given text and verify the
 * device actually has it. Arabic text tries ar-EG first, then generic ar.
 *
 * @return false when no usable voice exists for the text's language —
 * callers should skip speaking rather than let an English voice mangle Arabic.
 */
fun TextToSpeech.trySetLanguageFor(text: String): Boolean {
    val candidates =
        if (isArabicText(text)) listOf(Locale("ar", "EG"), Locale("ar")) else listOf(Locale.US)
    for (locale in candidates) {
        val result = setLanguage(locale)
        if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
            return true
        }
    }
    return false
}
