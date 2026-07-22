package com.jordigordillo.brainzexplorer.data.remote

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class UserAgentInterceptorTest {

    @Test
    fun `intercept adds a User-Agent header with the app version and contact email`() {
        val interceptor = UserAgentInterceptor(versionName = "1.2.3")
        val request = Request.Builder().url("https://musicbrainz.org/ws/2/artist").build()
        val chain = mockk<Interceptor.Chain>()
        val requestSlot = slot<Request>()
        every { chain.request() } returns request
        every { chain.proceed(capture(requestSlot)) } returns Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("".toResponseBody(null))
            .build()

        interceptor.intercept(chain)

        assertEquals(
            "BrainzExplorer/1.2.3 ( jordigordillo7@gmail.com )",
            requestSlot.captured.header("User-Agent")
        )
        assertEquals(request.url, requestSlot.captured.url)
        assertEquals(request.method, requestSlot.captured.method)
        verify { chain.proceed(any()) }
    }
}
