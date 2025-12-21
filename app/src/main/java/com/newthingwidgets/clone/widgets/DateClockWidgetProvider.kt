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
import android.provider.AlarmClock
import android.widget.RemoteViews
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import com.newthingwidgets.clone.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Date Clock Widget Provider
 * Displays:
 * - AM/PM pill (top-left)
 * - Large time in background (Nothing font, red with reduced opacity)
 * - Day of week (sans-serif, white)
 * - Date (sans-serif, white, caps)
 */
class DateClockWidgetProvider : AppWidgetProvider() {

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
                val componentName = ComponentName(context, DateClockWidgetProvider::class.java)
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
        val intent = Intent(context, DateClockWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
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
        val intent = Intent(context, DateClockWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        private const val ACTION_UPDATE_WIDGET = "com.newthingwidgets.clone.UPDATE_DATE_CLOCK"
        
        // Red color with reduced opacity (around 40% opacity for background effect)
        private const val TIME_COLOR = 0x66E53935.toInt()

        internal fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.date_clock_widget)
            
            val now = Calendar.getInstance()
            
            // Get formatted strings
            val amPm = SimpleDateFormat("a", Locale.getDefault()).format(now.time).uppercase()
            val time = SimpleDateFormat("h:mm", Locale.getDefault()).format(now.time)
            val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(now.time)
            val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(now.time).uppercase()
            val dayOfMonth = now.get(Calendar.DAY_OF_MONTH)
            val dateText = "$month $dayOfMonth"
            
            // Set AM/PM pill
            views.setTextViewText(R.id.ampm_pill, amPm)
            
            // Create time bitmap with Nothing font and reduced opacity red
            val timeBitmap = createTimeBitmap(context, time)
            views.setImageViewBitmap(R.id.time_image, timeBitmap)
            
            // Set day and date text
            views.setTextViewText(R.id.day_text, dayOfWeek)
            views.setTextViewText(R.id.date_text, dateText)
            
            // Set click to open clock app
            val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                clockIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.clock_card, pendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun createTimeBitmap(context: Context, time: String): Bitmap {
            val density = context.resources.displayMetrics.density
            val textSize = 80f * density
            
            val typeface = try {
                ResourcesCompat.getFont(context, R.font.nothing_5_7)
            } catch (e: Exception) {
                null
            }
            
            val paint = Paint().apply {
                isAntiAlias = true
                this.textSize = textSize
                color = TIME_COLOR
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
