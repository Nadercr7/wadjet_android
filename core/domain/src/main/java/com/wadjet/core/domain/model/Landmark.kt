package com.wadjet.core.domain.model

data class Landmark(
    val slug: String,
    val name: String,
    val nameAr: String?,
    val city: String?,
    val type: String?,
    val era: String?,
    val thumbnail: String?,
    val featured: Boolean,
    val popularity: Int,
)

data class LandmarkDetail(
    val slug: String,
    val name: String,
    val nameAr: String?,
    val city: String?,
    val type: String?,
    val subcategory: String?,
    val era: String?,
    val period: String?,
    val popularity: Int,
    val description: String?,
    val coordinates: Pair<Double, Double>?,
    val mapsUrl: String?,
    val thumbnail: String?,
    val tags: List<String>,
    val relatedSites: List<String>,
    val featured: Boolean,
    val images: List<LandmarkImage>,
    val sections: List<LandmarkSection>,
    val highlights: String?,
    val visitingTips: String?,
    val historicalSignificance: String?,
    val wikipediaUrl: String?,
    val recommendations: List<Recommendation>,
)

data class LandmarkImage(
    val url: String,
    val caption: String?,
)

data class LandmarkSection(
    val title: String,
    val content: String,
)

data class Recommendation(
    val slug: String,
    val name: String,
    val score: Float,
    val reasons: List<String>,
)

data class LandmarkPage(
    val landmarks: List<Landmark>,
    val total: Int,
    val page: Int,
    val totalPages: Int,
)

data class IdentifyMatch(
    val slug: String,
    val name: String,
    val confidence: Float,
)

data class IdentifyResult(
    val topMatch: IdentifyMatch?,
    val matches: List<IdentifyMatch>,
    val detail: LandmarkDetail?,
)
