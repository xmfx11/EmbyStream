package com.embystream.data.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    companion object {
        const val EMBY_AUTH = "MediaBrowser Client=\"EmbyStream\", Device=\"Android\", DeviceId=\"EmbyStream-Android-001\", Version=\"0.04\""
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val request = original.newBuilder()
            .header("X-Emby-Authorization", EMBY_AUTH)
            .header("Accept", "application/json")
            .apply {
                if (original.method == "POST" || original.method == "PUT") {
                    header("Content-Type", "application/json")
                }
            }
            .build()

        return chain.proceed(request)
    }
}
