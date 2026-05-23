package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Wallpaper

@Composable
fun WallpaperCard(
    wallpaper: Wallpaper,
    isArabic: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.6f) // Modern vertic-grid ratio
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("wallpaper_card_${wallpaper.id}"),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Asynchronous loaded image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(wallpaper.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = if (isArabic) wallpaper.titleAr else wallpaper.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic bottom dark gradient to ensure text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            // Text info layout
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = if (isArabic) wallpaper.titleAr else wallpaper.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "by ${wallpaper.photographer}",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    maxLines = 1
                )
            }

            // Floating quick-favorite toggle
            val favIconColor by animateColorAsState(
                targetValue = if (wallpaper.isFavorite) Color(0xFFE91E63) else Color.White,
                animationSpec = spring(),
                label = "fav_color"
            )

            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                    .testTag("favorite_button_${wallpaper.id}")
            ) {
                Icon(
                    imageVector = if (wallpaper.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = favIconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
