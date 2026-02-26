package com.newthingwidgets.clone.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.util.TypedValue
import android.widget.RemoteViews
import java.io.InputStream
import java.util.concurrent.Executors

object ContactPhotoLoader {

    private val executor = Executors.newFixedThreadPool(2)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val cache = object : LruCache<String, Bitmap>(cacheSizeKb()) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    fun loadIntoWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        layoutResId: Int,
        viewId: Int,
        photoUri: String?,
        targetSizeDp: Float,
        applyBottomFade: Boolean = false,
        fallbackDrawableRes: Int? = null
    ) {
        if (photoUri.isNullOrBlank()) {
            return
        }

        val targetSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            targetSizeDp.coerceAtLeast(1f),
            context.resources.displayMetrics
        ).toInt().coerceAtLeast(1)

        val cacheKey = "${photoUri}_${targetSizePx}"
        val cached = cache.get(cacheKey)
        if (cached != null) {
            applyBitmap(appWidgetManager, context, appWidgetId, layoutResId, viewId, cached)
            return
        }

        executor.execute {
            val bitmap = runCatching {
                decodeContactBitmap(context, Uri.parse(photoUri), targetSizePx, targetSizePx)
            }.getOrNull()

            val finalBitmap = bitmap?.let {
                if (applyBottomFade) {
                    applyFadeMask(it)
                } else {
                    it
                }
            }

            if (finalBitmap != null) {
                cache.put(cacheKey, finalBitmap)
                applyBitmap(appWidgetManager, context, appWidgetId, layoutResId, viewId, finalBitmap)
            } else if (fallbackDrawableRes != null) {
                applyFallback(appWidgetManager, context, appWidgetId, layoutResId, viewId, fallbackDrawableRes)
            }
        }
    }

    fun clear() {
        cache.evictAll()
    }

    private fun applyBitmap(
        appWidgetManager: AppWidgetManager,
        context: Context,
        appWidgetId: Int,
        layoutResId: Int,
        viewId: Int,
        bitmap: Bitmap
    ) {
        mainHandler.post {
            val remoteViews = RemoteViews(context.packageName, layoutResId)
            remoteViews.setImageViewBitmap(viewId, bitmap)
            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, remoteViews)
        }
    }

    private fun applyFallback(
        appWidgetManager: AppWidgetManager,
        context: Context,
        appWidgetId: Int,
        layoutResId: Int,
        viewId: Int,
        fallbackDrawableRes: Int
    ) {
        mainHandler.post {
            val remoteViews = RemoteViews(context.packageName, layoutResId)
            remoteViews.setImageViewResource(viewId, fallbackDrawableRes)
            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, remoteViews)
        }
    }

    private fun decodeContactBitmap(
        context: Context,
        uri: Uri,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap? {
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        var stream: InputStream? = null
        try {
            stream = context.contentResolver.openInputStream(uri)
            if (stream == null) {
                return null
            }
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        } finally {
            stream?.close()
        }

        val sampleSize = calculateInSampleSize(
            boundsOptions.outWidth,
            boundsOptions.outHeight,
            targetWidth * 2,
            targetHeight * 2
        )

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inMutable = true
        }

        var decoded: Bitmap? = null
        try {
            stream = context.contentResolver.openInputStream(uri)
            if (stream == null) {
                return null
            }
            decoded = BitmapFactory.decodeStream(stream, null, decodeOptions)
        } finally {
            stream?.close()
        }

        if (decoded == null) {
            return null
        }
        if (decoded.width == targetWidth && decoded.height == targetHeight) {
            return decoded
        }

        val scaled = Bitmap.createScaledBitmap(decoded, targetWidth, targetHeight, true)
        if (scaled != decoded) {
            decoded.recycle()
        }
        return scaled
    }

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        if (width <= 0 || height <= 0 || reqWidth <= 0 || reqHeight <= 0) {
            return 1
        }

        var inSampleSize = 1
        var halfWidth = width / 2
        var halfHeight = height / 2

        while (halfWidth / inSampleSize >= reqWidth && halfHeight / inSampleSize >= reqHeight) {
            inSampleSize *= 2
        }

        if (inSampleSize > 1) {
            inSampleSize /= 2
        }

        return inSampleSize.coerceAtLeast(1)
    }

    private fun applyFadeMask(bitmap: Bitmap): Bitmap {
        val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)
        val gradient = LinearGradient(
            0f,
            0f,
            0f,
            mutable.height.toFloat(),
            0xFF000000.toInt(),
            0x00000000,
            Shader.TileMode.CLAMP
        )
        val paint = Paint().apply {
            shader = gradient
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
        canvas.drawRect(0f, 0f, mutable.width.toFloat(), mutable.height.toFloat(), paint)
        return mutable
    }

    private fun cacheSizeKb(): Int {
        val maxMemoryKb = (Runtime.getRuntime().maxMemory() / 1024L).toInt()
        return (maxMemoryKb / 8).coerceAtLeast(1024)
    }
}