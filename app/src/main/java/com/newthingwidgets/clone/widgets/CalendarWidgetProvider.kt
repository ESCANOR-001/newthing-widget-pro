package com.newthingwidgets.clone.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.widget.RemoteViews
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import com.newthingwidgets.clone.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Calendar Widget Provider
 * Displays:
 * - Month name (Nothing font)
 * - Current day number (red)
 * - Full month calendar grid (max 31 days)
 * - Current day highlighted
 */
class CalendarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, CalendarWidgetProvider::class.java)
                val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (widgetId in widgetIds) {
                    updateWidget(context, appWidgetManager, widgetId)
                }
            }
        }
    }

    companion object {
        private const val WHITE_COLOR = 0xFFFFFFFF.toInt()

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.calendar_widget)
            
            val now = Calendar.getInstance()
            val currentDayOfMonth = now.get(Calendar.DAY_OF_MONTH)
            
            // Get month name
            val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(now.time)
            
            // Create month bitmap with Nothing font
            val monthBitmap = createMonthBitmap(context, monthName)
            views.setImageViewBitmap(R.id.month_image, monthBitmap)
            
            // Set current day number (red)
            views.setTextViewText(R.id.day_number, currentDayOfMonth.toString())
            
            // Get days in current month
            val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
            
            // Day cell IDs (day_1 through day_31)
            val dayIds = listOf(
                R.id.day_1, R.id.day_2, R.id.day_3, R.id.day_4, R.id.day_5, R.id.day_6, R.id.day_7,
                R.id.day_8, R.id.day_9, R.id.day_10, R.id.day_11, R.id.day_12, R.id.day_13, R.id.day_14,
                R.id.day_15, R.id.day_16, R.id.day_17, R.id.day_18, R.id.day_19, R.id.day_20, R.id.day_21,
                R.id.day_22, R.id.day_23, R.id.day_24, R.id.day_25, R.id.day_26, R.id.day_27, R.id.day_28,
                R.id.day_29, R.id.day_30, R.id.day_31
            )
            
            for (i in 0 until 31) {
                val dayNum = i + 1
                
                if (dayNum <= daysInMonth) {
                    views.setTextViewText(dayIds[i], dayNum.toString())
                    
                    if (dayNum == currentDayOfMonth) {
                        // Highlight current day
                        views.setInt(dayIds[i], "setBackgroundResource", R.drawable.current_day_bg)
                        views.setTextColor(dayIds[i], 0xFF1A1A1A.toInt())
                    } else {
                        views.setInt(dayIds[i], "setBackgroundResource", 0)
                        views.setTextColor(dayIds[i], WHITE_COLOR)
                    }
                } else {
                    // Hide days beyond month (e.g., 29, 30, 31 for February)
                    views.setTextViewText(dayIds[i], "")
                    views.setInt(dayIds[i], "setBackgroundResource", 0)
                }
            }
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun createMonthBitmap(context: Context, month: String): Bitmap {
            val density = context.resources.displayMetrics.density
            val textSize = 36f * density
            
            val typeface = try {
                ResourcesCompat.getFont(context, R.font.nothing_5_7)
            } catch (e: Exception) {
                null
            }
            
            val paint = Paint().apply {
                isAntiAlias = true
                this.textSize = textSize
                color = WHITE_COLOR
                this.typeface = typeface
            }
            
            val bounds = Rect()
            paint.getTextBounds(month, 0, month.length, bounds)
            val textWidth = paint.measureText(month).toInt()
            val textHeight = bounds.height()
            
            val padding = (4 * density).toInt()
            val bitmap = createBitmap(
                (textWidth + padding * 2).coerceAtLeast(1),
                (textHeight + padding * 2).coerceAtLeast(1)
            )
            val canvas = Canvas(bitmap)
            
            canvas.drawText(month, padding.toFloat(), textHeight.toFloat() + padding, paint)
            
            return bitmap
        }
    }
}
