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
 * Square Analog Clock Widget Provider
 * Displays a functional analog clock with custom styling that automatically updates.
 * Clicking on it opens the system Clock app.
 */
class SquareAnalogClockWidgetProvider : AppWidgetProvider() {

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
            val views = RemoteViews(context.packageName, R.layout.square_analog_clock_widget_you)

            // Set click to open Clock app
            val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val clockPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                clockIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.ana_3, clockPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

