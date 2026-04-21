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
    // Backend stores this as a JSON-encoded string (e.g. "[]" or "[\"A1\"]") in SQLite
    // and returns the raw column value. We accept it as a string and compute the count
    // in the repository. Use `glyphsLearnedCount` for the numeric value.
    @SerialName("glyphs_learned") val glyphsLearned: String = "[]",
    val score: Int = 0,
    val completed: Boolean = false,
    @SerialName("updated_at") val updatedAt: String? = null,
) {
    /** Number of glyphs learned, parsed from the JSON array string. */
    val glyphsLearnedCount: Int
        get() = runCatching {
            kotlinx.serialization.json.Json
                .parseToJsonElement(glyphsLearned)
                .let { el ->
                    if (el is kotlinx.serialization.json.JsonArray) el.size else 0
                }
        }.getOrDefault(0)
}

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
    @SerialName("chat_messages_today") val chatMessagesToday: Int = 0,
)

@Serializable
data class SaveProgressRequest(
    @SerialName("story_id") val storyId: String,
    @SerialName("chapter_index") val chapterIndex: Int,
    @SerialName("glyphs_learned") val glyphsLearned: List<String>,
    val score: Int,
    val completed: Boolean,
)
