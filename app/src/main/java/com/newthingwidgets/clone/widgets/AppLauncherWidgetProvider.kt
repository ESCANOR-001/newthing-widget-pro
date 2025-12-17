package com.newthingwidgets.clone.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.newthingwidgets.clone.AppPackages
import com.newthingwidgets.clone.R

/**
 * App Launcher Widget Provider
 * Launches the target app if installed (using dynamic package discovery), 
 * otherwise redirects to Play Store.
 */
class AppLauncherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            // Check if there's a pending config for new widgets
            val pendingPrefs = context.getSharedPreferences("PendingWidgetConfig", Context.MODE_PRIVATE)
            val pendingAppName = pendingPrefs.getString("pending_app_name", null)
            val pendingDrawable = pendingPrefs.getInt("pending_drawable", -1)
            
            if (pendingAppName != null && pendingDrawable != -1) {
                // Save the pending config to this widget
                saveWidgetConfig(context, appWidgetId, pendingAppName, pendingDrawable)
                // Clear the pending config
                pendingPrefs.edit().clear().apply()
            }
            
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_LAUNCH_APP) {
            val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: return
            launchOrInstallApp(context, appName)
        }
    }

    companion object {
        const val ACTION_LAUNCH_APP = "com.newthingwidgets.clone.ACTION_LAUNCH_APP"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_DRAWABLE_RES = "extra_drawable_res"
        private const val PREFS_NAME = "AppLauncherWidgetPrefs"
        private const val PREF_PREFIX_APP_NAME = "widget_app_name_"
        private const val PREF_PREFIX_DRAWABLE = "widget_drawable_"

        /**
         * Save widget configuration (which app it launches)
         */
        fun saveWidgetConfig(context: Context, widgetId: Int, appName: String, drawableRes: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(PREF_PREFIX_APP_NAME + widgetId, appName)
                .putInt(PREF_PREFIX_DRAWABLE + widgetId, drawableRes)
                .apply()
        }

        /**
         * Get saved app name for a widget
         */
        fun getAppName(context: Context, widgetId: Int): String? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(PREF_PREFIX_APP_NAME + widgetId, null)
        }

        /**
         * Get saved drawable resource for a widget
         */
        fun getDrawableRes(context: Context, widgetId: Int): Int {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getInt(PREF_PREFIX_DRAWABLE + widgetId, R.drawable.dial)
        }

        /**
         * Update the widget appearance and click behavior
         */
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val appName = getAppName(context, appWidgetId) ?: return
            val drawableRes = getDrawableRes(context, appWidgetId)

            // Use different layout for Clock app (functional analog clock)
            val views = if (appName == "Clock") {
                RemoteViews(context.packageName, R.layout.app_launcher_clock_widget)
            } else {
                RemoteViews(context.packageName, R.layout.app_launcher_widget).also {
                    // Set the app icon for non-clock widgets
                    it.setImageViewResource(R.id.amazon_icon, drawableRes)
                }
            }

            // Create click intent - pass app name for dynamic discovery at launch time
            val clickIntent = Intent(context, AppLauncherWidgetProvider::class.java).apply {
                action = ACTION_LAUNCH_APP
                putExtra(EXTRA_APP_NAME, appName)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.real_work, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * Launch app using dynamic package discovery, otherwise open Play Store
         */
        private fun launchOrInstallApp(context: Context, appName: String) {
            val pm = context.packageManager
            
            // Use dynamic package discovery to find the installed app
            val installedPackage = AppPackages.findInstalledPackage(context, appName)
            
            if (installedPackage != null) {
                // App found - launch it
                val launchIntent = pm.getLaunchIntentForPackage(installedPackage)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return
                }
            }
            
            // App not installed - open Play Store with default package
            val playStorePackage = AppPackages.getPlayStorePackage(appName) ?: return
            try {
                val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://details?id=$playStorePackage")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(playStoreIntent)
            } catch (e: Exception) {
                // Play Store not available - open web browser
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=$playStorePackage")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            }
        }

        /**
         * Delete widget configuration when widget is removed
         */
        fun deleteWidgetConfig(context: Context, widgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .remove(PREF_PREFIX_APP_NAME + widgetId)
                .remove(PREF_PREFIX_DRAWABLE + widgetId)
                .apply()
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            deleteWidgetConfig(context, widgetId)
        }
    }
}
