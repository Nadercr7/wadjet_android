package com.wadjet.core.domain.model

data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val preferredLang: String = "en",
    val tier: String = "free",
    val authProvider: String = "email",
    val emailVerified: Boolean = false,
    val avatarUrl: String? = null,
)
