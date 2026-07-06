package com.embystream.data.api

import com.embystream.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = runBlocking { TokenManager.getToken() }
        
        val request = original.newBuilder()
            .apply {
                if (!token.isNullOrEmpty()) {
                    header("X-Emby-Token", token)
                }
            }
            .header("Content-Type", "application/json")
            .build()
        
        return chain.proceed(request)
    }
}
