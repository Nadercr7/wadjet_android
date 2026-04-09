package com.wadjet.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("preferred_lang") val preferredLang: String? = null,
)

@Serializable
data class ChangePasswordRequest(
    @SerialName("current_password") val currentPassword: String,
    @SerialName("new_password") val newPassword: String,
)

@Serializable
data class OkResponse(
    val ok: Boolean = true,
)

@Serializable
data class UserStatsResponse(
    @SerialName("scans_today") val scansToday: Int = 0,
    @SerialName("total_scans") val totalScans: Int = 0,
    @SerialName("stories_completed") val storiesCompleted: Int = 0,
    @SerialName("glyphs_learned") val glyphsLearned: Int = 0,
)

@Serializable
data class ScanHistoryItemDto(
    val id: Int,
    @SerialName("results_json") val resultsJson: String? = null,
    @SerialName("confidence_avg") val confidenceAvg: Double? = null,
    @SerialName("glyph_count") val glyphCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class FavoriteItemDto(
    val id: Int,
    @SerialName("item_type") val itemType: String,
    @SerialName("item_id") val itemId: String,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class AddFavoriteRequest(
    @SerialName("item_type") val itemType: String,
    @SerialName("item_id") val itemId: String,
)

@Serializable
data class StoryProgressItemDto(
    val id: Int = 0,
    @SerialName("story_id") val storyId: String,
    @SerialName("chapter_index") val chapterIndex: Int = 0,
    @SerialName("glyphs_learned") val glyphsLearned: Int = 0,
    val score: Int = 0,
    val completed: Boolean = false,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class UserLimitsResponse(
    val tier: String = "free",
    val limits: LimitsDto = LimitsDto(),
    val usage: UsageDto = UsageDto(),
)

@Serializable
data class LimitsDto(
    @SerialName("scans_per_day") val scansPerDay: Int = 10,
    @SerialName("chat_messages_per_day") val chatMessagesPerDay: Int = 20,
    @SerialName("stories_accessible") val storiesAccessible: Int = 3,
)

@Serializable
data class UsageDto(
    @SerialName("scans_today") val scansToday: Int = 0,
)
