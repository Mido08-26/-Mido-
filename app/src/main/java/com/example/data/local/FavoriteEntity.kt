package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val id: String,
    val url: String,
    val thumbnailUrl: String,
    val title: String,
    val titleAr: String,
    val category: String,
    val categoryAr: String,
    val photographer: String,
    val width: Int,
    val height: Int,
    val timestamp: Long = System.currentTimeMillis()
)
