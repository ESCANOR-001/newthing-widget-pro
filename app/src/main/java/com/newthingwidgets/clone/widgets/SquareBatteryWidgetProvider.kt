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
import android.view.View
import android.widget.RemoteViews
import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.services.WidgetUpdateService
import java.util.concurrent.TimeUnit

/**
 * Square Battery Widget Provider
 * Displays battery status with:
 * - Lightning bolt icon + percentage at top
 * - 5 segmented battery bars (filled based on level)
 * - Time remaining at bottom (~X hours format)
 */
class SquareBatteryWidgetProvider : AppWidgetProvider() {

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
        
        // Update all widgets when battery state changes
        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, SquareBatteryWidgetProvider::class.java)
                val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (widgetId in widgetIds) {
                    updateWidget(context, appWidgetManager, widgetId)
                }
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Register with WidgetUpdateService for real-time updates
        WidgetUpdateService.registerWidget(
            SquareBatteryWidgetProvider::class.java,
            object : WidgetUpdateService.Companion.WidgetUpdateCallback {
                override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, widgetIds: IntArray) {
                    for (widgetId in widgetIds) {
                        updateWidget(context, appWidgetManager, widgetId)
                    }
                }
            }
        )
        WidgetUpdateService.start(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Unregister from service
        WidgetUpdateService.unregisterWidget(SquareBatteryWidgetProvider::class.java)
        if (!WidgetUpdateService.hasRegisteredWidgets()) {
            WidgetUpdateService.stop(context)
        }
    }

    companion object {
        /**
         * Update widget with current battery information
         */
        internal fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.square_battery_widget)
            
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
            
            // Update percentage text
            views.setTextViewText(R.id.battery_percentage, "$batteryPct%")
            
            // Update charging icon visibility and color
            if (isCharging) {
                views.setViewVisibility(R.id.charging_icon, View.VISIBLE)
                views.setInt(R.id.charging_icon, "setColorFilter", 
                    androidx.core.content.ContextCompat.getColor(context, R.color.red_color))
            } else {
                views.setViewVisibility(R.id.charging_icon, View.GONE)
            }
            
            // Update 5 segments based on battery percentage
            updateSegments(views, batteryPct)
            
            // Update time remaining text
            val timeText = getTimeRemainingText(context, isCharging, batteryPct)
            views.setTextViewText(R.id.time_remaining, timeText)
            
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
         * Update segment backgrounds based on battery percentage
         * Each segment represents 20% (5 segments = 100%)
         * Segments fill vertically from bottom to top
         */
        private fun updateSegments(views: RemoteViews, batteryPct: Int) {
            val segmentIds = listOf(
                R.id.segment_1,
                R.id.segment_2,
                R.id.segment_3,
                R.id.segment_4,
                R.id.segment_5
            )
            
            // Each segment represents 20%
            val segmentValue = 20
            
            for ((index, segmentId) in segmentIds.withIndex()) {
                val segmentStart = index * segmentValue  // 0, 20, 40, 60, 80
                val segmentEnd = segmentStart + segmentValue  // 20, 40, 60, 80, 100
                
                val drawable = when {
                    // Battery is past this segment - fully filled
                    batteryPct >= segmentEnd -> R.drawable.battery_segment_filled
                    // Battery is before this segment - empty
                    batteryPct <= segmentStart -> R.drawable.battery_segment_empty
                    // Battery is within this segment - show partial fill
                    else -> {
                        // Calculate fill percentage within this segment (0-100)
                        val fillPercent = ((batteryPct - segmentStart) * 100) / segmentValue
                        when {
                            fillPercent < 25 -> R.drawable.battery_segment_empty
                            fillPercent < 50 -> R.drawable.battery_segment_quarter
                            fillPercent < 75 -> R.drawable.battery_segment_half
                            fillPercent < 95 -> R.drawable.battery_segment_threequarters
                            else -> R.drawable.battery_segment_filled
                        }
                    }
                }
                views.setImageViewResource(segmentId, drawable)
            }
        }
        
        /**
         * Get time remaining text in "~X hours" format
         */
        private fun getTimeRemainingText(
            context: Context,
            isCharging: Boolean,
            batteryPct: Int
        ): String {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                
                if (isCharging) {
                    val chargeTimeRemaining = batteryManager.computeChargeTimeRemaining()
                    if (chargeTimeRemaining > 0) {
                        return formatTimeApprox(chargeTimeRemaining / 1000)
                    }
                }
            }
            
            // Fallback: estimate based on typical drain/charge rates
            return if (isCharging) {
                val minutesLeft = ((100 - batteryPct) / 1.5).toInt()
                if (minutesLeft > 0) formatTimeApprox(minutesLeft.toLong() * 60) else "~Full"
            } else {
                // Estimate: ~10 hours for full battery
                val hoursLeft = (batteryPct * 10) / 100.0
                if (hoursLeft > 0) {
                    val minutes = (hoursLeft * 60).toInt()
                    formatTimeApprox(minutes.toLong() * 60)
                } else "~0 min"
            }
        }
        
        /**
         * Format seconds to approximate time string "~X hours" or "~X min"
         */
        private fun formatTimeApprox(seconds: Long): String {
            val hours = TimeUnit.SECONDS.toHours(seconds)
            val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
            
            return when {
                hours >= 1 -> "~${hours} hour${if (hours > 1) "s" else ""}"
                minutes > 0 -> "~${minutes} min"
                else -> "~Full"
            }
        }
    }
}
