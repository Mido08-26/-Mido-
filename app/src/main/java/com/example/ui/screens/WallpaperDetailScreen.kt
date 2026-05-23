package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.Wallpaper
import com.example.util.WallpaperHelper
import kotlinx.coroutines.launch

@Composable
fun WallpaperDetailScreen(
    wallpaper: Wallpaper,
    isArabic: Boolean,
    collections: List<com.example.data.local.CollectionEntity>,
    onPinToCollection: (collectionId: String, wallpaperId: String) -> Unit,
    onBack: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Screen States
    var showApplyDialog by remember { mutableStateOf(false) }
    var isSavingToGallery by remember { mutableStateOf(false) }
    var isApplyingWallpaper by remember { mutableStateOf(false) }
    var showHudSimulation by remember { mutableStateOf(false) }

    // local editing studio states
    var isEditMode by remember { mutableStateOf(false) }
    var editRotation by remember { mutableStateOf(0f) }
    var editCropRatio by remember { mutableStateOf("Original") }
    var editFilter by remember { mutableStateOf("original") }
    var showPinDialog by remember { mutableStateOf(false) }

    val composeColorFilter = remember(editFilter) {
        when (editFilter) {
            "grayscale", "أبيض وأسود" -> {
                ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
            }
            "contrast", "تباين عالي" -> {
                val contrast = 1.3f
                ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                    contrast, 0f, 0f, 0f, 0f,
                    0f, contrast, 0f, 0f, 0f,
                    0f, 0f, contrast, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )))
            }
            "warm", "دافئ" -> {
                ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                    1.2f, 0f, 0f, 0f, 0.05f,
                    0f, 1.1f, 0f, 0f, 0.02f,
                    0f, 0f, 0.8f, 0f, -0.05f,
                    0f, 0f, 0f, 1f, 0f
                )))
            }
            "cool", "بارد" -> {
                ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                    0.8f, 0f, 0f, 0f, -0.05f,
                    0f, 1.1f, 0f, 0f, 0.02f,
                    0f, 0f, 1.3f, 0f, 0.05f,
                    0f, 0f, 0f, 1f, 0f
                )))
            }
            "vintage", "سينمائي" -> {
                ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )))
            }
            else -> null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Fullscreen dynamic background image
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(wallpaper.url)
                .crossfade(true)
                .build(),
            contentDescription = if (isArabic) wallpaper.titleAr else wallpaper.title,
            contentScale = ContentScale.Crop,
            colorFilter = composeColorFilter,
            modifier = Modifier
                .fillMaxSize()
                .rotate(editRotation)
        )

        // Visual Crop Boundary Overlay Guide
        if (isEditMode && editCropRatio != "Original") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                val cropAspect = when (editCropRatio) {
                    "1:1" -> 1.0f
                    "9:16" -> 9f/16f
                    "3:4" -> 3f/4f
                    else -> 1.0f
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .aspectRatio(cropAspect)
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .background(Color.Transparent)
                )
            }
        }

        // Dark ambient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        // Lockscreen / Homescreen overlay simulation
        AnimatedVisibility(
            visible = showHudSimulation,
            enter = fadeIn() + expandIn(),
            exit = fadeOut() + shrinkOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                // Mock Time and Date Hud
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "12:30",
                        color = Color.White,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 76.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp
                        )
                    )
                    Text(
                        text = if (isArabic) "السبت، ٢٣ مايو" else "Saturday, May 23",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal)
                    )
                }

                // Mock App Icons at the bottom to give homescreen perspective
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 150.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    repeat(4) { idx ->
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (idx) {
                                    0 -> Icons.Filled.Call
                                    1 -> Icons.Filled.Mail
                                    2 -> Icons.Filled.Search
                                    else -> Icons.Filled.Settings
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Custom Navigation Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant Frosted Back button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                    .clickable { onBack() }
                    .testTag("detail_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Title Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isArabic) wallpaper.titleAr else wallpaper.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pin/Link to Custom Room Collection Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .clickable { showPinDialog = true }
                        .testTag("detail_pin_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlaylistAdd,
                        contentDescription = "Pin to Collection",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Clean Favorite Heart
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .clickable { onFavoriteToggle() }
                        .testTag("detail_fav_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (wallpaper.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (wallpaper.isFavorite) Color(0xFFE91E63) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Bottom Actions Shelf & Interactive Creative Studio Drawer
        if (isEditMode) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.88f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Filled.AutoFixHigh, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Text(stringResource(R.string.edit_editor_title), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        IconButton(onClick = { isEditMode = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    // 1. ROTATE OPTION
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.edit_rotate), color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                        Button(
                            onClick = { editRotation = (editRotation + 90f) % 360f },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Filled.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "تدوير" else "Rotate", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // 2. CROP PRESETS
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(R.string.edit_crop), color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val ratios = listOf("Original", "1:1", "9:16", "3:4")
                            items(ratios) { ratio ->
                                val active = editCropRatio == ratio
                                FilterChip(
                                    selected = active,
                                    onClick = { editCropRatio = ratio },
                                    label = { Text(ratio) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White.copy(alpha = 0.12f),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // 3. ATMOSPHERE FILTERS
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(R.string.edit_filters), color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val filters = listOf(
                                "original" to R.string.filter_original,
                                "grayscale" to R.string.filter_grayscale,
                                "contrast" to R.string.filter_high_contrast,
                                "warm" to R.string.filter_warm,
                                "cool" to R.string.filter_cool,
                                "vintage" to R.string.filter_vintage
                            )
                            items(filters) { (filterKey, strRes) ->
                                val active = editFilter == filterKey
                                FilterChip(
                                    selected = active,
                                    onClick = { editFilter = filterKey },
                                    label = { Text(stringResource(strRes)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White.copy(alpha = 0.12f),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // 4. SET / SAVE DESIGN ACTION
                    Button(
                        onClick = {
                            isEditMode = false
                            showApplyDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.edit_apply_changes), fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "by ${wallpaper.photographer}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isArabic) "الفئة: ${wallpaper.categoryAr}" else "Category: ${wallpaper.category}",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Advanced Edit Studio Button
                        Button(
                            onClick = { isEditMode = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("edit_mode_toggle")
                        ) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit Studio", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = stringResource(R.string.edit_wallpaper_action), color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }

                        // Preview overlay button
                        Button(
                            onClick = { showHudSimulation = !showHudSimulation },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showHudSimulation) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.45f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("hud_toggle")
                        ) {
                            val eyeIcon = if (showHudSimulation) Icons.Filled.Visibility else Icons.Outlined.Visibility
                            Icon(
                                imageVector = eyeIcon,
                                contentDescription = "Preview Overlay",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "معاينة" else "Preview",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                // Primary Set as Wallpaper
                Button(
                    onClick = { showApplyDialog = true },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(54.dp)
                        .testTag("apply_wallpaper_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Wallpaper,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.set_as_wallpaper),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }

                // Secondary Download to Device with Active In-Studio Filters Applied
                Button(
                    onClick = {
                        scope.launch {
                            isSavingToGallery = true
                            var bitmap = WallpaperHelper.fetchBitmap(context, wallpaper.url)
                            if (bitmap != null) {
                                if (editRotation != 0f || editCropRatio != "Original" || editFilter != "original") {
                                    bitmap = WallpaperHelper.editBitmap(bitmap, editRotation, editCropRatio, editFilter)
                                }
                                val success = WallpaperHelper.saveToGallery(context, bitmap, wallpaper.title)
                                if (success) {
                                    Toast.makeText(context, context.getString(R.string.wallpaper_saved_success), Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, context.getString(R.string.wallpaper_save_failed), Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, context.getString(R.string.wallpaper_save_failed), Toast.LENGTH_LONG).show()
                            }
                            isSavingToGallery = false
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("download_wallpaper_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSavingToGallery
                ) {
                    if (isSavingToGallery) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.download_to_device),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

        // Processing Overlay HUD
        if (isApplyingWallpaper) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = stringResource(R.string.wallpaper_apply_started),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        // Apply To Screen Picker dialog (Home, Lock, Both)
        if (showApplyDialog) {
            Dialog(onDismissRequest = { showApplyDialog = false }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.dialog_title_set_wallpaper),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Set option: Home Screen
                        ApplyOptionItem(
                            title = stringResource(R.string.option_home_screen),
                            icon = Icons.Filled.Home,
                            onClick = {
                                showApplyDialog = false
                                applyToDevice(context, scope, wallpaper.url, WallpaperHelper.WallpaperLocation.HOME, editRotation, editCropRatio, editFilter) { state ->
                                    isApplyingWallpaper = state
                                }
                            }
                        )

                        // Set option: Lock Screen
                        ApplyOptionItem(
                            title = stringResource(R.string.option_lock_screen),
                            icon = Icons.Filled.Lock,
                            onClick = {
                                showApplyDialog = false
                                applyToDevice(context, scope, wallpaper.url, WallpaperHelper.WallpaperLocation.LOCK, editRotation, editCropRatio, editFilter) { state ->
                                    isApplyingWallpaper = state
                                }
                            }
                        )

                        // Set option: Both Screens
                        ApplyOptionItem(
                            title = stringResource(R.string.option_both_screens),
                            icon = Icons.Filled.AllInclusive,
                            onClick = {
                                showApplyDialog = false
                                applyToDevice(context, scope, wallpaper.url, WallpaperHelper.WallpaperLocation.BOTH, editRotation, editCropRatio, editFilter) { state ->
                                    isApplyingWallpaper = state
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Cancel Button
                        TextButton(
                            onClick = { showApplyDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.dialog_cancel),
                                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }

        // Shared Room Pinning Selection Dialog
        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                title = { Text(stringResource(R.string.select_coll_dialog), fontWeight = FontWeight.Bold) },
                text = {
                    val userJoinedCollections = collections.filter { it.isJoined }
                    if (userJoinedCollections.isEmpty()) {
                        Text(if (isArabic) "يرجى الانضمام إلى مجموعة أو إنشاء واحدة أولاً لتتمكن من إضافة الصور!" else "Please join or construct a collection first to pin wallpapers!")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                        ) {
                            items(userJoinedCollections) { col ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onPinToCollection(col.id, wallpaper.id)
                                            showPinDialog = false
                                            Toast.makeText(context, context.getString(R.string.pinned_success_toast), Toast.LENGTH_SHORT).show()
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(col.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showPinDialog = false }) {
                        Text(stringResource(R.string.dialog_cancel))
                    }
                }
            )
        }
    }

@Composable
private fun ApplyOptionItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )
        }
    }
}

private fun applyToDevice(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    url: String,
    location: WallpaperHelper.WallpaperLocation,
    rotation: Float,
    cropRatio: String,
    filter: String,
    loadingState: (Boolean) -> Unit
) {
    scope.launch {
        loadingState(true)
        var bitmap = WallpaperHelper.fetchBitmap(context, url)
        if (bitmap != null) {
            if (rotation != 0f || cropRatio != "Original" || filter != "original") {
                bitmap = WallpaperHelper.editBitmap(bitmap, rotation, cropRatio, filter)
            }
            val success = WallpaperHelper.setDeviceWallpaper(context, bitmap, location)
            if (success) {
                Toast.makeText(context, context.getString(R.string.wallpaper_apply_success), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, context.getString(R.string.wallpaper_apply_failed), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, context.getString(R.string.wallpaper_apply_failed), Toast.LENGTH_SHORT).show()
        }
        loadingState(false)
    }
}
