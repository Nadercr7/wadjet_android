package com.wadjet.feature.explore.image

import android.content.Context
import android.content.SharedPreferences
import com.wadjet.core.network.api.PexelsApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves a reliable image URL for a landmark, falling back to Pexels when
 * the backend-provided thumbnail is missing or returns an error (e.g. many
 * Wikimedia URLs in the backend are stale/404).
 *
 * Results are cached in SharedPreferences keyed by landmark slug so Pexels
 * is only called once per landmark, keeping us well under the 200 req/hr
 * free-tier limit.
 */
@Singleton
class ThumbnailResolver @Inject constructor(
    @ApplicationContext context: Context,
    private val pexelsApi: PexelsApiService,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("wadjet_thumbnail_cache", Context.MODE_PRIVATE)
    private val mutex = Mutex()

    /** Returns a cached Pexels URL for the given slug, or null if not yet resolved. */
    fun cached(slug: String): String? = prefs.getString(slug, null)

    /** Returns a cached list of Pexels URLs for the given slug, or null if not resolved. */
    fun cachedMany(slug: String): List<String>? {
        val raw = prefs.getString("$slug::many", null) ?: return null
        return raw.split("\n").filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }
    }

    /**
     * Fetches a high-quality landscape photo from Pexels for the given landmark
     * and caches it. Returns null if Pexels returns no results or fails.
     *
     * [query] should be the landmark's English name, optionally with "Egypt"
     * appended for disambiguation.
     */
    suspend fun resolve(slug: String, query: String): String? {
        cached(slug)?.let { return it }
        return mutex.withLock {
            cached(slug)?.let { return@withLock it }
            try {
                val searchQuery = buildQuery(query)
                val response = pexelsApi.search(query = searchQuery, perPage = 1)
                if (!response.isSuccessful) {
                    Timber.w("Pexels search failed for '$searchQuery': HTTP ${response.code()}")
                    return@withLock null
                }
                val photo = response.body()?.photos?.firstOrNull()
                val url = photo?.src?.large ?: photo?.src?.medium ?: photo?.src?.original
                if (url != null) {
                    prefs.edit().putString(slug, url).apply()
                    Timber.d("Pexels resolved '$slug' -> $url")
                }
                url
            } catch (e: Exception) {
                Timber.w(e, "Pexels lookup failed for '$slug'")
                null
            }
        }
    }

    /**
     * Fetches up to [count] photos from Pexels for the given landmark and
     * caches them. The first result also populates the single-photo cache so
     * subsequent [resolve] calls for the same slug return instantly.
     */
    suspend fun resolveMany(slug: String, query: String, count: Int = 6): List<String> {
        cachedMany(slug)?.let { return it }
        return mutex.withLock {
            cachedMany(slug)?.let { return@withLock it }
            try {
                val searchQuery = buildQuery(query)
                val response = pexelsApi.search(query = searchQuery, perPage = count)
                if (!response.isSuccessful) {
                    Timber.w("Pexels multi search failed for '$searchQuery': HTTP ${response.code()}")
                    return@withLock emptyList()
                }
                val urls = response.body()?.photos.orEmpty().mapNotNull { p ->
                    p.src.large.takeIf { it.isNotBlank() }
                        ?: p.src.medium.takeIf { it.isNotBlank() }
                        ?: p.src.original.takeIf { it.isNotBlank() }
                }
                if (urls.isNotEmpty()) {
                    val editor = prefs.edit()
                    editor.putString("$slug::many", urls.joinToString("\n"))
                    if (prefs.getString(slug, null) == null) {
                        editor.putString(slug, urls.first())
                    }
                    editor.apply()
                    Timber.d("Pexels resolved ${urls.size} photos for '$slug'")
                }
                urls
            } catch (e: Exception) {
                Timber.w(e, "Pexels multi lookup failed for '$slug'")
                emptyList()
            }
        }
    }

    /**
     * Appends "Egypt" to disambiguate when the name is generic (e.g. "Dahab",
     * "Nile Cruise"), yielding better Pexels matches.
     */
    private fun buildQuery(name: String): String {
        val lower = name.lowercase()
        return if (lower.contains("egypt") || lower.contains("cairo") ||
            lower.contains("giza") || lower.contains("luxor") ||
            lower.contains("aswan") || lower.contains("sinai") ||
            lower.contains("nile")
        ) {
            name
        } else {
            "$name Egypt"
        }
    }
}
