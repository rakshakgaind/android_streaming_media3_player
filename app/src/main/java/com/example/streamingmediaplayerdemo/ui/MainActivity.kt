package com.example.streamingmediaplayerdemo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.streamingmediaplayerdemo.viewModel.VideoViewModel

class MainActivity : ComponentActivity() {
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: VideoViewModel by viewModels { VideoViewModel.Factory(application) }
        setContent {
            ShortVideoPlayer(viewModel)
        }
    }
}