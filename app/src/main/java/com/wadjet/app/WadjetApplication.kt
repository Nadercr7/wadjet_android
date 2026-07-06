package com.wadjet.app

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.intercept.Interceptor
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Named

@HiltAndroidApp
class WadjetApplication : Application(), SingletonImageLoader.Factory, androidx.work.Configuration.Provider {

    @javax.inject.Inject lateinit var seedImporter: dagger.Lazy<com.wadjet.core.data.seed.SeedImporter>
    @javax.inject.Inject @com.wadjet.core.common.di.ApplicationScope lateinit var appScope: kotlinx.coroutines.CoroutineScope
    @javax.inject.Inject lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory
    @javax.inject.Inject lateinit var preferencesDataStore: com.wadjet.core.data.datastore.UserPreferencesDataStore
    @javax.inject.Inject lateinit var prefetchScheduler: com.wadjet.core.data.prefetch.StoryPrefetchScheduler

    // E-P1: WorkManager on-demand init with Hilt-injected workers
    override val workManagerConfiguration: androidx.work.Configuration
        get() = androidx.work.Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ImageLoaderEntryPoint {
        @Named("baseUrl")
        fun baseUrl(): String
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // A4: surface production warnings/errors in Crashlytics
            Timber.plant(CrashlyticsTree())
        }
        Timber.tag("Wadjet").i("App started; BuildConfig.DEBUG=${BuildConfig.DEBUG}")
        // E-04: populate signs/categories/landmarks from bundled seed on first run
        appScope.launch { seedImporter.get().seedIfEmpty() }
        // E-P1: keep the daily Wi-Fi story prefetch in sync with the Settings toggle
        appScope.launch {
            preferencesDataStore.prefetchStoriesOnWifi.collect { enabled ->
                if (enabled) prefetchScheduler.schedule(this@WadjetApplication)
                else prefetchScheduler.cancel(this@WadjetApplication)
            }
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val entryPoint = EntryPointAccessors.fromApplication(
            this,
            ImageLoaderEntryPoint::class.java,
        )
        val baseUrl = entryPoint.baseUrl().trimEnd('/')

        return ImageLoader.Builder(context)
            .crossfade(true)
            .components {
                add(BaseUrlInterceptor(baseUrl))
                // Dedicated OkHttpClient for image loading. Wikimedia (upload.wikimedia.org)
                // rejects the default "okhttp/..." User-Agent with HTTP 403, so we MUST
                // send a descriptive UA per their policy:
                // https://meta.wikimedia.org/wiki/User-Agent_policy
                val imageClient = OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val req = chain.request().newBuilder()
                            .header(
                                "User-Agent",
                                "WadjetAndroid/${BuildConfig.VERSION_NAME} " +
                                    "(https://wadjet.app; contact@wadjet.app) " +
                                    "okhttp/4.12.0",
                            )
                            .build()
                        chain.proceed(req)
                    }
                    .build()
                add(OkHttpNetworkFetcherFactory(callFactory = { imageClient }))
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_cache"))
                    .maxSizePercent(0.10)
                    .build()
            }
            .build()
    }
}

/**
 * Coil pipeline interceptor that prepends the server base URL to relative paths.
 * E.g. "/static/cache/images/foo.jpg" → "https://server.com/static/cache/images/foo.jpg"
 * E.g. "static/cache/images/foo.jpg" → "https://server.com/static/cache/images/foo.jpg"
 * Absolute URLs (http/https) and local file URIs are left unchanged.
 */
private class BaseUrlInterceptor(private val baseUrl: String) : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): coil3.request.ImageResult {
        val data = chain.request.data
        if (data is String && !data.startsWith("http") && !data.startsWith("file:") && !data.startsWith("content:")) {
            val path = if (data.startsWith("/")) data else "/$data"
            val newRequest = chain.request.newBuilder()
                .data("$baseUrl$path")
                .build()
            return chain.withRequest(newRequest).proceed()
        }
        return chain.proceed()
    }
}
