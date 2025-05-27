package com.example.streamingmediaplayerdemo.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

// Media3 Player using shared ExoPlayer instance
@Composable
fun Media3PlayerView(
    exoPlayer: ExoPlayer,
    modifier: Modifier = Modifier,
    onError: (String) -> Unit,
) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                errorMessage = "Playback error: ${error.message}"
                onError(errorMessage ?: "Unknown error")
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    if (errorMessage != null) {
        Text(
            text = errorMessage!!,
            color = Color.Red,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.7f)) // Improved transparency
                .padding(16.dp),
            textAlign = TextAlign.Center
        )
    } else {
        val playerView = remember { PlayerView(context).apply { useController = true } }
        AndroidView(
            factory = { playerView },
            update = { it.player = exoPlayer }, // Update player instead of recreating
            modifier = modifier
        )
    }
}
