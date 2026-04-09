package com.wadjet.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LandmarkListResponse(
    val landmarks: List<LandmarkSummaryDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 24,
    @SerialName("total_pages") val totalPages: Int = 1,
)

@Serializable
data class LandmarkSummaryDto(
    val slug: String,
    val name: String = "",
    @SerialName("name_ar") val nameAr: String? = null,
    val city: String? = null,
    val type: String? = null,
    val era: String? = null,
    val thumbnail: String? = null,
    val featured: Boolean? = null,
    val popularity: Int? = null,
)

@Serializable
data class LandmarkDetailDto(
    val slug: String,
    val name: String = "",
    @SerialName("name_ar") val nameAr: String? = null,
    val city: String? = null,
    val type: String? = null,
    val subcategory: String? = null,
    val era: String? = null,
    val period: String? = null,
    val popularity: Int? = null,
    val description: String? = null,
    val coordinates: List<Double>? = null,
    @SerialName("maps_url") val mapsUrl: String? = null,
    val thumbnail: String? = null,
    val tags: List<String>? = null,
    @SerialName("related_sites") val relatedSites: List<String>? = null,
    val featured: Boolean? = null,
    val images: List<LandmarkImageDto>? = null,
    val sections: List<LandmarkSectionDto>? = null,
    val highlights: String? = null,
    @SerialName("visiting_tips") val visitingTips: String? = null,
    @SerialName("historical_significance") val historicalSignificance: String? = null,
    @SerialName("wikipedia_url") val wikipediaUrl: String? = null,
    val recommendations: List<RecommendationDto>? = null,
)

@Serializable
data class LandmarkImageDto(
    val url: String,
    val caption: String? = null,
)

@Serializable
data class LandmarkSectionDto(
    val title: String,
    val content: String,
)

@Serializable
data class RecommendationDto(
    val slug: String,
    val name: String = "",
    val score: Float = 0f,
    val reasons: List<String> = emptyList(),
)

@Serializable
data class LandmarkCategoriesResponse(
    val categories: List<String> = emptyList(),
)

@Serializable
data class LandmarkChildrenResponse(
    val children: List<LandmarkSummaryDto> = emptyList(),
)

@Serializable
data class IdentifyResponse(
    val slug: String? = null,
    val name: String? = null,
    val confidence: Float = 0f,
    val top3: List<IdentifyMatchDto> = emptyList(),
    val landmark: LandmarkDetailDto? = null,
)

@Serializable
data class IdentifyMatchDto(
    val slug: String,
    val name: String = "",
    val confidence: Float = 0f,
)
