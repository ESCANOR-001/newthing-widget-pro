package com.newthingwidgets.clone.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.widget.RemoteViews
import androidx.core.content.res.ResourcesCompat
import com.newthingwidgets.clone.AppPackages
import com.newthingwidgets.clone.R
import java.util.Calendar

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
        
        when (intent.action) {
            ACTION_LAUNCH_APP -> {
                val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: return
                launchOrInstallApp(context, appName)
            }
            Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> {
                // Refresh all calendar widgets when date changes
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = android.content.ComponentName(context, AppLauncherWidgetProvider::class.java)
                val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (widgetId in widgetIds) {
                    val appName = getAppName(context, widgetId)
                    if (appName == "Calendar") {
                        updateAppWidget(context, appWidgetManager, widgetId)
                    }
                }
            }
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

            // Use different layout for Clock app (functional analog clock) and Calendar app (real-time date)
            val views = when (appName) {
                "Clock" -> RemoteViews(context.packageName, R.layout.app_launcher_clock_widget)
                "Calendar" -> RemoteViews(context.packageName, R.layout.app_launcher_calendar_widget).also {
                    // Render date with custom font as bitmap
                    val dateBitmap = createDateBitmap(context)
                    it.setImageViewBitmap(R.id.calendar_date, dateBitmap)
                }
                else -> RemoteViews(context.packageName, R.layout.app_launcher_widget).also {
                    // Set the app icon for other widgets
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
         * Create a bitmap with the current date using custom font
         */
        private fun createDateBitmap(context: Context): Bitmap {
            val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString()
            
            // Load custom font
            val typeface = ResourcesCompat.getFont(context, R.font.nothing_5_7) ?: Typeface.DEFAULT_BOLD
            
            // Create paint with custom font
            val paint = Paint().apply {
                this.typeface = typeface
                textSize = 400f
                color = android.graphics.Color.WHITE
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            
            // Measure text bounds
            val textBounds = android.graphics.Rect()
            paint.getTextBounds(dayOfMonth, 0, dayOfMonth.length, textBounds)
            
            // Create bitmap with padding
            val padding = 40
            val width = textBounds.width() + padding * 2
            val height = textBounds.height() + padding * 2
            
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Draw text centered
            val x = width / 2f
            val y = height / 2f - textBounds.exactCenterY()
            canvas.drawText(dayOfMonth, x, y, paint)
            
            return bitmap
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
