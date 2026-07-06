package com.embystream.utils

import com.embystream.data.api.AuthInterceptor
import com.embystream.data.api.EmbyApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    
    fun createApiService(server: String, token: String? = null): EmbyApiService {
        val baseUrl = if (server.endsWith("/")) server else "$server/"
        
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor())
            .build()
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmbyApiService::class.java)
    }
    
    fun createApiServiceWithToken(server: String, token: String): EmbyApiService {
        val baseUrl = if (server.endsWith("/")) server else "$server/"
        
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("X-Emby-Token", token)
                    .header("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmbyApiService::class.java)
    }
}

object ImageUrlBuilder {
    
    fun buildImageUrl(server: String, itemId: String?, imageTag: String?): String? {
        if (itemId.isNullOrEmpty() || imageTag.isNullOrEmpty()) return null
        val baseUrl = if (server.endsWith("/")) server else "$server/"
        return "${baseUrl}Items/$itemId/Images/Primary?tag=$imageTag"
    }
    
    fun buildPersonImageUrl(server: String, personId: String?, imageTag: String?): String? {
        if (personId.isNullOrEmpty() || imageTag.isNullOrEmpty()) return null
        val baseUrl = if (server.endsWith("/")) server else "$server/"
        return "${baseUrl}Items/$personId/Images/Primary?tag=$imageTag"
    }
}
