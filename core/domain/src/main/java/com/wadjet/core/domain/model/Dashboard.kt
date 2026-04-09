package com.wadjet.core.domain.model

data class UserStats(
    val scansToday: Int = 0,
    val totalScans: Int = 0,
    val storiesCompleted: Int = 0,
    val glyphsLearned: Int = 0,
)

data class ScanHistoryItem(
    val id: Int,
    val glyphCount: Int = 0,
    val confidenceAvg: Double? = null,
    val createdAt: String? = null,
)

data class FavoriteItem(
    val id: Int,
    val itemType: String,
    val itemId: String,
    val createdAt: String? = null,
)

data class DashboardData(
    val user: User?,
    val stats: UserStats,
    val recentScans: List<ScanHistoryItem>,
    val favorites: List<FavoriteItem>,
    val storyProgress: List<DashboardStoryProgress>,
)

data class DashboardStoryProgress(
    val storyId: String,
    val chapterIndex: Int = 0,
    val glyphsLearned: Int = 0,
    val score: Int = 0,
    val completed: Boolean = false,
)
