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

data class UserLimits(
    val tier: String,
    val scansPerDay: Int,
    val chatMessagesPerDay: Int,
    val storiesAccessible: Int,
    val scansToday: Int,
    val chatMessagesToday: Int,
)
