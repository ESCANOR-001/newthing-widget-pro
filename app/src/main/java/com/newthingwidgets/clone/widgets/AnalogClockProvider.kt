package com.newthingwidgets.clone.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.newthingwidgets.clone.R

/**
 * Single provider for all Analog Clock widgets.
 * Each clock variant uses a different widget_info.xml with different layout,
 * but shares this same provider class.
 */
class AnalogClockProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Called when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget is removed
    }

    companion object {
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Get widget info to determine which layout to use
            val widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
            val layoutId = widgetInfo?.initialLayout ?: R.layout.analog2_widget
            
            val views = RemoteViews(context.packageName, layoutId)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
