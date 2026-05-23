package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Wallpaper
import com.example.ui.WallpaperViewModel
import com.example.ui.components.CategorySelector
import com.example.ui.components.WallpaperCard

@Composable
fun DashboardScreen(
    viewModel: WallpaperViewModel,
    isArabic: Boolean,
    onWallpaperSelected: (Wallpaper) -> Unit,
    modifier: Modifier = Modifier
) {
    // Current active bottom navigation state
    var activeTab by remember { mutableStateOf(0) } // 0: Explore, 1: Search, 2: Favorites

    // UI States observed reactively from ViewModel
    val wallpapers by viewModel.filteredWallpapers.collectAsState()
    val favorites by viewModel.favoriteWallpapers.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            // High-fidelity Material 3 bottom navigation bar
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_navigation_bar"),
                tonalElevation = 8.dp
            ) {
                // Explore Tab
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == 0) Icons.Filled.Explore else Icons.Outlined.Explore,
                            contentDescription = stringResource(R.string.explore_tab)
                        )
                    },
                    label = { Text(stringResource(R.string.explore_tab)) },
                    modifier = Modifier.testTag("nav_explore")
                )

                // Smart Search Tab
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == 1) Icons.Filled.Search else Icons.Outlined.Search,
                            contentDescription = stringResource(R.string.search_tab)
                        )
                    },
                    label = { Text(stringResource(R.string.search_tab)) },
                    modifier = Modifier.testTag("nav_search")
                )

                // Local Favorites Tab
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == 2) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                            contentDescription = stringResource(R.string.favorites_tab)
                        )
                    },
                    label = { Text(stringResource(R.string.favorites_tab)) },
                    modifier = Modifier.testTag("nav_favorites")
                )

                // Shared Collections Tab
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == 3) Icons.Filled.Group else Icons.Outlined.Group,
                            contentDescription = stringResource(R.string.nav_collections)
                        )
                    },
                    label = { Text(stringResource(R.string.nav_collections)) },
                    modifier = Modifier.testTag("nav_collections")
                )

                // Notifications Options Tab
                NavigationBarItem(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == 4) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                            contentDescription = stringResource(R.string.nav_notifications)
                        )
                    },
                    label = { Text(stringResource(R.string.nav_notifications)) },
                    modifier = Modifier.testTag("nav_notifications")
                )
            }
        }
    ) { innerPadding ->
        // Adaptive container layout based on available width (supporting tablets beautifully)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val columnsCount = if (maxWidth > 600.dp) 3 else 2

            Column(modifier = Modifier.fillMaxSize()) {
                // App Brand Bar with Language Toggle Accent
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = if (isArabic) "عالم الخلفيات المذهلة" else "Your ultimate custom screens",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    // Frosted Language Switcher Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .clickable { viewModel.toggleLanguage() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("locale_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Language,
                                contentDescription = "Language",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.toggle_language),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                // Display appropriate pane based on selections
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (activeTab) {
                        0 -> ExplorePane(
                            wallpapers = wallpapers,
                            selectedCategory = selectedCategory,
                            isArabic = isArabic,
                            columnsCount = columnsCount,
                            onCategorySelected = { viewModel.selectCategory(it) },
                            onWallpaperClick = onWallpaperSelected,
                            onFavoriteToggle = { viewModel.toggleFavorite(it) }
                        )
                        1 -> SmartSearchPane(
                            viewModel = viewModel,
                            wallpapers = wallpapers,
                            searchQuery = searchQuery,
                            isArabic = isArabic,
                            columnsCount = columnsCount,
                            onWallpaperClick = onWallpaperSelected,
                            onFavoriteToggle = { viewModel.toggleFavorite(it) }
                        )
                        2 -> FavoritesPane(
                            favoritesList = favorites,
                            isArabic = isArabic,
                            columnsCount = columnsCount,
                            onWallpaperClick = onWallpaperSelected,
                            onFavoriteToggle = { viewModel.toggleFavorite(it) }
                        )
                        3 -> CollectionsPane(
                            viewModel = viewModel,
                            isArabic = isArabic,
                            onWallpaperClick = onWallpaperSelected
                        )
                        4 -> NotificationsPane(
                            viewModel = viewModel,
                            isArabic = isArabic
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExplorePane(
    wallpapers: List<Wallpaper>,
    selectedCategory: String,
    isArabic: Boolean,
    columnsCount: Int,
    onCategorySelected: (String) -> Unit,
    onWallpaperClick: (Wallpaper) -> Unit,
    onFavoriteToggle: (Wallpaper) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Daily Updates Announcement Banner Cards
        DailyUpdatesBannerCard(isArabic = isArabic)

        // Categories selector horizontal bar
        CategorySelector(
            selectedCategory = selectedCategory,
            isArabic = isArabic,
            onCategorySelected = onCategorySelected
        )

        // Subheader showing available content
        Text(
            text = java.lang.String.format(
                stringResource(R.string.all_wallpapers_subheader),
                wallpapers.size
            ),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Wallpapers grid layout
        if (wallpapers.isEmpty()) {
            EmptyListPlaceholder(
                title = stringResource(R.string.no_results_title),
                description = stringResource(R.string.no_results_desc)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnsCount),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("explore_grid_view")
            ) {
                items(wallpapers, key = { it.id }) { item ->
                    WallpaperCard(
                        wallpaper = item,
                        isArabic = isArabic,
                        onClick = { onWallpaperClick(item) },
                        onFavoriteToggle = { onFavoriteToggle(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun SmartSearchPane(
    viewModel: WallpaperViewModel,
    wallpapers: List<Wallpaper>,
    searchQuery: String,
    isArabic: Boolean,
    columnsCount: Int,
    onWallpaperClick: (Wallpaper) -> Unit,
    onFavoriteToggle: (Wallpaper) -> Unit
) {
    val focusManager = LocalFocusManager.current

    // Curated search shortcut chips for quick interaction
    val recommendations = if (isArabic) {
        listOf("طبيعة", "سيارات", "الفضاء", "بسيط", "غروب", "بورشه", "نيون", "داكن")
    } else {
        listOf("Forest", "Porsche", "Space", "Minimal", "Sunset", "Neon", "Abstract", "Classic")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Smart Search input field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("smart_search_text_input"),
            placeholder = { Text(stringResource(R.string.search_placeholder), fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        // Recommended chips row
        Text(
            text = if (isArabic) "اقتراحات سريعة" else "Quick Shortcuts",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Horizontal tags line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Render top 4 items horizontally to keep safe bounds
            recommendations.take(4).forEach { rec ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            viewModel.setSearchQuery(rec)
                            focusManager.clearFocus()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("search_recommendation_$rec"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rec,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // Search Result Count
        if (searchQuery.isNotEmpty()) {
            Text(
                text = if (isArabic) "نتائج البحث (${wallpapers.size} خلفية)" else "Search Results (${wallpapers.size} wallpapers)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Results Grid
        if (wallpapers.isEmpty()) {
            EmptyListPlaceholder(
                title = stringResource(R.string.no_results_title),
                description = stringResource(R.string.no_results_desc)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnsCount),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("search_results_grid")
            ) {
                items(wallpapers, key = { it.id }) { item ->
                    WallpaperCard(
                        wallpaper = item,
                        isArabic = isArabic,
                        onClick = { onWallpaperClick(item) },
                        onFavoriteToggle = { onFavoriteToggle(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritesPane(
    favoritesList: List<Wallpaper>,
    isArabic: Boolean,
    columnsCount: Int,
    onWallpaperClick: (Wallpaper) -> Unit,
    onFavoriteToggle: (Wallpaper) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = if (isArabic) "خلفياتك المفضلة (${favoritesList.size})" else "Your Saved Favorites (${favoritesList.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        if (favoritesList.isEmpty()) {
            EmptyListPlaceholder(
                title = stringResource(R.string.no_favorites_title),
                description = stringResource(R.string.no_favorites_desc)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnsCount),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("favorites_grid")
            ) {
                items(favoritesList, key = { it.id }) { item ->
                    WallpaperCard(
                        wallpaper = item,
                        isArabic = isArabic,
                        onClick = { onWallpaperClick(item) },
                        onFavoriteToggle = { onFavoriteToggle(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun DailyUpdatesBannerCard(isArabic: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .height(96.dp)
            .testTag("daily_updates_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Sleek side neon vector shading simulated with Canvas
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.NewReleases,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.daily_updated_tag),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.daily_updated_sub),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyListPlaceholder(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .testTag("empty_list_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.SentimentDissatisfied,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsPane(
    viewModel: WallpaperViewModel,
    isArabic: Boolean,
    onWallpaperClick: (Wallpaper) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val searchQuery by viewModel.collectionsSearchQuery.collectAsState()
    val collectionsList by viewModel.collectionsList.collectAsState()
    
    var showCreateSheet by remember { mutableStateOf(false) }
    var selectCollectionForDetails by remember { mutableStateOf<com.example.data.local.CollectionEntity?>(null) }
    var showInviteDialog by remember { mutableStateOf<String?>(null) }

    if (showInviteDialog != null) {
        val friends = listOf("علي اليافعي", "Sarah Walker", "محمد القاسم", "Emilie Rose", "فاطمة أحمد")
        AlertDialog(
            onDismissRequest = { showInviteDialog = null },
            title = {
                Text(
                    text = stringResource(R.string.coll_invite_dialog_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    friends.forEach { friend ->
                        var isInvited by remember { mutableStateOf(false) }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { isInvited = !isInvited }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(friend, style = MaterialTheme.typography.bodyLarge)
                            Checkbox(checked = isInvited, onCheckedChange = { isInvited = it })
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInviteDialog = null
                        android.widget.Toast.makeText(context, context.getString(R.string.coll_invite_success_toast), android.widget.Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(if (isArabic) "إرسال الدعوات" else "Send Invites")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = null }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    if (selectCollectionForDetails != null) {
        val col = selectCollectionForDetails!!
        val pinnedWallpapersIds by viewModel.getWallpaperIdsInCollection(col.id).collectAsState(initial = emptyList())
        val allWallpapers by viewModel.filteredWallpapers.collectAsState()
        val mappedPinnedWallpapers = allWallpapers.filter { pinnedWallpapersIds.contains(it.id) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { selectCollectionForDetails = null }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = col.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.primary
                )
                if (col.isJoined) {
                    IconButton(onClick = { showInviteDialog = col.id }) {
                        Icon(imageVector = Icons.Filled.PersonAdd, contentDescription = "Invite")
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(col.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "${if (isArabic) "المؤسس: " else "Founder: "} ${col.creator}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${if (isArabic) "الأعضاء: " else "Members: "} ${col.memberCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            if (mappedPinnedWallpapers.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isArabic) "لا توجد خلفيات مضافة لهذه المجموعة حالياً" else "No matching pinned papers inside this group",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(mappedPinnedWallpapers) { wall ->
                        WallpaperCard(
                            wallpaper = wall,
                            isArabic = isArabic,
                            onClick = { onWallpaperClick(wall) },
                            onFavoriteToggle = { viewModel.toggleFavorite(wall) }
                        )
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.coll_header_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = { showCreateSheet = true },
                    modifier = Modifier.testTag("button_create_collection"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isArabic) "إنشاء غرفتي" else "Create")
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setCollectionsSearchQuery(it) },
                placeholder = { Text(stringResource(R.string.coll_search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Group, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("collection_search_bar"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            if (collectionsList.isEmpty()) {
                EmptyListPlaceholder(
                    title = stringResource(R.string.coll_empty_state_title),
                    description = stringResource(R.string.coll_empty_state_desc)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(collectionsList) { col ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectCollectionForDetails = col }
                                .testTag("collection_card_${col.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = col.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (col.isPublic) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (col.isPublic) {
                                                if (isArabic) "عامة" else "Public"
                                            } else {
                                                if (isArabic) "خاصة" else "Private"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (col.isPublic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = col.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${if (isArabic) "المؤسس: " else "By: "} ${col.creator}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${col.memberCount} ${if (isArabic) "أعضاء" else "Members"}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    Button(
                                        onClick = {
                                            viewModel.joinOrLeaveCollection(col)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (col.isJoined) MaterialTheme.colorScheme.secondaryContainer
                                            else MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (col.isJoined) {
                                                if (isArabic) "عضو منضم (مغادرة)" else "Joined (Leave)"
                                            } else {
                                                stringResource(R.string.coll_join_btn)
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (col.isJoined) MaterialTheme.colorScheme.onSecondaryContainer
                                            else MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(30.dp)) }
                }
            }
        }
    }

    if (showCreateSheet) {
        var newColName by remember { mutableStateOf("") }
        var newColDesc by remember { mutableStateOf("") }
        var isPublicGroup by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showCreateSheet = false },
            title = {
                Text(
                    text = stringResource(R.string.coll_creation_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newColName,
                        onValueChange = { newColName = it },
                        label = { Text(stringResource(R.string.coll_name_label)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_collection_input_name")
                    )

                    OutlinedTextField(
                        value = newColDesc,
                        onValueChange = { newColDesc = it },
                        label = { Text(stringResource(R.string.coll_desc_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_collection_input_desc")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.coll_public_switch), style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = isPublicGroup,
                            onCheckedChange = { isPublicGroup = it },
                            modifier = Modifier.testTag("collection_public_switch")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newColName.isNotBlank()) {
                            viewModel.createCollection(newColName, newColDesc, isPublicGroup)
                            showCreateSheet = false
                            android.widget.Toast.makeText(context, context.getString(R.string.coll_created_toast), android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("collection_submit_button")
                ) {
                    Text(stringResource(R.string.coll_submit))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateSheet = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
fun NotificationsPane(
    viewModel: WallpaperViewModel,
    isArabic: Boolean
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings by viewModel.notificationSettings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.notif_panel_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.notif_toggle), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                    }
                    Switch(
                        checked = settings.notificationsEnabled,
                        onCheckedChange = {
                            viewModel.updateNotificationSettings(settings.copy(notificationsEnabled = it))
                        },
                        modifier = Modifier.testTag("switch_notif_enabled")
                    )
                }

                if (settings.notificationsEnabled) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    Column {
                        Text(stringResource(R.string.notif_freq_title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("daily" to R.string.notif_freq_daily, "weekly" to R.string.notif_freq_weekly).forEach { (freqKey, strRes) ->
                                val active = settings.frequency == freqKey
                                FilterChip(
                                    selected = active,
                                    onClick = {
                                        viewModel.updateNotificationSettings(settings.copy(frequency = freqKey))
                                    },
                                    label = { Text(stringResource(strRes)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.notif_topics_title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        
                        Row(Modifier.fillMaxWidth().clickable { viewModel.updateNotificationSettings(settings.copy(notifyNature = !settings.notifyNature)) }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.notif_topic_nature), style = MaterialTheme.typography.bodyMedium)
                            Checkbox(checked = settings.notifyNature, onCheckedChange = { viewModel.updateNotificationSettings(settings.copy(notifyNature = it)) })
                        }
                        Row(Modifier.fillMaxWidth().clickable { viewModel.updateNotificationSettings(settings.copy(notifySpace = !settings.notifySpace)) }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.notif_topic_space), style = MaterialTheme.typography.bodyMedium)
                            Checkbox(checked = settings.notifySpace, onCheckedChange = { viewModel.updateNotificationSettings(settings.copy(notifySpace = it)) })
                        }
                        Row(Modifier.fillMaxWidth().clickable { viewModel.updateNotificationSettings(settings.copy(notifyCars = !settings.notifyCars)) }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.notif_topic_cars), style = MaterialTheme.typography.bodyMedium)
                            Checkbox(checked = settings.notifyCars, onCheckedChange = { viewModel.updateNotificationSettings(settings.copy(notifyCars = it)) })
                        }
                        Row(Modifier.fillMaxWidth().clickable { viewModel.updateNotificationSettings(settings.copy(notifyCommunity = !settings.notifyCommunity)) }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.notif_topic_community), style = MaterialTheme.typography.bodyMedium)
                            Checkbox(checked = settings.notifyCommunity, onCheckedChange = { viewModel.updateNotificationSettings(settings.copy(notifyCommunity = it)) })
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (settings.notificationsEnabled) {
                    val sampleTitle = if (isArabic) "مجموعة سيارات خارقة جديدة تتوفر الآن!" else "New Supercars Available!"
                    val sampleBody = if (isArabic) {
                        "سعد السواح أطلق المجموعة المشتركة بـ 25 خلفية بورش جديدة وعالية الدقة والوضوح!"
                    } else {
                        "SariRider just added 25 gorgeous ultra HD 4K Porsche wallpapers in public garage collections."
                    }
                    com.example.util.NotificationHelper.showUpdateNotification(context, sampleTitle, sampleBody)
                } else {
                    android.widget.Toast.makeText(context, if (isArabic) "الإشعارات غير مفعلة!" else "Please enable notification toggles first!", android.widget.Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("button_trigger_mock_notification"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(imageVector = Icons.Filled.NotificationsActive, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isArabic) "إطلاق إشعار ذكي تجريبي" else "Simulate Smart Notification")
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
