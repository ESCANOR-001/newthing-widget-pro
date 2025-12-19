package com.newthingwidgets.clone.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.widget.RemoteViews
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.services.WidgetUpdateService
import java.util.concurrent.TimeUnit

/**
 * Battery Meter Widget Provider
 * Displays:
 * - BATTERY label at top
 * - Horizontal battery figure with 10 segments
 * - Large percentage in Nothing font
 * - Time remaining with "REMAINING" in red
 */
class BatteryMeterWidgetProvider : AppWidgetProvider() {

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
            Intent.ACTION_BATTERY_CHANGED,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, BatteryMeterWidgetProvider::class.java)
                val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (widgetId in widgetIds) {
                    updateWidget(context, appWidgetManager, widgetId)
                }
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateService.registerWidget(
            BatteryMeterWidgetProvider::class.java,
            object : WidgetUpdateService.Companion.WidgetUpdateCallback {
                override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, widgetIds: IntArray) {
                    for (widgetId in widgetIds) {
                        Companion.updateWidget(context, appWidgetManager, widgetId)
                    }
                }
            }
        )
        WidgetUpdateService.start(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetUpdateService.unregisterWidget(BatteryMeterWidgetProvider::class.java)
        if (!WidgetUpdateService.hasRegisteredWidgets()) {
            WidgetUpdateService.stop(context)
        }
    }

    companion object {
        // Segment IDs for the 10 battery bars
        private val SEGMENT_IDS = listOf(
            R.id.segment_1, R.id.segment_2, R.id.segment_3, R.id.segment_4, R.id.segment_5,
            R.id.segment_6, R.id.segment_7, R.id.segment_8, R.id.segment_9, R.id.segment_10
        )

        internal fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.battery_meter_widget)
            
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
            
            // Update segments (10 segments, each represents 10%)
            updateSegments(views, batteryPct)
            
            // Create and set battery percentage bitmap with custom font
            val percentageBitmap = createPercentageBitmap(context, batteryPct)
            views.setImageViewBitmap(R.id.battery_percentage_image, percentageBitmap)
            
            // Update time value
            val timeText = getTimeText(context, isCharging, batteryPct)
            views.setTextViewText(R.id.time_value, timeText)
            
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
            views.setOnClickPendingIntent(R.id.battery_card, pendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * Update the 10 battery segments based on percentage
         */
        private fun updateSegments(views: RemoteViews, batteryPct: Int) {
            // Calculate how many segments should be filled (each segment = 10%)
            val filledSegments = batteryPct / 10
            
            for ((index, segmentId) in SEGMENT_IDS.withIndex()) {
                val drawable = if (index < filledSegments) {
                    R.drawable.battery_meter_segment_filled
                } else {
                    R.drawable.battery_meter_segment_empty
                }
                views.setImageViewResource(segmentId, drawable)
            }
        }

        /**
         * Create percentage bitmap with custom Nothing font
         */
        private fun createPercentageBitmap(context: Context, batteryPct: Int): Bitmap {
            val text = "$batteryPct"
            val percentSign = "%"
            
            // Load custom font
            val typeface = ResourcesCompat.getFont(context, R.font.nothing_5_7)
            
            // Red color for percentage
            val redColor = context.getColor(R.color.red_color)
            
            // Paint for main number
            val mainPaint = Paint().apply {
                isAntiAlias = true
                textSize = 84f
                color = redColor
                this.typeface = typeface
            }
            
            // Paint for percent sign (smaller)
            val percentPaint = Paint().apply {
                isAntiAlias = true
                textSize = 48f
                color = redColor
                this.typeface = typeface
            }
            
            // Measure text dimensions
            val mainBounds = Rect()
            mainPaint.getTextBounds(text, 0, text.length, mainBounds)
            val mainWidth = mainPaint.measureText(text).toInt()
            
            val percentBounds = Rect()
            percentPaint.getTextBounds(percentSign, 0, percentSign.length, percentBounds)
            val percentWidth = percentPaint.measureText(percentSign).toInt()
            
            // Create bitmap
            val totalWidth = mainWidth + percentWidth + 8
            val totalHeight = mainBounds.height() + 20
            
            val bitmap = createBitmap(totalWidth, totalHeight)
            val canvas = Canvas(bitmap)
            
            // Draw main number
            canvas.drawText(text, 0f, mainBounds.height().toFloat() + 10, mainPaint)
            
            // Draw percent sign
            canvas.drawText(percentSign, mainWidth.toFloat() + 4, mainBounds.height().toFloat() + 10, percentPaint)
            
            return bitmap
        }

        /**
         * Get time remaining in compact format
         */
        private fun getTimeText(
            context: Context,
            isCharging: Boolean,
            batteryPct: Int
        ): String {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                
                if (isCharging) {
                    val chargeTimeRemaining = batteryManager.computeChargeTimeRemaining()
                    if (chargeTimeRemaining > 0) {
                        return formatTimeCompact(chargeTimeRemaining / 1000)
                    }
                }
            }
            
            // Fallback estimates
            return if (isCharging) {
                val minutesLeft = ((100 - batteryPct) * 1.5).toInt()
                formatTimeCompact(minutesLeft.toLong() * 60)
            } else {
                // Estimate: ~10 hours for full battery
                val hoursLeft = (batteryPct * 10) / 100.0
                val minutes = (hoursLeft * 60).toInt()
                formatTimeCompact(minutes.toLong() * 60)
            }
        }

        /**
         * Format seconds to compact "XH YM" format
         */
        private fun formatTimeCompact(seconds: Long): String {
            val hours = TimeUnit.SECONDS.toHours(seconds)
            val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
            
            return when {
                hours >= 1 && minutes > 0 -> "${hours}H${minutes}M"
                hours >= 1 -> "${hours}H"
                minutes > 0 -> "${minutes}M"
                else -> "0M"
            }
        }
    }
}
