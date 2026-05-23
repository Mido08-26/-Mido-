package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val isPublic: Boolean,
    val creator: String,
    val memberCount: Int,
    val isJoined: Boolean,
    val isUserCreated: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
