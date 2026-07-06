package com.embystream.presentation.ui.player

import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.embystream.R
import com.embystream.data.local.TokenManager
import com.embystream.data.repository.EmbyRepository
import com.embystream.utils.NetworkModule
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class PlayerActivity : ComponentActivity() {
    private var player: ExoPlayer? = null
    private lateinit var surfaceView: SurfaceView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        
        surfaceView = findViewById(R.id.surface_view)
        val itemId = intent.getStringExtra("item_id") ?: ""
        val userId = runBlocking { TokenManager.getUserId() } ?: ""
        val server = runBlocking { TokenManager.getServer() } ?: ""
        val token = runBlocking { TokenManager.getToken() } ?: ""
        
        if (itemId.isEmpty()) {
            Toast.makeText(this, "无效的视频ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = NetworkModule.createApiServiceWithToken(server, token)
                val repository = EmbyRepository(apiService)
                val result = repository.getPlaybackInfo(itemId, userId)
                
                withContext(Dispatchers.Main) {
                    result.onSuccess { info ->
                        val sources = info.MediaSources ?: emptyList()
                        if (sources.isNotEmpty()) {
                            val directUrl = sources.firstOrNull()?.DirectStreamUrl
                            if (!directUrl.isNullOrEmpty()) {
                                val finalUrl = if (directUrl.startsWith("http")) {
                                    directUrl
                                } else {
                                    "$server$directUrl"
                                }
                                val urlWithToken = if (finalUrl.contains("api_key")) {
                                    finalUrl
                                } else {
                                    "$finalUrl${if (finalUrl.contains("?")) "&" else "?"}api_key=$token"
                                }
                                initializePlayer(urlWithToken, token)
                            } else {
                                Toast.makeText(this@PlayerActivity, "无播放地址", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        } else {
                            Toast.makeText(this@PlayerActivity, "无可用播放源", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }.onFailure { error ->
                        Toast.makeText(this@PlayerActivity, "获取播放信息失败: ${error.message}", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PlayerActivity, "错误: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
    
    private fun initializePlayer(videoUrl: String, token: String) {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("EmbyStream")
            .setDefaultRequestProperties(
                mapOf("X-Emby-Token" to token)
            )
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(30000)
            .setReadTimeoutMs(30000)
        
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(videoUrl))
        
        player = ExoPlayer.Builder(this).build()
        player?.setMediaSource(mediaSource)
        player?.setVideoSurfaceView(surfaceView)
        player?.prepare()
        player?.play()
        
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> Log.d("Player", "准备就绪")
                    Player.STATE_BUFFERING -> Log.d("Player", "缓冲中")
                    Player.STATE_ENDED -> Log.d("Player", "播放结束")
                    Player.STATE_IDLE -> Log.d("Player", "空闲")
                }
            }
            
            override fun onPlayerError(error: PlaybackException) {
                Log.e("Player", "播放错误", error)
                runOnUiThread {
                    Toast.makeText(this@PlayerActivity, "播放错误: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }
    
    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
    
    override fun onPause() {
        super.onPause()
        player?.pause()
    }
    
    override fun onResume() {
        super.onResume()
        player?.play()
    }
}
