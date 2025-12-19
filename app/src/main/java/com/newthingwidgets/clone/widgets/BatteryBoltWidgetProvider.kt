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
import com.newthingwidgets.clone.services.WidgetUpdateService
import java.util.concurrent.TimeUnit

/**
 * Battery Bolt Widget Provider
 * Displays battery status with:
 * - Large percentage at top-left
 * - Status text (Charged/Draining)
 * - Time remaining at bottom-left
 * - Large lightning bolt that fills based on battery level
 */
class BatteryBoltWidgetProvider : AppWidgetProvider() {

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
                val componentName = ComponentName(context, BatteryBoltWidgetProvider::class.java)
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
            BatteryBoltWidgetProvider::class.java,
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
        WidgetUpdateService.unregisterWidget(BatteryBoltWidgetProvider::class.java)
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
            val views = RemoteViews(context.packageName, R.layout.battery_bolt_widget)
            
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
            
            // Update percentage text (number only, % sign is separate)
            views.setTextViewText(R.id.battery_number, "$batteryPct%")
            
            // Update status text
            val statusText = if (isCharging) "Charged" else "Draining"
            views.setTextViewText(R.id.status_text, statusText)
            
            // Update bolt icon based on battery level
            val boltDrawable = getBoltDrawable(batteryPct)
            views.setImageViewResource(R.id.bolt_icon, boltDrawable)
            
            // Update time text (combined into single TextView)
            val timeText = getTimeText(context, isCharging, batteryPct)
            views.setTextViewText(R.id.time_text, timeText)
            
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
         * Get the appropriate bolt drawable based on battery percentage
         */
        private fun getBoltDrawable(batteryPct: Int): Int {
            return when {
                batteryPct <= 10 -> R.drawable.ic_bolt_empty
                batteryPct <= 30 -> R.drawable.ic_bolt_quarter
                batteryPct <= 55 -> R.drawable.ic_bolt_half
                batteryPct <= 80 -> R.drawable.ic_bolt_threequarters
                else -> R.drawable.ic_bolt_filled
            }
        }
        
        /**
         * Get time remaining text as combined string
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
                        val timeStr = formatTime(chargeTimeRemaining / 1000)
                        return "$timeStr\nfor full charge"
                    }
                }
            }
            
            // Fallback estimates
            return if (isCharging) {
                val minutesLeft = ((100 - batteryPct) * 1.5).toInt()
                val timeStr = formatTime(minutesLeft.toLong() * 60)
                "$timeStr\nfor full charge"
            } else {
                // Estimate: ~10 hours for full battery
                val hoursLeft = (batteryPct * 10) / 100.0
                val minutes = (hoursLeft * 60).toInt()
                val timeStr = formatTime(minutes.toLong() * 60)
                "$timeStr\nremaining"
            }
        }
        
        /**
         * Format seconds to readable time string
         */
        private fun formatTime(seconds: Long): String {
            val hours = TimeUnit.SECONDS.toHours(seconds)
            val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
            
            return when {
                hours >= 1 && minutes > 0 -> "${hours},${minutes / 10} hours"
                hours >= 1 -> "$hours hours"
                minutes > 0 -> "$minutes min"
                else -> "Full"
            }
        }
    }
}
