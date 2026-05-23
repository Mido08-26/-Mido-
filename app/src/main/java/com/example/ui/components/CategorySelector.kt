package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategorySelector(
    selectedCategory: String,
    isArabic: Boolean,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Standard localized list of categories
    val categories = if (isArabic) {
        listOf("الكل", "طبيعة", "بساطة", "أنمي", "تجريدي", "الفضاء", "سيارات", "الوضع الداكن", "عمارة")
    } else {
        listOf("All", "Nature", "Minimal", "Anime", "Abstract", "Space", "Cars", "Dark", "Architecture")
    }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("category_selector_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory || 
                (category == "All" && selectedCategory == "الكل") ||
                (category == "الكل" && selectedCategory == "All") ||
                (category == "Nature" && selectedCategory == "طبيعة") ||
                (category == "طبيعة" && selectedCategory == "Nature") ||
                (category == "Minimal" && selectedCategory == "بساطة") ||
                (category == "بساطة" && selectedCategory == "Minimal") ||
                (category == "Anime" && selectedCategory == "أنمي") ||
                (category == "أنمي" && selectedCategory == "Anime") ||
                (category == "Abstract" && selectedCategory == "تجريدي") ||
                (category == "تجريدي" && selectedCategory == "Abstract") ||
                (category == "Space" && selectedCategory == "الفضاء") ||
                (category == "الفضاء" && selectedCategory == "Space") ||
                (category == "Cars" && selectedCategory == "سيارات") ||
                (category == "سيارات" && selectedCategory == "Cars") ||
                (category == "Dark" && selectedCategory == "الوضع الداكن") ||
                (category == "الوضع الداكن" && selectedCategory == "Dark") ||
                (category == "Architecture" && selectedCategory == "عمارة") ||
                (category == "عمارة" && selectedCategory == "Architecture")

            val containerColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                label = "cat_bg"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                label = "cat_text"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(containerColor)
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("category_pill_$category"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category,
                    color = textColor,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                    maxLines = 1
                )
            }
        }
    }
}
