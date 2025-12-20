package com.newthingwidgets.clone.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.BatteryManager
import android.provider.Settings
import android.widget.RemoteViews
import androidx.core.graphics.createBitmap
import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.services.WidgetUpdateService

/**
 * Battery Dot Matrix Widget Provider
 * Displays battery level as a dot matrix grid (18 columns × 8 rows)
 * Dots fill from left to right based on battery percentage
 */
class BatteryDotMatrixWidgetProvider : AppWidgetProvider() {

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
                val componentName = ComponentName(context, BatteryDotMatrixWidgetProvider::class.java)
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
            BatteryDotMatrixWidgetProvider::class.java,
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
        WidgetUpdateService.unregisterWidget(BatteryDotMatrixWidgetProvider::class.java)
        if (!WidgetUpdateService.hasRegisteredWidgets()) {
            WidgetUpdateService.stop(context)
        }
    }

    companion object {
        // Grid dimensions - 10x10 square = 100 dots
        private const val COLUMNS = 10
        private const val ROWS = 10
        private const val TOTAL_DOTS = COLUMNS * ROWS  // 100 dots
        
        // Dot appearance
        private const val DOT_RADIUS = 8f
        private const val DOT_SPACING = 18f
        
        // Colors
        private const val EMPTY_COLOR = 0xFF404040.toInt()    // Dark gray

        internal fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.battery_dot_matrix_widget)
            
            // Get battery status
            val batteryStatus = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            
            val batteryPct = if (level >= 0 && scale > 0) {
                (level * 100) / scale
            } else {
                0
            }
            
            // Create and set the dot matrix bitmap
            val dotMatrixBitmap = createDotMatrixBitmap(context, batteryPct)
            views.setImageViewBitmap(R.id.dot_matrix_image, dotMatrixBitmap)
            
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
            views.setOnClickPendingIntent(R.id.dot_matrix_card, pendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * Create the dot matrix bitmap based on battery percentage
         * Fills from top-left, row by row (drains from bottom-right up)
         * 100% = all filled from top, 0% = all empty
         */
        private fun createDotMatrixBitmap(context: Context, batteryPct: Int): Bitmap {
            val density = context.resources.displayMetrics.density
            val dotRadius = DOT_RADIUS * density
            val dotSpacing = DOT_SPACING * density
            
            // Calculate bitmap dimensions
            val width = (COLUMNS * dotSpacing).toInt()
            val height = (ROWS * dotSpacing).toInt()
            
            val bitmap = createBitmap(width, height)
            val canvas = Canvas(bitmap)
            
            // Paint for filled dots (red)
            val filledPaint = Paint().apply {
                isAntiAlias = true
                color = context.getColor(R.color.red_color)
                style = Paint.Style.FILL
            }
            
            // Paint for empty dots (gray)
            val emptyPaint = Paint().apply {
                isAntiAlias = true
                color = EMPTY_COLOR
                style = Paint.Style.FILL
            }
            
            // Calculate how many dots should be EMPTY (from top)
            // When battery drains, empty area grows from top down
            val emptyDots = 100 - batteryPct  // Empty dots at top
            
            // Draw dots row by row, starting from top
            // Low battery = more empty dots at top, filled dots at bottom
            var dotIndex = 0
            for (row in 0 until ROWS) {
                for (col in 0 until COLUMNS) {
                    val cx = (col * dotSpacing) + (dotSpacing / 2)
                    val cy = (row * dotSpacing) + (dotSpacing / 2)
                    
                    // Empty from top, filled from bottom
                    val paint = if (dotIndex < emptyDots) emptyPaint else filledPaint
                    canvas.drawCircle(cx, cy, dotRadius, paint)
                    
                    dotIndex++
                }
            }
            
            return bitmap
        }
    }
}
