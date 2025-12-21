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
import android.widget.RemoteViews
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import com.newthingwidgets.clone.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dot Matrix Clock Widget Provider
 * Displays:
 * - Day of week (sans-serif, white)
 * - Time in 24-hour format (Nothing font, red)
 * - Date DD MMM YYYY (sans-serif, white)
 */
class DotMatrixClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            Companion.updateWidget(context, appWidgetManager, appWidgetId)
        }
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
                val componentName = ComponentName(context, DotMatrixClockWidgetProvider::class.java)
                val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (widgetId in widgetIds) {
                    Companion.updateWidget(context, appWidgetManager, widgetId)
                }
                scheduleNextUpdate(context)
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
        val intent = Intent(context, DotMatrixClockWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2, // Unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 1)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        alarmManager.set(
            AlarmManager.RTC,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun cancelScheduledUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DotMatrixClockWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        private const val ACTION_UPDATE_WIDGET = "com.newthingwidgets.clone.UPDATE_DOT_MATRIX_CLOCK"
        private const val RED_COLOR = 0xFFE53935.toInt()

        internal fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.dot_matrix_clock_widget)
            
            val now = Calendar.getInstance()
            
            // Day of week (e.g., "SUNDAY")
            val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(now.time).uppercase()
            views.setTextViewText(R.id.day_of_week, dayOfWeek)
            
            // Time in 24-hour format (e.g., "10:51")
            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time)
            val timeBitmap = createTimeBitmap(context, time)
            views.setImageViewBitmap(R.id.time_image, timeBitmap)
            
            // Date (e.g., "21 DEC 2025")
            val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(now.time).uppercase()
            views.setTextViewText(R.id.date_text, date)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun createTimeBitmap(context: Context, time: String): Bitmap {
            val density = context.resources.displayMetrics.density
            val textSize = 72f * density  // Large time
            
            val typeface = try {
                ResourcesCompat.getFont(context, R.font.nothing_5_7)
            } catch (e: Exception) {
                null
            }
            
            val paint = Paint().apply {
                isAntiAlias = true
                this.textSize = textSize
                color = RED_COLOR
                this.typeface = typeface
            }
            
            val bounds = Rect()
            paint.getTextBounds(time, 0, time.length, bounds)
            val textWidth = paint.measureText(time).toInt()
            val textHeight = bounds.height()
            
            val padding = (8 * density).toInt()
            val bitmap = createBitmap(
                (textWidth + padding * 2).coerceAtLeast(1),
                (textHeight + padding * 2).coerceAtLeast(1)
            )
            val canvas = Canvas(bitmap)
            
            canvas.drawText(time, padding.toFloat(), textHeight.toFloat() + padding, paint)
            
            return bitmap
        }
    }
}
