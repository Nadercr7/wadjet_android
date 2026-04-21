package com.wadjet.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PexelsSearchResponse(
    @SerialName("total_results") val totalResults: Int = 0,
    val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 1,
    val photos: List<PexelsPhoto> = emptyList(),
)

@Serializable
data class PexelsPhoto(
    val id: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val url: String = "",
    val photographer: String = "",
    @SerialName("photographer_url") val photographerUrl: String = "",
    val alt: String = "",
    val src: PexelsPhotoSrc = PexelsPhotoSrc(),
)

@Serializable
data class PexelsPhotoSrc(
    val original: String = "",
    val large2x: String = "",
    val large: String = "",
    val medium: String = "",
    val small: String = "",
    val portrait: String = "",
    val landscape: String = "",
    val tiny: String = "",
)
