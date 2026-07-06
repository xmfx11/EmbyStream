package com.embystream.data.api

import com.embystream.data.model.EmbyUser
import com.embystream.data.model.ItemsResponse
import com.embystream.data.model.LoginRequest
import com.embystream.data.model.LoginResponse
import com.embystream.data.model.PlaybackInfoResponse
import com.embystream.data.model.ViewsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EmbyApiService {
    @POST("Users/AuthenticateByName")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @GET("Users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Response<EmbyUser>
    
    @GET("Users/{userId}/Views")
    suspend fun getViews(@Path("userId") userId: String): Response<ViewsResponse>
    
    @GET("Users/{userId}/Items")
    suspend fun getItems(
        @Path("userId") userId: String,
        @Query("ParentId") parentId: String? = null,
        @Query("Recursive") recursive: Boolean = true,
        @Query("IncludeItemTypes") includeTypes: String = "Movie,Series",
        @Query("Fields") fields: String = "PrimaryImageAspectRatio,Overview,Taglines,People,MediaSources,ImageTags"
    ): Response<ItemsResponse>
    
    @GET("Users/{userId}/Items")
    suspend fun getItemsByTag(
        @Path("userId") userId: String,
        @Query("Tags") tag: String,
        @Query("Recursive") recursive: Boolean = true,
        @Query("Fields") fields: String = "PrimaryImageAspectRatio,Overview,Taglines,People,MediaSources,ImageTags"
    ): Response<ItemsResponse>
    
    @GET("Users/{userId}/Items")
    suspend fun getItemsByPerson(
        @Path("userId") userId: String,
        @Query("Person") personId: String,
        @Query("Recursive") recursive: Boolean = true,
        @Query("Fields") fields: String = "PrimaryImageAspectRatio,Overview,Taglines,People,MediaSources,ImageTags"
    ): Response<ItemsResponse>
    
    @GET("Videos/{itemId}/PlaybackInfo")
    suspend fun getPlaybackInfo(
        @Path("itemId") itemId: String,
        @Query("UserId") userId: String
    ): Response<PlaybackInfoResponse>
}
