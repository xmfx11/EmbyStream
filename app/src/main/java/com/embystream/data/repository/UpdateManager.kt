package com.embystream.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import com.embystream.BuildConfig
import com.embystream.data.api.UpdateApiService
import com.embystream.data.model.UpdateResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class UpdateManager(private val context: Context) {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val apiService = retrofit.create(UpdateApiService::class.java)
    
    suspend fun checkForUpdates(): Result<UpdateResponse> {
        return try {
            val response = apiService.getLatestRelease()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("获取更新信息失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun isUpdateAvailable(updateResponse: UpdateResponse): Boolean {
        val latestVersion = updateResponse.tag_name?.replace("v", "") ?: return false
        val currentVersion = BuildConfig.VERSION_NAME
        
        return compareVersions(latestVersion, currentVersion) > 0
    }
    
    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLength = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLength) {
            val p1 = if (i < parts1.size) parts1[i] else 0
            val p2 = if (i < parts2.size) parts2[i] else 0
            if (p1 > p2) return 1
            if (p1 < p2) return -1
        }
        return 0
    }
    
    fun getApkDownloadUrl(updateResponse: UpdateResponse): String? {
        return updateResponse.assets?.firstOrNull {
            it.name?.endsWith(".apk", ignoreCase = true) == true
        }?.browser_download_url
    }
    
    suspend fun downloadApk(url: String, onProgress: (Int) -> Unit): Result<File> {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.connect()
            
            val contentLength = connection.contentLength.toLong()
            val inputStream: InputStream = connection.inputStream
            
            val apkFile = File(context.externalCacheDir, "EmbyStream.apk")
            val outputStream = FileOutputStream(apkFile)
            
            val buffer = ByteArray(4096)
            var bytesRead: Int
            var totalRead: Long = 0
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalRead += bytesRead
                if (contentLength > 0) {
                    val progress = ((totalRead * 100) / contentLength).toInt()
                    onProgress(progress)
                }
            }
            
            outputStream.close()
            inputStream.close()
            connection.disconnect()
            
            Result.success(apkFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun installApk(file: File) {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                file
            )
        } else {
            Uri.fromFile(file)
        }
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        context.startActivity(intent)
    }
}
