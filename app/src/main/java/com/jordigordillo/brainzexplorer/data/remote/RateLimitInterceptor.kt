package com.jordigordillo.brainzexplorer.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MusicBrainz's API policy asks clients to keep requests to ~1/sec. This blocks the calling
 * (background) OkHttp dispatcher thread just long enough to keep that spacing between requests.
 */
@Singleton
class RateLimitInterceptor @Inject constructor() : Interceptor {

    private val lock = Any()
    private var lastRequestEndedAt = 0L

    override fun intercept(chain: Interceptor.Chain): Response {
        synchronized(lock) {
            val waitMillis = MIN_INTERVAL_MILLIS - (System.currentTimeMillis() - lastRequestEndedAt)
            if (waitMillis > 0) {
                Timber.tag("RateLimit").d("Throttling request for %dms", waitMillis)
                Thread.sleep(waitMillis)
            }
        }
        val response = chain.proceed(chain.request())
        synchronized(lock) {
            lastRequestEndedAt = System.currentTimeMillis()
        }
        return response
    }

    private companion object {
        const val MIN_INTERVAL_MILLIS = 1000L
    }
}
