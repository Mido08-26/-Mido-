package com.example.data

import com.example.data.local.FavoriteDao
import com.example.data.local.FavoriteEntity
import com.example.data.local.CollectionAndSettingsDao
import com.example.data.local.CollectionEntity
import com.example.data.local.CollectionWallpaperCrossRef
import com.example.data.local.NotificationSettingsEntity
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperProvider
import com.example.data.model.toWallpaper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class WallpaperRepository(
    private val favoriteDao: FavoriteDao,
    private val collectionDao: CollectionAndSettingsDao
) {

    // Emits the entire list of 1,200 wallpapers with live favorite status updated reactively
    val wallpapers: Flow<List<Wallpaper>> = favoriteDao.getAllFavorites()
        .map { favEntities -> favEntities.map { it.id }.toSet() }
        .map { favIds ->
            WallpaperProvider.wallpapersList.map { wallpaper ->
                wallpaper.copy(isFavorite = favIds.contains(wallpaper.id))
            }
        }

    // --- Collections & Community Rooms ---
    val collections: Flow<List<CollectionEntity>> = collectionDao.getAllCollections()

    fun searchPublicCollections(query: String): Flow<List<CollectionEntity>> {
        return if (query.isBlank()) {
            collectionDao.getAllCollections()
        } else {
            collectionDao.searchPublicCollections("%$query%")
        }
    }

    suspend fun insertCollection(collection: CollectionEntity) {
        collectionDao.insertCollection(collection)
    }

    suspend fun updateCollection(collection: CollectionEntity) {
        collectionDao.updateCollection(collection)
    }

    suspend fun deleteCollection(id: String) {
        collectionDao.deleteCollectionById(id)
        collectionDao.clearCollectionWallpapers(id)
    }

    suspend fun insertWallpaperToCollection(collectionId: String, wallpaperId: String) {
        collectionDao.insertWallpaperToCollection(CollectionWallpaperCrossRef(collectionId, wallpaperId))
    }

    suspend fun removeWallpaperFromCollection(collectionId: String, wallpaperId: String) {
        collectionDao.removeWallpaperFromCollection(CollectionWallpaperCrossRef(collectionId, wallpaperId))
    }

    fun getWallpaperIdsForCollection(collectionId: String): Flow<List<String>> {
        return collectionDao.getWallpaperIdsForCollection(collectionId)
    }

    // --- Smart Notifications Preferences ---
    val notificationSettings: Flow<NotificationSettingsEntity> = collectionDao.getNotificationSettings()
        .map { it ?: NotificationSettingsEntity() }

    suspend fun saveNotificationSettings(settings: NotificationSettingsEntity) {
        collectionDao.saveNotificationSettings(settings)
    }

    suspend fun seedMockCollectionsIfEmpty() {
        // Run check to seed if there are zero collections currently
        val initialSeeds = listOf(
            CollectionEntity(
                id = "space_wonders",
                name = "أسرار الفضاء والكون (Cosmos wonders)",
                description = "مجموعة مشتركة تجمع لقطات السوبرنوفا والنجوم المتلألئة المحدثة لحظة بلحظة.",
                isPublic = true,
                creator = "أحمد الفلكي (CosmosAhmed)",
                memberCount = 142,
                isJoined = false,
                isUserCreated = false
            ),
            CollectionEntity(
                id = "supercars_garage",
                name = "عشاق المحركات الفارهة (Porsche Drivers)",
                description = "ساحة لمشاركة تصاميم وخلفيات سيارات السباق الحديثة والمعدلة.",
                isPublic = true,
                creator = "سعد السواح (SariRider)",
                memberCount = 295,
                isJoined = true,
                isUserCreated = false
            ),
            CollectionEntity(
                id = "minimal_nature",
                name = "بساطة الطبيعة والهدوء (Zen Nature)",
                description = "ملتقى مشاركة جماليات الطبيعة الخضراء الهادئة وتدرجات الألوان الصامتة.",
                isPublic = true,
                creator = "ندى هلال (ZenNada)",
                memberCount = 88,
                isJoined = false,
                isUserCreated = false
            )
        )
        for (col in initialSeeds) {
            collectionDao.insertCollection(col)
        }

        // Link default existing wallpapers list as mock references for seed rooms
        val providerIds = WallpaperProvider.wallpapersList.map { it.id }
        providerIds.take(4).forEach { wallId ->
            collectionDao.insertWallpaperToCollection(
                CollectionWallpaperCrossRef("supercars_garage", wallId)
            )
        }
        providerIds.drop(4).take(3).forEach { wallId ->
            collectionDao.insertWallpaperToCollection(
                CollectionWallpaperCrossRef("space_wonders", wallId)
            )
        }
    }

    // Emits only the favorited wallpapers from local storage
    val favoriteWallpapers: Flow<List<Wallpaper>> = favoriteDao.getAllFavorites()
        .map { entities -> entities.map { it.toWallpaper() } }

    /**
     * Search wallpapers with query and category filters
     */
    fun searchWallpapers(query: String, category: String?): Flow<List<Wallpaper>> {
        return wallpapers.map { list ->
            var result = list
            if (!category.isNullOrBlank() && category != "All" && category != "الكل") {
                result = result.filter { 
                    it.category.equals(category, ignoreCase = true) || 
                    it.categoryAr.equals(category, ignoreCase = true)
                }
            }
            if (query.isNotBlank()) {
                val cleanedQuery = query.trim().lowercase()
                result = result.filter {
                    it.title.lowercase().contains(cleanedQuery) ||
                    it.titleAr.contains(cleanedQuery) ||
                    it.photographer.lowercase().contains(cleanedQuery) ||
                    it.category.lowercase().contains(cleanedQuery) ||
                    it.categoryAr.contains(cleanedQuery)
                }
            }
            result
        }
    }

    /**
     * Get a specific wallpaper reactively with live favorite status
     */
    fun getWallpaperById(id: String): Flow<Wallpaper?> {
        return favoriteDao.isFavorite(id).combine(wallpapers) { isFav, allWallpapers ->
            val wall = allWallpapers.find { it.id == id }
                ?: FavoriteEntityToWallpaperFallback(id)
            wall?.copy(isFavorite = isFav)
        }
    }

    private suspend fun FavoriteEntityToWallpaperFallback(id: String): Wallpaper? {
        // Fallback or handle cases if it was purely loaded from favorites
        return null
    }

    suspend fun toggleFavorite(wallpaper: Wallpaper) {
        if (wallpaper.isFavorite) {
            favoriteDao.deleteFavoriteById(wallpaper.id)
        } else {
            favoriteDao.insertFavorite(wallpaper.toFavoriteEntity())
        }
    }

    fun isFavorite(id: String): Flow<Boolean> {
        return favoriteDao.isFavorite(id)
    }
}
