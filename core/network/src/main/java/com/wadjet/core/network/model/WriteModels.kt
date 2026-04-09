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
    val mdc: String? = null,
)

@Serializable
data class WriteGlyphDto(
    @SerialName("gardiner_code") val gardinerCode: String = "",
    val glyph: String = "",
    val transliteration: String? = null,
    @SerialName("phonetic_value") val phoneticValue: String? = null,
    val meaning: String? = null,
)

@Serializable
data class PaletteResponse(
    val signs: List<PaletteSignDto> = emptyList(),
)

@Serializable
data class PaletteSignDto(
    val code: String = "",
    val glyph: String = "",
    val transliteration: String? = null,
    val category: String = "",
)

@Serializable
data class SpeakRequest(
    val text: String,
    val lang: String = "en",
    val context: String = "dictionary",
)
