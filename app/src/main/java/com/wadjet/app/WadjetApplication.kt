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
import timber.log.Timber
import javax.inject.Named

@HiltAndroidApp
class WadjetApplication : Application(), SingletonImageLoader.Factory {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ImageLoaderEntryPoint {
        fun okHttpClient(): OkHttpClient

        @Named("baseUrl")
        fun baseUrl(): String
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val entryPoint = EntryPointAccessors.fromApplication(
            this,
            ImageLoaderEntryPoint::class.java,
        )
        val baseUrl = entryPoint.baseUrl().trimEnd('/')
        val okHttpClient = entryPoint.okHttpClient()

        return ImageLoader.Builder(context)
            .crossfade(true)
            .components {
                add(BaseUrlInterceptor(baseUrl))
                add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_cache"))
                    .maxSizePercent(0.05)
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
