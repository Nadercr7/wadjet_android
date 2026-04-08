package com.wadjet.app.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Splash : Route
    @Serializable data object Welcome : Route
    @Serializable data object Landing : Route
    @Serializable data object Scan : Route
    @Serializable data class ScanResult(val scanId: String) : Route
    @Serializable data object ScanHistory : Route
    @Serializable data object Dictionary : Route
    @Serializable data class DictionarySign(val code: String) : Route
    @Serializable data class Lesson(val level: Int) : Route
    @Serializable data object Explore : Route
    @Serializable data class LandmarkDetail(val slug: String) : Route
    @Serializable data object Identify : Route
    @Serializable data object Chat : Route
    @Serializable data class ChatLandmark(val slug: String) : Route
    @Serializable data object Stories : Route
    @Serializable data class StoryReader(val storyId: String) : Route
    @Serializable data object Dashboard : Route
    @Serializable data object Settings : Route
    @Serializable data object Feedback : Route
}
