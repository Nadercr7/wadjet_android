package com.wadjet.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TranslateRequest(
    val transliteration: String,
    @SerialName("gardiner_sequence") val gardinerSequence: String? = null,
)

@Serializable
data class TranslateResponse(
    val transliteration: String = "",
    val english: String = "",
    val arabic: String = "",
    val context: String = "",
    val error: String = "",
    val provider: String = "",
    @SerialName("latency_ms") val latencyMs: Long = 0,
    @SerialName("from_cache") val fromCache: Boolean = false,
)
