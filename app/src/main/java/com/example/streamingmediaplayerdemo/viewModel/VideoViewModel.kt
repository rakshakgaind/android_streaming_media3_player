package com.example.streamingmediaplayerdemo.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.streamingmediaplayerdemo.database.AppDatabase
import com.example.streamingmediaplayerdemo.database.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File


@SuppressLint("UnsafeOptInUsageError")
@UnstableApi
class VideoViewModel(application: Application,private val savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).videoDao()
    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos


    // Use Saved State Handle to persist currentPage
    var currentPage: Int
        get() = savedStateHandle.get<Int>("currentPage") ?: 0
        set(value) {
            savedStateHandle["currentPage"] = value
            Log.d("VideoViewModel", "Set currentPage: $value")
        }

    val exoPlayer: ExoPlayer by lazy {
        val cacheDir = File(application.cacheDir, "media_cache").apply { mkdirs() }
        val cache = Cache(cacheDir, 200L * 1024 * 1024)
        val okHttpClient = OkHttpClient.Builder()
            .cache(cache)
            .build()
        val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)

        ExoPlayer.Builder(application)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val videosToInsert = listOf(
                Video(
                    id = "1",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg",
                    title = "Big Buck Bunny"
                ),
                Video(
                    id = "2",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg",
                    title = "Elephants Dream"
                ),
                Video(
                    id = "3",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
                    thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerMeltdowns.jpg",
                    title = "ForBiggerMeltdowns"
                ),
                Video(
                    id = "4",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerEscapes.jpg",
                    title = "For Bigger Escape"
                ),
                Video(
                    id = "5",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerFun.jpg",
                    title = "For Bigger Fun"
                ),
                Video(
                    id = "6",
                    url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
                    thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerJoyrides.jpg",
                    title = "ForBiggerJoyrides"
                )
            )
            Log.d("VideoViewModel", "Inserting ${videosToInsert.size} videos")
            dao.upsertAll(videosToInsert)
            Log.d("VideoViewModel", "Inserted videos, checking DB: ${dao.getAllVideos().first().size}")
            dao.getAllVideos().collectLatest { videos ->
                Log.d("VideoViewModel", "Emitting ${videos.size} videos to StateFlow")
                _videos.value = videos
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                    val savedStateHandle = extras.createSavedStateHandle()
                    @Suppress("UNCHECKED_CAST")
                    return VideoViewModel(application, savedStateHandle) as T
                }
            }
        }
    }
}

