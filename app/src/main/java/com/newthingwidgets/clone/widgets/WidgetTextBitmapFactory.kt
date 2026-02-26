package com.newthingwidgets.clone.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.newthingwidgets.clone.R
import kotlin.math.ceil
import kotlin.math.max

object WidgetTextBitmapFactory {

    enum class WidgetFont {
        PRODUCT_MED,
        PROD_SANS_REG,
        NOTHING_5_7
    }

    fun createTextBitmap(
        context: Context,
        text: String,
        textSizeSp: Float,
        textColor: Int = Color.WHITE,
        horizontalPaddingDp: Int = 8,
        verticalPaddingDp: Int = 4
    ): Bitmap {
        return createAdaptiveTextBitmap(
            context = context,
            text = text,
            baseTextSize = textSizeSp,
            textColor = textColor,
            font = WidgetFont.PRODUCT_MED,
            horizontalPaddingDp = horizontalPaddingDp.toFloat(),
            verticalPaddingDp = verticalPaddingDp.toFloat(),
            applyLandscapeAdjustment = false
        )
    }

    fun createAdaptiveTextBitmap(
        context: Context,
        text: String,
        baseTextSize: Float,
        textColor: Int = Color.WHITE,
        font: WidgetFont = WidgetFont.PRODUCT_MED,
        horizontalPaddingDp: Float = 8f,
        verticalPaddingDp: Float = 8f,
        applyLandscapeAdjustment: Boolean = false
    ): Bitmap {
        val content = if (text.isBlank()) " " else text
        val metrics = context.resources.displayMetrics
        val density = metrics.density.coerceAtLeast(1f)
        val densityDpi = metrics.densityDpi.coerceAtLeast(160)
        val screenWidthDp = (metrics.widthPixels / density).toInt().coerceAtLeast(320)
        val screenHeightDp = (metrics.heightPixels / density).toInt().coerceAtLeast(320)

        val densityAdjustment = when {
            densityDpi <= 120 -> 0.75f
            densityDpi <= 160 -> 0.8f
            densityDpi <= 240 -> 0.9f
            densityDpi > 480 -> 1.2f
            else -> 1.1f
        }

        val screenAdjustment = when {
            screenWidthDp <= 320 -> 0.85f
            screenWidthDp <= 480 -> 0.95f
            screenWidthDp <= 720 -> 1f
            else -> 1.1f
        }

        val orientationAdjustment = if (applyLandscapeAdjustment && screenWidthDp > screenHeightDp) {
            0.85f
        } else {
            1f
        }

        val computedPx = (baseTextSize * density * densityAdjustment * screenAdjustment * orientationAdjustment)
            .coerceAtLeast(8f)

        val paint = Paint().apply {
            isAntiAlias = true
            isSubpixelText = true
            isLinearText = true
            color = textColor
            textSize = computedPx
            typeface = loadTypeface(context, font)
        }

        val lines = content.split('\n')
        val fontMetrics = paint.fontMetrics
        val lineHeight = (fontMetrics.descent - fontMetrics.ascent).coerceAtLeast(1f)
        val textWidth = lines.maxOfOrNull { paint.measureText(it) } ?: 1f
        val textHeight = max(1f, lineHeight * lines.size)
        val padH = horizontalPaddingDp * density
        val padV = verticalPaddingDp * density

        val bitmap = Bitmap.createBitmap(
            ceil(textWidth + (padH * 2f)).toInt().coerceAtLeast(1),
            ceil(textHeight + (padV * 2f)).toInt().coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        bitmap.density = densityDpi

        val canvas = Canvas(bitmap)
        var y = padV - fontMetrics.ascent
        val x = padH
        lines.forEach { line ->
            canvas.drawText(line, x, y, paint)
            y += lineHeight
        }

        return bitmap
    }

    private fun loadTypeface(context: Context, font: WidgetFont): Typeface {
        val fontRes = when (font) {
            WidgetFont.PRODUCT_MED -> R.font.product_med
            WidgetFont.PROD_SANS_REG -> R.font.prod_sans_reg
            WidgetFont.NOTHING_5_7 -> R.font.nothing_5_7
        }
        return runCatching { ResourcesCompat.getFont(context, fontRes) }.getOrNull()
            ?: Typeface.DEFAULT_BOLD
    }
}

