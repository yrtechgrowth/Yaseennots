package com.example.ui.screens.files

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * High-performance, memory-efficient thumbnail cache for Yaseen Files.
 * Prevents decoding massive 20-50MB photos and video frames on the main UI thread.
 */
object ThumbnailCache {

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    // Use 1/8th of available memory for thumbnail bitmap cache
    private val cacheSize = maxMemory / 8

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    fun getFromCache(path: String): Bitmap? {
        return memoryCache.get(path)
    }

    fun putInCache(path: String, bitmap: Bitmap) {
        if (memoryCache.get(path) == null) {
            memoryCache.put(path, bitmap)
        }
    }

    fun clearCache() {
        memoryCache.evictAll()
    }

    /**
     * Efficiently loads and caches a downsampled bitmap for an image or video file.
     */
    suspend fun getOrLoadThumbnail(
        file: File,
        targetWidth: Int = 160,
        targetHeight: Int = 160,
        isVideo: Boolean = false
    ): Bitmap? = withContext(Dispatchers.IO) {
        val path = file.absolutePath
        val cached = memoryCache.get(path)
        if (cached != null && !cached.isRecycled) {
            return@withContext cached
        }

        if (!file.exists() || !file.canRead()) return@withContext null

        try {
            val bitmap = if (isVideo) {
                loadVideoThumbnail(file, targetWidth, targetHeight)
            } else {
                loadImageThumbnail(file, targetWidth, targetHeight)
            }

            if (bitmap != null) {
                memoryCache.put(path, bitmap)
            }
            bitmap
        } catch (_: Exception) {
            null
        }
    }

    private fun loadImageThumbnail(file: File, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565 // Half memory footprint compared to ARGB_8888

            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (_: OutOfMemoryError) {
            memoryCache.evictAll()
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun loadVideoThumbnail(file: File, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ThumbnailUtils.createVideoThumbnail(
                    file,
                    android.util.Size(reqWidth, reqHeight),
                    null
                )
            } else {
                @Suppress("DEPRECATION")
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(file.absolutePath)
                val frame = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                retriever.release()
                frame
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }
}
