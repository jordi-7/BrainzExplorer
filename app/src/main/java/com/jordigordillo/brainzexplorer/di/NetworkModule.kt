package com.jordigordillo.brainzexplorer.di

import android.content.Context
import com.jordigordillo.brainzexplorer.R
import com.jordigordillo.brainzexplorer.data.remote.MusicBrainzApi
import com.jordigordillo.brainzexplorer.data.remote.RateLimitInterceptor
import com.jordigordillo.brainzexplorer.data.remote.UserAgentInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

private const val MUSICBRAINZ_BASE_URL = "https://musicbrainz.org/ws/2/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideUserAgentInterceptor(@ApplicationContext context: Context): UserAgentInterceptor =
        UserAgentInterceptor(context.getString(R.string.app_version_name))

    @Provides
    @Singleton
    fun provideOkHttpClient(
        userAgentInterceptor: UserAgentInterceptor,
        rateLimitInterceptor: RateLimitInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(rateLimitInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                },
            )
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(MUSICBRAINZ_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideMusicBrainzApi(retrofit: Retrofit): MusicBrainzApi =
        retrofit.create(MusicBrainzApi::class.java)
}
