package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.activity.compose.BackHandler
import androidx.lifecycle.ViewModelProvider
import com.example.data.WallpaperRepository
import com.example.data.local.AppDatabase
import com.example.ui.WallpaperViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.WallpaperDetailScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge to Edge bleed drawing
        enableEdgeToEdge()

        // Init local Database & Repository singleton layers
        val database = AppDatabase.getDatabase(applicationContext)
        val favoriteDao = database.favoriteDao()
        val collectionDao = database.collectionDao()
        val repository = WallpaperRepository(favoriteDao, collectionDao)

        setContent {
            // Setup Android ViewModel with factory injection
            val viewModel: WallpaperViewModel = ViewModelProvider(
                this,
                WallpaperViewModel.Factory(application, repository)
            )[WallpaperViewModel::class.java]

            val appLanguage by viewModel.appLanguage.collectAsState()
            val isArabic = appLanguage == "ar"
            val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            // State to monitor selected fullscreen item detail
            var activeWallpaper by remember { mutableStateOf<com.example.data.model.Wallpaper?>(null) }

            // Reactive favorites and collections database feedback loops
            val favorites by viewModel.favoriteWallpapers.collectAsState()
            val userCollections by viewModel.collectionsList.collectAsState()

            // Back pressed handler mapping back to dashboard safely
            BackHandler(enabled = activeWallpaper != null) {
                activeWallpaper = null
            }

            MyApplicationTheme {
                // Ensure layout direction adapts to selected in-app language
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            
                            // 1. Core Dashboard Layout with dynamic switching tabs
                            DashboardScreen(
                                viewModel = viewModel,
                                isArabic = isArabic,
                                onWallpaperSelected = { wall ->
                                    activeWallpaper = wall
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            // 2. Fullscreen transitions for our active Wallpaper Detail Preview screen
                            AnimatedVisibility(
                                visible = activeWallpaper != null,
                                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                            ) {
                                activeWallpaper?.let { currentWall ->
                                    // Make sure detail page reflects local database changes dynamically
                                    val isFavFlow = viewModel.favoriteWallpapers.collectAsState()
                                    val isNowFav = isFavFlow.value.any { it.id == currentWall.id }
                                    val reactiveWall = currentWall.copy(isFavorite = isNowFav)

                                    WallpaperDetailScreen(
                                        wallpaper = reactiveWall,
                                        isArabic = isArabic,
                                        collections = userCollections,
                                        onPinToCollection = { colId, wallId ->
                                            viewModel.pinWallpaperToCollection(colId, wallId)
                                        },
                                        onBack = { activeWallpaper = null },
                                        onFavoriteToggle = {
                                            viewModel.toggleFavorite(reactiveWall)
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
