package com.wadjet.app.di

import android.content.Context
import com.wadjet.app.BuildConfig
import com.wadjet.app.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("baseUrl")
    fun provideBaseUrl(): String = BuildConfig.BASE_URL.let { url ->
        if (url.endsWith("/")) url else "$url/"
    }

    @Provides
    @Singleton
    @Named("webClientId")
    fun provideWebClientId(@ApplicationContext context: Context): String =
        context.getString(R.string.default_web_client_id)
}
