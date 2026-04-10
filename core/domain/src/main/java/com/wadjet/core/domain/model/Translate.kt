package com.wadjet.core.domain.model

data class TranslationResult(
    val transliteration: String,
    val english: String,
    val arabic: String,
    val context: String,
    val provider: String,
    val fromCache: Boolean,
)
