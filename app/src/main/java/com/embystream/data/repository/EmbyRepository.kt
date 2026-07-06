package com.embystream.data.repository

import com.embystream.data.api.EmbyApiService
import com.embystream.data.model.EmbyItem
import com.embystream.data.model.LoginRequest
import com.embystream.data.model.LoginResponse
import com.embystream.data.model.PlaybackInfoResponse

class EmbyRepository(private val apiService: EmbyApiService) {
    
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(Username = username, Pw = password))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                Result.failure(Exception("登录失败 ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getViews(userId: String): Result<List<EmbyItem>> {
        return try {
            val response = apiService.getViews(userId)
            if (response.isSuccessful) {
                Result.success(response.body()?.Items ?: emptyList())
            } else {
                Result.failure(Exception("获取媒体库失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getItems(userId: String, parentId: String?): Result<List<EmbyItem>> {
        return try {
            val response = apiService.getItems(
                userId = userId,
                parentId = parentId,
                recursive = true,
                includeTypes = "Movie,Series",
                fields = "PrimaryImageAspectRatio,Overview,Taglines,People,MediaSources,ImageTags"
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.Items ?: emptyList())
            } else {
                Result.failure(Exception("获取内容失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getItemsByTag(userId: String, tag: String): Result<List<EmbyItem>> {
        return try {
            val response = apiService.getItemsByTag(userId, tag)
            if (response.isSuccessful) {
                Result.success(response.body()?.Items ?: emptyList())
            } else {
                Result.failure(Exception("标签检索失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getItemsByPerson(userId: String, personId: String): Result<List<EmbyItem>> {
        return try {
            val response = apiService.getItemsByPerson(userId, personId)
            if (response.isSuccessful) {
                Result.success(response.body()?.Items ?: emptyList())
            } else {
                Result.failure(Exception("演员检索失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPlaybackInfo(itemId: String, userId: String): Result<PlaybackInfoResponse> {
        return try {
            val response = apiService.getPlaybackInfo(itemId, userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("获取播放信息失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
