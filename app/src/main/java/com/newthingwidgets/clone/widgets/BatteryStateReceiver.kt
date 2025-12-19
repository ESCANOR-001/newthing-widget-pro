package com.newthingwidgets.clone.widgets

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * Receiver that listens for power connection/disconnection events
 * and triggers updates to the Charging Widget
 */
class BatteryStateReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED -> {
                // Update all charging widgets
                updateChargingWidgets(context)
            }
        }
    }
    
    private fun updateChargingWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, ChargingWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
        
        for (widgetId in widgetIds) {
            ChargingWidgetProvider.updateWidget(context, appWidgetManager, widgetId)
        }
    }
}
