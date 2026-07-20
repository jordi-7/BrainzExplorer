package com.jordigordillo.brainzexplorer.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * MusicBrainz requires a descriptive User-Agent identifying the app and a way to contact its
 * maintainer.
 */
class UserAgentInterceptor(private val versionName: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", "BrainzExplorer/$versionName ( jordigordillo7@gmail.com )")
            .build()
        return chain.proceed(request)
    }
}
