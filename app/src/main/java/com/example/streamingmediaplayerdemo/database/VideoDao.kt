package com.example.streamingmediaplayerdemo.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM video")
    fun getAllVideos(): Flow<List<Video>>

    @Upsert
    suspend fun upsertAll(videos: List<Video>)
}
