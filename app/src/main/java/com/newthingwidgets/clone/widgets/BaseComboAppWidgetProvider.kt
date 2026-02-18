package com.newthingwidgets.clone.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.newthingwidgets.clone.utils.AppLaunchRouter

abstract class BaseComboAppWidgetProvider : AppWidgetProvider() {

    protected abstract val layoutResId: Int
    protected abstract val clickMap: Map<Int, String>

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_LAUNCH_COMBO_APP) {
            val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: return
            AppLaunchRouter.launchOrInstallApp(context, appName)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, layoutResId)

        for ((viewId, appName) in clickMap) {
            val clickIntent = Intent(context, javaClass).apply {
                action = ACTION_LAUNCH_COMBO_APP
                putExtra(EXTRA_APP_NAME, appName)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(EXTRA_VIEW_ID, viewId)
            }

            val requestCode = (appWidgetId * 1000) + viewId
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(viewId, pendingIntent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val ACTION_LAUNCH_COMBO_APP = "com.newthingwidgets.clone.ACTION_LAUNCH_COMBO_APP"
        const val EXTRA_APP_NAME = "extra_combo_app_name"
        const val EXTRA_VIEW_ID = "extra_combo_view_id"
    }
}
