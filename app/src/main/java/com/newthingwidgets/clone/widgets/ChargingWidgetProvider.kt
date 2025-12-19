package com.newthingwidgets.clone.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.widget.RemoteViews
import com.newthingwidgets.clone.R
import java.util.concurrent.TimeUnit
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt

/**
 * Charging Widget Provider
 * Displays real-time battery status with:
 * - Charging/Discharging indicator
 * - Battery percentage
 * - Time remaining (charging: time to full, discharging: estimated time left)
 * - Visual progress bar
 */
class ChargingWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        // Progress bar dimensions
        private const val PROGRESS_BAR_WIDTH = 800
        private const val PROGRESS_BAR_HEIGHT = 120
        private const val CORNER_RADIUS = 40f
        private const val PADDING = 12
        
        /**
         * Update widget with current battery information
         */
        internal fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.charging_widget)
            
            // Get battery status
            val batteryStatus = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            
            val batteryPct = if (level >= 0 && scale > 0) {
                (level * 100) / scale
            } else {
                0
            }
            
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            
            // Update charging status text, icon visibility, and colors
            if (isCharging) {
                views.setTextViewText(R.id.charging_status, "Charging...")
                views.setViewVisibility(R.id.charging_icon, android.view.View.VISIBLE)
                // Set icon to flash/yellow color and text to white when charging
                views.setInt(R.id.charging_icon, "setColorFilter", android.graphics.Color.parseColor("#FFD700")) // Gold/Flash color
                views.setTextColor(R.id.charging_status, android.graphics.Color.WHITE)
            } else {
                views.setTextViewText(R.id.charging_status, "Battery")
                views.setViewVisibility(R.id.charging_icon, android.view.View.GONE)
                // Set text to gray when not charging
                views.setTextColor(R.id.charging_status, "#808080".toColorInt())
            }
            
            // Calculate time remaining
            val timeText = getTimeRemainingText(context, isCharging, batteryPct)
            
            // Update battery info text as bitmap with custom font
            val batteryInfoText = if (timeText.isNotEmpty()) {
                "$batteryPct% • $timeText"
            } else {
                "$batteryPct%"
            }
            val batteryInfoBitmap = createTextBitmap(context, batteryInfoText, 64f, android.graphics.Color.WHITE)
            views.setImageViewBitmap(R.id.battery_info_image, batteryInfoBitmap)
            
            // Render dynamic progress bar - using native ProgressBar for proper resizing
            views.setProgressBar(R.id.progress_bar_view, 100, batteryPct, false)
            
            // Set click to open battery settings
            val batteryIntent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                batteryIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        
        /**
         * Create a bitmap with text rendered using the custom font
         */
        private fun createTextBitmap(context: Context, text: String, textSize: Float, textColor: Int): android.graphics.Bitmap {
            // Load custom font
            val typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.nothing_5_7)
            
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                this.textSize = textSize
                color = textColor
                this.typeface = typeface
            }
            
            // Measure text dimensions
            val textBounds = android.graphics.Rect()
            paint.getTextBounds(text, 0, text.length, textBounds)
            val textWidth = paint.measureText(text).toInt()
            val textHeight = textBounds.height()
            
            // Create bitmap with padding
            val paddingH = 16
            val paddingV = 12
            val bitmap = createBitmap(textWidth + paddingH * 2, textHeight + paddingV * 2)
            val canvas = android.graphics.Canvas(bitmap)
            
            // Draw text centered
            canvas.drawText(text, paddingH.toFloat(), textHeight.toFloat() + paddingV, paint)
            
            return bitmap
        }
        
        /**
         * Create a bitmap for scale labels (0, 50, 100) with custom font
         */
        private fun createScaleLabelsBitmap(context: Context): android.graphics.Bitmap {
            val width = PROGRESS_BAR_WIDTH
            val height = 50
            
            val bitmap = createBitmap(width, height)
            val canvas = android.graphics.Canvas(bitmap)
            
            // Load custom font
            val typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.nothing_5_7)
            
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 40f
                color = "#FFFFFF".toColorInt()
                this.typeface = typeface
            }
            
            // Draw "0" aligned left
            paint.textAlign = android.graphics.Paint.Align.LEFT
            canvas.drawText("0%", 8f, height - 8f, paint)
            
            // Draw "50" centered
            paint.textAlign = android.graphics.Paint.Align.CENTER
            canvas.drawText("50%", width / 2f, height - 8f, paint)
            
            // Draw "100" aligned right
            paint.textAlign = android.graphics.Paint.Align.RIGHT
            canvas.drawText("100%", width - 8f, height - 8f, paint)
            
            return bitmap
        }
        
        /**
         * Create a bitmap for the progress bar with dynamic fill
         */
        private fun createProgressBarBitmap(context: Context, batteryPct: Int): android.graphics.Bitmap {
            val bitmap = createBitmap(PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT)
            val canvas = android.graphics.Canvas(bitmap)
            
            // Get red color from resources
            val redColor = androidx.core.content.ContextCompat.getColor(context, R.color.red_color)
            val trackColor = "#333333".toColorInt()
            
            // Draw track (background)
            val trackPaint = android.graphics.Paint().apply {
            color = trackColor
                isAntiAlias = true
            }
            val trackRect = android.graphics.RectF(
                0f, 0f, 
                PROGRESS_BAR_WIDTH.toFloat(), 
                PROGRESS_BAR_HEIGHT.toFloat()
            )
            canvas.drawRoundRect(trackRect, CORNER_RADIUS, CORNER_RADIUS, trackPaint)
            
            // Draw progress fill
            if (batteryPct > 0) {
                val fillWidth = ((PROGRESS_BAR_WIDTH - PADDING * 2) * batteryPct / 100f).coerceAtLeast(CORNER_RADIUS)
                val fillPaint = android.graphics.Paint().apply {
                    color = redColor
                    isAntiAlias = true
                }
                val fillRect = android.graphics.RectF(
                    PADDING.toFloat(),
                    PADDING.toFloat(),
                    PADDING + fillWidth,
                    (PROGRESS_BAR_HEIGHT - PADDING).toFloat()
                )
                canvas.drawRoundRect(fillRect, CORNER_RADIUS - 8, CORNER_RADIUS - 8, fillPaint)
            }
            
            return bitmap
        }
        
        /**
         * Get time remaining text
         */
        private fun getTimeRemainingText(
            context: Context,
            isCharging: Boolean,
            batteryPct: Int
        ): String {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                
                if (isCharging) {
                    // Time until full charge
                    val chargeTimeRemaining = batteryManager.computeChargeTimeRemaining()
                    if (chargeTimeRemaining > 0) {
                        return formatTime(chargeTimeRemaining / 1000) // Convert from ms to seconds
                    }
                }
            }
            
            // Fallback: estimate based on typical drain/charge rates
            return if (isCharging) {
                // Estimate: ~1.5% per minute when charging
                val minutesLeft = ((100 - batteryPct) / 1.5).toInt()
                if (minutesLeft > 0) formatTime(minutesLeft.toLong() * 60) else ""
            } else {
                // Estimate: average ~10 hours for full battery (typical usage)
                val hoursLeft = (batteryPct * 10) / 100.0
                if (hoursLeft > 0) {
                    val minutes = (hoursLeft * 60).toInt()
                    formatTime(minutes.toLong() * 60)
                } else ""
            }
        }
        
        /**
         * Format seconds to readable time string
         */
        private fun formatTime(seconds: Long): String {
            val hours = TimeUnit.SECONDS.toHours(seconds)
            val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
            
            return when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}min left"
                hours > 0 -> "${hours}h left"
                minutes > 0 -> "${minutes} min left"
                else -> ""
            }
        }
        
        /**
         * Request widget update
         */
        fun requestUpdate(context: Context) {
            val intent = Intent(context, ChargingWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ChargingWidgetProvider::class.java)
            intent.putExtra(
                AppWidgetManager.EXTRA_APPWIDGET_IDS,
                appWidgetManager.getAppWidgetIds(componentName)
            )
            context.sendBroadcast(intent)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        // Update all widgets when battery state changes
        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, ChargingWidgetProvider::class.java)
                val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (widgetId in widgetIds) {
                    updateWidget(context, appWidgetManager, widgetId)
                }
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // First widget added - register with WidgetUpdateService and start
        com.newthingwidgets.clone.services.WidgetUpdateService.registerWidget(
            ChargingWidgetProvider::class.java,
            object : com.newthingwidgets.clone.services.WidgetUpdateService.Companion.WidgetUpdateCallback {
                override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, widgetIds: IntArray) {
                    for (widgetId in widgetIds) {
                        updateWidget(context, appWidgetManager, widgetId)
                    }
                }
            }
        )
        com.newthingwidgets.clone.services.WidgetUpdateService.start(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Last widget removed - unregister and stop service if no more widgets
        com.newthingwidgets.clone.services.WidgetUpdateService.unregisterWidget(ChargingWidgetProvider::class.java)
        if (!com.newthingwidgets.clone.services.WidgetUpdateService.hasRegisteredWidgets()) {
            com.newthingwidgets.clone.services.WidgetUpdateService.stop(context)
        }
    }
}
