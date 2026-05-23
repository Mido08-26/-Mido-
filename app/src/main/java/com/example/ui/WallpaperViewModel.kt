package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.WallpaperRepository
import com.example.data.local.AppDatabase
import com.example.data.model.Wallpaper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class WallpaperViewModel(
    application: Application,
    private val repository: WallpaperRepository
) : AndroidViewModel(application) {

    init {
        viewModelScope.launch {
            try {
                // Ensure default collaborative collection cards exist
                repository.seedMockCollectionsIfEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Language configuration ("ar" or "en")
    private val _isEnglishNative = getSystemLocaleIsEnglish()
    private val _appLanguage = MutableStateFlow(if (_isEnglishNative) "en" else "ar")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    // Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // --- Custom Shared Collections State flows ---
    private val _collectionsSearchQuery = MutableStateFlow("")
    val collectionsSearchQuery: StateFlow<String> = _collectionsSearchQuery.asStateFlow()

    val collectionsList: StateFlow<List<com.example.data.local.CollectionEntity>> = _collectionsSearchQuery
        .flatMapLatest { query ->
            repository.searchPublicCollections(query)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCollectionsSearchQuery(query: String) {
        _collectionsSearchQuery.value = query
    }

    fun joinOrLeaveCollection(collection: com.example.data.local.CollectionEntity) {
        viewModelScope.launch {
            val updated = collection.copy(
                isJoined = !collection.isJoined,
                memberCount = collection.memberCount + (if (collection.isJoined) -1 else 1)
            )
            repository.updateCollection(updated)
        }
    }

    fun createCollection(name: String, description: String, isPublic: Boolean) {
        viewModelScope.launch {
            val newCol = com.example.data.local.CollectionEntity(
                id = "user_col_${System.currentTimeMillis()}",
                name = name,
                description = description,
                isPublic = isPublic,
                creator = if (appLanguage.value == "ar") "أنت (صاحب الغرفة)" else "You (Owner)",
                memberCount = 1,
                isJoined = true,
                isUserCreated = true
            )
            repository.insertCollection(newCol)
        }
    }

    fun pinWallpaperToCollection(collectionId: String, wallpaperId: String) {
        viewModelScope.launch {
            repository.insertWallpaperToCollection(collectionId, wallpaperId)
        }
    }

    fun unpinWallpaperFromCollection(collectionId: String, wallpaperId: String) {
        viewModelScope.launch {
            repository.removeWallpaperFromCollection(collectionId, wallpaperId)
        }
    }

    fun getWallpaperIdsInCollection(collectionId: String): Flow<List<String>> {
        return repository.getWallpaperIdsForCollection(collectionId)
    }

    // --- Notifications State & Methods ---
    val notificationSettings: StateFlow<com.example.data.local.NotificationSettingsEntity> = repository.notificationSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.example.data.local.NotificationSettingsEntity()
        )

    fun updateNotificationSettings(settings: com.example.data.local.NotificationSettingsEntity) {
        viewModelScope.launch {
            repository.saveNotificationSettings(settings)
        }
    }

    // Exposed Wallpapers matching Query & Category filters
    val filteredWallpapers: StateFlow<List<Wallpaper>> = combine(
        _searchQuery,
        _selectedCategory,
        _appLanguage // Reactive language-specific labels
    ) { query, category, _ ->
        Pair(query, category)
    }.flatMapLatest { (query, category) ->
        val mappedCategory = when(category) {
            "All", "الكل" -> "All"
            "Nature", "طبيعة" -> "Nature"
            "Minimal", "بساطة" -> "Minimal"
            "Anime", "أنمي" -> "Anime"
            "Abstract", "تجريدي" -> "Abstract"
            "Space", "الفضاء" -> "Space"
            "Cars", "سيارات" -> "Cars"
            "Dark Mode", "الوضع الداكن", "Dark" -> "Dark"
            "Architecture", "عمارة" -> "Architecture"
            else -> category
        }
        repository.searchWallpapers(query, mappedCategory)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Exposed Favorited Wallpapers
    val favoriteWallpapers: StateFlow<List<Wallpaper>> = repository.favoriteWallpapers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun getSystemLocaleIsEnglish(): Boolean {
        val currentLocale = Locale.getDefault()
        return !currentLocale.language.startsWith("ar")
    }

    fun toggleLanguage() {
        val nextLang = if (_appLanguage.value == "en") "ar" else "en"
        _appLanguage.value = nextLang
    }

    fun setLanguage(lang: String) {
        _appLanguage.value = lang
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch {
            repository.toggleFavorite(wallpaper)
        }
    }

    // Factory to construct this ViewModel easily in MainActivity
    class Factory(
        private val application: Application,
        private val repository: WallpaperRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WallpaperViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WallpaperViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
