package com.embystream.data.api

import com.embystream.data.model.UpdateResponse
import retrofit2.Response
import retrofit2.http.GET

interface UpdateApiService {
    @GET("repos/xmfx11/EmbyStream/releases/latest")
    suspend fun getLatestRelease(): Response<UpdateResponse>
}
