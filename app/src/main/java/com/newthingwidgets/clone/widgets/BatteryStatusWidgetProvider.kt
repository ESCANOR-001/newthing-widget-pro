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
 * Battery Status Widget Provider
 * Displays:
 * - SYSTEM label + charging status
 * - Battery icon (changes based on charging state)
 * - Large percentage in red
 * - Progress bar
 * - BATTERY label + time remaining
 */
class BatteryStatusWidgetProvider : AppWidgetProvider() {

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
                val componentName = ComponentName(context, BatteryStatusWidgetProvider::class.java)
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
            BatteryStatusWidgetProvider::class.java,
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
        WidgetUpdateService.unregisterWidget(BatteryStatusWidgetProvider::class.java)
        if (!WidgetUpdateService.hasRegisteredWidgets()) {
            WidgetUpdateService.stop(context)
        }
    }

    companion object {
        internal fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.battery_status_widget)
            
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
            
            // Update charging status
            if (isCharging) {
                views.setViewVisibility(R.id.charging_bolt_small, View.VISIBLE)
                views.setTextViewText(R.id.charging_status, "CHARGING")
                views.setImageViewResource(R.id.battery_icon, R.drawable.ic_battery_charging)
            } else {
                views.setViewVisibility(R.id.charging_bolt_small, View.GONE)
                views.setTextViewText(R.id.charging_status, "DISCHARGING")
                views.setImageViewResource(R.id.battery_icon, R.drawable.ic_battery_outline)
            }
            
            // Update percentage text
            views.setTextViewText(R.id.battery_percentage, "$batteryPct%")
            
            // Update progress bar
            views.setProgressBar(R.id.battery_progress, 100, batteryPct, false)
            
            // Update time remaining
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
         * Get time remaining text in "Xh Xm left" format
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
                        return formatTimeCompact(chargeTimeRemaining / 1000) + " left"
                    }
                }
            }
            
            // Fallback estimates
            return if (isCharging) {
                val minutesLeft = ((100 - batteryPct) * 1.5).toInt()
                formatTimeCompact(minutesLeft.toLong() * 60) + " to full"
            } else {
                // Estimate: ~10 hours for full battery
                val hoursLeft = (batteryPct * 10) / 100.0
                val minutes = (hoursLeft * 60).toInt()
                formatTimeCompact(minutes.toLong() * 60) + " left"
            }
        }
        
        /**
         * Format seconds to compact time string "Xh Xm"
         */
        private fun formatTimeCompact(seconds: Long): String {
            val hours = TimeUnit.SECONDS.toHours(seconds)
            val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
            
            return when {
                hours >= 1 && minutes > 0 -> "${hours}h ${minutes}m"
                hours >= 1 -> "${hours}h"
                minutes > 0 -> "${minutes}m"
                else -> "0m"
            }
        }
    }
}
