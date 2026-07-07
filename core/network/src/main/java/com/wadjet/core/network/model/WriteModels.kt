package com.wadjet.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WriteRequest(
    val text: String,
    val mode: String,
)

@Serializable
data class WriteResponse(
    val hieroglyphs: String = "",
    val glyphs: List<WriteGlyphDto> = emptyList(),
    val mode: String = "",
    val input: String = "",
    val provider: String = "",
)

@Serializable
data class WriteGlyphDto(
    val type: String = "glyph",
    val code: String = "",
    val transliteration: String? = null,
    @SerialName("unicode_char") val unicodeChar: String = "",
    val description: String? = null,
    val verified: Boolean = false,
)

@Serializable
data class PaletteResponse(
    val groups: PaletteGroupsDto = PaletteGroupsDto(),
)

@Serializable
data class PaletteGroupsDto(
    val uniliteral: List<PaletteSignDto> = emptyList(),
    val biliteral: List<PaletteSignDto> = emptyList(),
    val triliteral: List<PaletteSignDto> = emptyList(),
    val logogram: List<PaletteSignDto> = emptyList(),
    val number: List<PaletteSignDto> = emptyList(),
    val determinative: List<PaletteSignDto> = emptyList(),
)

@Serializable
data class PaletteSignDto(
    val code: String = "",
    @SerialName("unicode_char") val unicodeChar: String = "",
    val transliteration: String? = null,
    val description: String? = null,
    @SerialName("phonetic_value") val phoneticValue: String? = null,
)

@Serializable
data class SpeakRequest(
    val text: String,
    val lang: String = "en",
    // Must match backend SpeakRequest (audio.py): text/lang/context only.
    // Voice and style are chosen server-side from the context preset (H-03).
    val context: String = "dictionary",
)

@Serializable
data class SttResponse(
    val text: String,
    val language: String = "en",
)
