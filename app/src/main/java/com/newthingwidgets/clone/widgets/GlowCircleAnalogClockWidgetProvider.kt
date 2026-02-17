package com.newthingwidgets.clone.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.RemoteViews
import com.newthingwidgets.clone.R

/**
 * Glow Circle Analog Clock Widget Provider
 * Based on the analog12_widget_you layout from the extracted res workspace.
 * Displays a circular analog clock with glow background. Clicking opens the system Clock app.
 */
class GlowCircleAnalogClockWidgetProvider : AppWidgetProvider() {

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
        internal fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.glow_circle_analog_clock_widget_you)

            // Click opens Clock app
            val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                clockIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Root layout id comes from original analog12_widget_you
            views.setOnClickPendingIntent(R.id.ana_12, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

