package com.example.streamingmediaplayerdemo.ui

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.example.streamingmediaplayerdemo.view.Media3PlayerView
import com.example.streamingmediaplayerdemo.viewModel.VideoViewModel

// Main Short Video Player Composable
@OptIn(UnstableApi::class)
@Composable
fun ShortVideoPlayer(viewModel: VideoViewModel) {
    val videos by viewModel.videos.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    // Initialize pagerState with currentPage from ViewModel
    val pagerState = rememberPagerState(
        initialPage = viewModel.currentPage,
        pageCount = { videos.size }
    )
    var playbackPosition by rememberSaveable { mutableLongStateOf(0L) }

    // Handle lifecycle events for pause/resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    Log.d("ShortVideoPlayer", "Pausing video at position: ${viewModel.exoPlayer.currentPosition}")
                    playbackPosition = viewModel.exoPlayer.currentPosition
                    viewModel.exoPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    Log.d("ShortVideoPlayer", "Resuming video at position: $playbackPosition")
                    viewModel.exoPlayer.seekTo(pagerState.currentPage, playbackPosition)
                    viewModel.exoPlayer.play()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Update media items and restore playback on page change or initial load
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            Log.d("ShortVideoPlayer", "Current page: $page, Videos size: ${videos.size}")
            viewModel.currentPage = page // Save current page
            val video = videos.getOrNull(page)
            if (video != null) {
                Log.d("ShortVideoPlayer", "Setting video: ${video.url}")
                if (viewModel.exoPlayer.mediaItemCount == 0) {
                    viewModel.exoPlayer.setMediaItems(
                        videos.map { MediaItem.fromUri(it.url.toUri()) }
                    )
                    viewModel.exoPlayer.seekTo(page, playbackPosition)
                    viewModel.exoPlayer.prepare()
                    viewModel.exoPlayer.play()
                } else {
                    viewModel.exoPlayer.seekTo(page, playbackPosition)
                    viewModel.exoPlayer.play()
                }
            } else {
                Log.w("ShortVideoPlayer", "No video for page $page")
            }
        }
    }

    when {
        videos.isEmpty() -> {
            Text(
                text = "Loading videos...",
                color = Color.White,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
        else -> {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val video = videos.getOrNull(page)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (video != null) {
                        if (pagerState.currentPage != page) {
                            AsyncImage(
                                model = video.thumbnailUrl,
                                contentDescription = video.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Media3PlayerView(
                                exoPlayer = viewModel.exoPlayer,
                                modifier = Modifier.fillMaxSize(),
                                onError = { error ->
                                    errorMessage = error
                                    Log.e("ShortVideoPlayer", "Error: $error")
                                }
                            )
                        }
                    }
                    if (errorMessage != null && pagerState.currentPage == page) {
                        Text(
                            text = errorMessage!!,
                            color = Color.Red,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}