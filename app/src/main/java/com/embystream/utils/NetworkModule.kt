package com.embystream.utils

import com.embystream.data.api.AuthInterceptor
import com.embystream.data.api.EmbyApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    fun createApiService(server: String, token: String? = null): EmbyApiService {
        val baseUrl = formatBaseUrl(server)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor())

        // 如果有 token，添加 token 拦截器
        if (!token.isNullOrEmpty()) {
            builder.addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("X-Emby-Token", token)
                    .build()
                chain.proceed(request)
            }
        }

        builder.addInterceptor(loggingInterceptor)

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmbyApiService::class.java)
    }

    fun createApiServiceWithToken(server: String, token: String): EmbyApiService {
        return createApiService(server, token)
    }

    private fun formatBaseUrl(server: String): String {
        var url = server.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
        }
        if (!url.endsWith("/")) {
            url = "$url/"
        }
        return url
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
