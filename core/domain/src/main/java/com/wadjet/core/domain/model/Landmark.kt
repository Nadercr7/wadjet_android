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
    val originalImage: String?,
    val tags: List<String>,
    val relatedSites: List<String>,
    val featured: Boolean,
    val images: List<LandmarkImage>,
    val sections: List<LandmarkSection>,
    val highlights: String?,
    val visitingTips: String?,
    val historicalSignificance: String?,
    val dynasty: String?,
    val notablePharaohs: List<String>,
    val notableTombs: List<String>,
    val notableFeatures: List<String>,
    val keyArtifacts: List<String>,
    val architecturalFeatures: List<String>,
    val wikipediaExtract: String?,
    val wikipediaUrl: String?,
    val children: List<LandmarkChild>,
    val parent: LandmarkParent?,
    val recommendations: List<Recommendation>,
)

data class LandmarkChild(
    val slug: String,
    val name: String,
    val nameAr: String?,
    val description: String?,
    val thumbnail: String?,
)

data class LandmarkParent(
    val slug: String,
    val name: String,
    val nameAr: String?,
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
    val source: String?,
)

data class IdentifyResult(
    val topMatch: IdentifyMatch?,
    val matches: List<IdentifyMatch>,
    val source: String?,
    val agreement: String?,
    val description: String?,
    val isKnownLandmark: Boolean,
    val isEgyptian: Boolean,
)
