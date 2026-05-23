package com.example.data.local

import androidx.room.Entity

@Entity(
    tableName = "collection_wallpaper_cross_ref",
    primaryKeys = ["collectionId", "wallpaperId"]
)
data class CollectionWallpaperCrossRef(
    val collectionId: String,
    val wallpaperId: String
)
