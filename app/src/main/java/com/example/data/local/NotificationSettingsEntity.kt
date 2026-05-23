package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_settings")
data class NotificationSettingsEntity(
    @PrimaryKey
    val id: String = "global_settings",
    val notificationsEnabled: Boolean = true,
    val frequency: String = "daily", // daily, weekly, manual
    val notifyNature: Boolean = true,
    val notifySpace: Boolean = true,
    val notifyCars: Boolean = true,
    val notifyMinimal: Boolean = true,
    val notifyCommunity: Boolean = true
)
