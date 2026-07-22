package com.jordigordillo.brainzexplorer.data.remote

import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test

class RateLimitInterceptorTest {

    @Test
    fun `enforces at least 1 second between consecutive requests`() {
        val interceptor = RateLimitInterceptor()
        val request = Request.Builder().url("https://musicbrainz.org/ws/2/artist").build()
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        every { chain.proceed(any()) } returns Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("".toResponseBody(null))
            .build()

        val start = System.nanoTime()
        interceptor.intercept(chain)
        interceptor.intercept(chain)
        val elapsedMillis = (System.nanoTime() - start) / 1_000_000

        assertTrue("expected at least ~1000ms between requests, was ${elapsedMillis}ms", elapsedMillis >= 950)
    }
}
