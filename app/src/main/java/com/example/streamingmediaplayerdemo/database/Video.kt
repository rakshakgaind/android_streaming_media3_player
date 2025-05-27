package com.example.streamingmediaplayerdemo.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room Database Setup
@Entity
data class Video(
    @PrimaryKey val id: String,
    val url: String,
    val thumbnailUrl: String,
    val title: String,
)