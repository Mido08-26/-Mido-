package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionAndSettingsDao {

    // --- Collections ---
    @Query("SELECT * FROM collections ORDER BY timestamp DESC")
    fun getAllCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections WHERE isPublic = 1 AND (name LIKE :query OR description LIKE :query)")
    fun searchPublicCollections(query: String): Flow<List<CollectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity)

    @Update
    suspend fun updateCollection(collection: CollectionEntity)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteCollectionById(id: String)


    // --- Wallpaper Mapping ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWallpaperToCollection(ref: CollectionWallpaperCrossRef)

    @Delete
    suspend fun removeWallpaperFromCollection(ref: CollectionWallpaperCrossRef)

    @Query("DELETE FROM collection_wallpaper_cross_ref WHERE collectionId = :collectionId")
    suspend fun clearCollectionWallpapers(collectionId: String)

    @Query("SELECT wallpaperId FROM collection_wallpaper_cross_ref WHERE collectionId = :collectionId")
    fun getWallpaperIdsForCollection(collectionId: String): Flow<List<String>>


    // --- Smart Notifications Settings ---
    @Query("SELECT * FROM notification_settings WHERE id = 'global_settings' LIMIT 1")
    fun getNotificationSettings(): Flow<NotificationSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNotificationSettings(settings: NotificationSettingsEntity)
}
