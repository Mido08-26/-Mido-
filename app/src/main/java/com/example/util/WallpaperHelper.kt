package com.example.util

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

object WallpaperHelper {

    /**
     * Download and convert an image URL to bitmap using Coil
     */
    suspend fun fetchBitmap(context: Context, url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false) // Crucial for converting to bitmap
                    .build()
                val result = loader.execute(request)
                if (result is SuccessResult) {
                    (result.drawable as? BitmapDrawable)?.bitmap
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Sets the wallpaper on Android device (Home screen, Lock screen, or Both)
     */
    suspend fun setDeviceWallpaper(
        context: Context,
        bitmap: Bitmap,
        location: WallpaperLocation
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val wm = WallpaperManager.getInstance(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val flag = when (location) {
                        WallpaperLocation.HOME -> WallpaperManager.FLAG_SYSTEM
                        WallpaperLocation.LOCK -> WallpaperManager.FLAG_LOCK
                        WallpaperLocation.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    }
                    wm.setBitmap(bitmap, null, true, flag)
                } else {
                    // Pre-Nougat fallback sets both
                    wm.setBitmap(bitmap)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * Save bitmap to device gallery via MediaStore
     */
    suspend fun saveToGallery(context: Context, bitmap: Bitmap, title: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val filename = "WallMax_${title.replace(" ", "_")}_${System.currentTimeMillis()}.jpg"
                val resolver = context.contentResolver

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/WallMax")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }

                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                if (imageUri != null) {
                    val out: OutputStream? = resolver.openOutputStream(imageUri)
                    if (out != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        out.close()
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        resolver.update(imageUri, contentValues, null, null)
                    }
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * Applies rotation, aspect-ratio cropping, and pre-selected color filters directly to a raw Bitmap
     */
    fun editBitmap(
        original: Bitmap,
        rotationDegrees: Float,
        cropRatioName: String, // "1:1", "9:16", "3:4", etc.
        filterName: String // "grayscale", "contrast", "warm", "cool", "noir", "original"
    ): Bitmap {
        var bitmap = original

        // 1. Rotate if needed
        if (rotationDegrees % 360f != 0f) {
            val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees) }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        // 2. Crop if requested
        val width = bitmap.width
        val height = bitmap.height
        if (cropRatioName != "Free" && cropRatioName != "حر" && cropRatioName != "Original" && cropRatioName != "الأصلية") {
            val ratioValue = when (cropRatioName) {
                "1:1" -> 1.0f
                "9:16" -> 9.0f / 16.0f
                "3:4" -> 3.0f / 4.0f
                "4:5" -> 4.0f / 5.0f
                else -> null
            }

            if (ratioValue != null) {
                var targetWidth = width
                var targetHeight = height
                val currentRatio = width.toFloat() / height.toFloat()

                if (currentRatio > ratioValue) {
                    targetWidth = (height * ratioValue).toInt()
                } else {
                    targetHeight = (width / ratioValue).toInt()
                }

                val startX = (width - targetWidth) / 2
                val startY = (height - targetHeight) / 2
                
                val safeX = startX.coerceAtLeast(0)
                val safeY = startY.coerceAtLeast(0)
                val safeW = targetWidth.coerceAtMost(width - safeX)
                val safeH = targetHeight.coerceAtMost(height - safeY)

                bitmap = Bitmap.createBitmap(bitmap, safeX, safeY, safeW, safeH)
            }
        }

        // 3. Apply color filters
        if (filterName != "original" && filterName != "الأصلية") {
            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(output)
            val paint = android.graphics.Paint()

            val cm = android.graphics.ColorMatrix()
            when (filterName) {
                "grayscale", "أبيض وأسود" -> {
                    cm.setSaturation(0f)
                }
                "contrast", "تباين عالي" -> {
                    val contrast = 1.4f
                    val translate = (-0.5f * contrast + 0.5f) * 255f
                    cm.set(floatArrayOf(
                        contrast, 0f, 0f, 0f, translate,
                        0f, contrast, 0f, 0f, translate,
                        0f, 0f, contrast, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    ))
                }
                "warm", "دافئ" -> {
                    cm.set(floatArrayOf(
                        1.2f, 0f, 0f, 0f, 10f,
                        0f, 1.1f, 0f, 0f, 5f,
                        0f, 0f, 0.8f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    ))
                }
                "cool", "بارد" -> {
                    cm.set(floatArrayOf(
                        0.8f, 0f, 0f, 0f, -10f,
                        0f, 1.1f, 0f, 0f, 5f,
                        0f, 0f, 1.3f, 0f, 15f,
                        0f, 0f, 0f, 1f, 0f
                    ))
                }
                "vintage", "سينمائي" -> {
                    cm.set(floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 0f,
                        0.349f, 0.686f, 0.168f, 0f, 0f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    ))
                }
            }
            paint.colorFilter = android.graphics.ColorMatrixColorFilter(cm)
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            bitmap = output
        }

        return bitmap
    }

    enum class WallpaperLocation {
        HOME, LOCK, BOTH
    }
}
