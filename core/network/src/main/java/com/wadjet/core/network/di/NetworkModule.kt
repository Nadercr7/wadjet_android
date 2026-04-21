package com.wadjet.core.network.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.wadjet.core.network.AuthInterceptor
import com.wadjet.core.network.RateLimitInterceptor
import com.wadjet.core.network.TokenAuthenticator
import com.wadjet.core.network.TokenManager
import com.wadjet.core.network.api.AuthApiService
import com.wadjet.core.network.api.AudioApiService
import com.wadjet.core.network.api.ChatApiService
import com.wadjet.core.network.api.DictionaryApiService
import com.wadjet.core.network.api.LandmarkApiService
import com.wadjet.core.network.api.PexelsApiService
import com.wadjet.core.network.api.ScanApiService
import com.wadjet.core.network.api.FeedbackApiService
import com.wadjet.core.network.api.StoriesApiService
import com.wadjet.core.network.api.TranslateApiService
import com.wadjet.core.network.api.UserApiService
import com.wadjet.core.network.api.WriteApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        rateLimitInterceptor: RateLimitInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(rateLimitInterceptor)
        .authenticator(tokenAuthenticator)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (com.wadjet.core.network.BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            },
        )
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Wadjet-Android/${com.wadjet.core.network.BuildConfig.APP_VERSION}")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS) // SSE-safe
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
        @Named("baseUrl") baseUrl: String,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideDictionaryApiService(retrofit: Retrofit): DictionaryApiService =
        retrofit.create(DictionaryApiService::class.java)

    @Provides
    @Singleton
    fun provideWriteApiService(retrofit: Retrofit): WriteApiService =
        retrofit.create(WriteApiService::class.java)

    @Provides
    @Singleton
    fun provideAudioApiService(retrofit: Retrofit): AudioApiService =
        retrofit.create(AudioApiService::class.java)

    @Provides
    @Singleton
    fun provideScanApiService(retrofit: Retrofit): ScanApiService =
        retrofit.create(ScanApiService::class.java)

    @Provides
    @Singleton
    fun provideLandmarkApiService(retrofit: Retrofit): LandmarkApiService =
        retrofit.create(LandmarkApiService::class.java)

    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): ChatApiService =
        retrofit.create(ChatApiService::class.java)

    @Provides
    @Singleton
    fun provideStoriesApiService(retrofit: Retrofit): StoriesApiService =
        retrofit.create(StoriesApiService::class.java)

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideFeedbackApiService(retrofit: Retrofit): FeedbackApiService =
        retrofit.create(FeedbackApiService::class.java)

    @Provides
    @Singleton
    fun provideTranslateApiService(retrofit: Retrofit): TranslateApiService =
        retrofit.create(TranslateApiService::class.java)

    // ---- Pexels (image fallback) ----
    // Separate Retrofit instance with its own OkHttpClient that injects the
    // Pexels Authorization header. Rotates to a secondary key on HTTP 429
    // (rate limit) to effectively double the free-tier budget.
    @Provides
    @Singleton
    @Named("pexels")
    fun providePexelsOkHttpClient(): OkHttpClient {
        val keys = listOfNotNull(
            com.wadjet.core.network.BuildConfig.PEXELS_API_KEY.takeIf { it.isNotBlank() },
            com.wadjet.core.network.BuildConfig.PEXELS_API_KEY_2.takeIf { it.isNotBlank() },
        )
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val ua = "Wadjet-Android/${com.wadjet.core.network.BuildConfig.APP_VERSION}"
                var lastResponse: okhttp3.Response? = null
                for ((index, key) in keys.withIndex()) {
                    lastResponse?.close()
                    val req = chain.request().newBuilder()
                        .header("Authorization", key)
                        .header("User-Agent", ua)
                        .build()
                    val response = chain.proceed(req)
                    if (response.code != 429 || index == keys.lastIndex) {
                        return@addInterceptor response
                    }
                    lastResponse = response
                }
                // No keys configured — fall through with original request.
                chain.proceed(chain.request())
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("pexels")
    fun providePexelsRetrofit(
        @Named("pexels") client: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.pexels.com/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun providePexelsApiService(@Named("pexels") retrofit: Retrofit): PexelsApiService =
        retrofit.create(PexelsApiService::class.java)
}
