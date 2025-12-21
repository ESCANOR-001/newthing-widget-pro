package com.newthingwidgets.clone.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.provider.CalendarContract
import android.widget.RemoteViews
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import com.newthingwidgets.clone.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Date Time Matrix Widget Provider
 * Displays:
 * - Day of week (e.g., "Wed") - white
 * - Time (e.g., "12:12am") - red
 * - Month (e.g., "February") - white
 * - Date with suffix (e.g., "19th") - white
 * All rendered with Nothing 5/7 font
 */
class DateTimeMatrixWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            Companion.updateWidget(context, appWidgetManager, appWidgetId)
        }
        // Schedule next update
        scheduleNextUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            Intent.ACTION_TIME_TICK,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            ACTION_UPDATE_WIDGET -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, DateTimeMatrixWidgetProvider::class.java)
                val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (widgetId in widgetIds) {
                    updateWidget(context, appWidgetManager, widgetId)
                }
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleNextUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelScheduledUpdate(context)
    }

    private fun scheduleNextUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DateTimeMatrixWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Schedule update at the start of the next minute (inexact to avoid permission)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 1)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // Use set instead of setExact to avoid SCHEDULE_EXACT_ALARM permission
        alarmManager.set(
            AlarmManager.RTC,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun cancelScheduledUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DateTimeMatrixWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        private const val ACTION_UPDATE_WIDGET = "com.newthingwidgets.clone.UPDATE_DATETIME_MATRIX"
        
        // Colors
        private const val WHITE_COLOR = 0xFFFFFFFF.toInt()
        private const val RED_COLOR = 0xFFE53935.toInt()

        internal fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.date_time_matrix_widget)
            
            val now = Calendar.getInstance()
            
            // Get formatted strings
            val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(now.time)
            val time = SimpleDateFormat("h:mma", Locale.getDefault()).format(now.time).lowercase()
            val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(now.time)
            val dayOfMonth = now.get(Calendar.DAY_OF_MONTH)
            val dateWithSuffix = "$dayOfMonth${getDaySuffix(dayOfMonth)}"
            
            // Create bitmaps for each text element
            val dayBitmap = createTextBitmap(context, dayOfWeek, 32f, WHITE_COLOR)
            val timeBitmap = createTextBitmap(context, time, 50f, RED_COLOR)
            val monthBitmap = createTextBitmap(context, month, 32f, WHITE_COLOR)
            val dateBitmap = createTextBitmap(context, dateWithSuffix, 32f, WHITE_COLOR)
            
            // Set bitmaps to ImageViews
            views.setImageViewBitmap(R.id.day_of_week_image, dayBitmap)
            views.setImageViewBitmap(R.id.time_image, timeBitmap)
            views.setImageViewBitmap(R.id.month_image, monthBitmap)
            views.setImageViewBitmap(R.id.date_image, dateBitmap)
            
            // Set click to open calendar
            val calendarIntent = Intent(Intent.ACTION_VIEW).apply {
                data = CalendarContract.CONTENT_URI
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                calendarIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.datetime_card, pendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * Create text bitmap with Nothing font
         */
        private fun createTextBitmap(
            context: Context,
            text: String,
            textSize: Float,
            textColor: Int
        ): Bitmap {
            val density = context.resources.displayMetrics.density
            val scaledTextSize = textSize * density
            
            // Load custom font
            val typeface = try {
                ResourcesCompat.getFont(context, R.font.nothing_5_7)
            } catch (e: Exception) {
                null
            }
            
            val paint = Paint().apply {
                isAntiAlias = true
                this.textSize = scaledTextSize
                color = textColor
                this.typeface = typeface
            }
            
            // Measure text
            val bounds = Rect()
            paint.getTextBounds(text, 0, text.length, bounds)
            val textWidth = paint.measureText(text).toInt()
            val textHeight = bounds.height()
            
            // Create bitmap with padding
            val padding = (4 * density).toInt()
            val bitmap = createBitmap(textWidth + padding * 2, textHeight + padding * 2)
            val canvas = Canvas(bitmap)
            
            // Draw text
            canvas.drawText(text, padding.toFloat(), textHeight.toFloat() + padding, paint)
            
            return bitmap
        }

        /**
         * Get ordinal suffix for day of month
         */
        private fun getDaySuffix(day: Int): String {
            return when {
                day in 11..13 -> "th"
                day % 10 == 1 -> "st"
                day % 10 == 2 -> "nd"
                day % 10 == 3 -> "rd"
                else -> "th"
            }
        }
    }
}
