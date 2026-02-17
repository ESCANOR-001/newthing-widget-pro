package com.newthingwidgets.clone.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.provider.AlarmClock
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.services.WidgetUpdateService
import java.util.Calendar
import java.util.Locale

/**
 * Drop Pulse Analog Clock Widget Provider.
 * Uses RemoteViews with a rotating drop pointer and a center hour text.
 */
class DropPulseAnalogClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        ensureRealtimeUpdates(context)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            Intent.ACTION_TIME_TICK,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, DropPulseAnalogClockWidgetProvider::class.java)
                val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (widgetId in widgetIds) {
                    updateWidget(context, appWidgetManager, widgetId)
                }
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        ensureRealtimeUpdates(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)

        WidgetUpdateService.unregisterWidget(DropPulseAnalogClockWidgetProvider::class.java)
        if (!WidgetUpdateService.hasRegisteredWidgets()) {
            WidgetUpdateService.stop(context)
        }
    }

    private fun ensureRealtimeUpdates(context: Context) {
        WidgetUpdateService.registerWidget(
            DropPulseAnalogClockWidgetProvider::class.java,
            object : WidgetUpdateService.Companion.WidgetUpdateCallback {
                override fun onUpdate(
                    context: Context,
                    appWidgetManager: AppWidgetManager,
                    widgetIds: IntArray
                ) {
                    for (widgetId in widgetIds) {
                        updateWidget(context, appWidgetManager, widgetId)
                    }
                }
            }
        )

        WidgetUpdateService.setUpdateInterval(1000L)
        WidgetUpdateService.start(context)
    }

    companion object {
        internal fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.drop_pulse_analog_clock_widget_you)

            val calendar = Calendar.getInstance()
            val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
            val displayHour = ((hour24 + 11) % 12) + 1
            val hourText = String.format(Locale.US, "%02d", displayHour)
            val hourBitmap = createHourBitmap(context, hourText)
            views.setImageViewBitmap(R.id.hour_image, hourBitmap)

            // Keep pointer travelling continuously toward the next minute.
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)
            val milli = calendar.get(Calendar.MILLISECOND)
            val minuteProgress = minute + (second / 60f) + (milli / 60000f)
            val minuteRotation = minuteProgress * 6f
            val pointerBitmap = createRotatedPointerBitmap(context, minuteRotation)
            views.setImageViewBitmap(R.id.drop_pointer, pointerBitmap)

            val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                clockIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.mainc, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun createRotatedPointerBitmap(context: Context, angleDegrees: Float): Bitmap {
            val drawable = ContextCompat.getDrawable(context, R.drawable.drop_pulse_minute_hand)
                ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

            val size = resolveDrawableSize(drawable).coerceAtLeast(1)
            val base = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val baseCanvas = Canvas(base)
            drawable.setBounds(0, 0, size, size)
            drawable.draw(baseCanvas)

            val rotated = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val rotatedCanvas = Canvas(rotated)
            rotatedCanvas.save()
            val pivot = size / 2f
            rotatedCanvas.rotate(angleDegrees, pivot, pivot)
            rotatedCanvas.drawBitmap(base, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG))
            rotatedCanvas.restore()
            base.recycle()
            return rotated
        }

        private fun createHourBitmap(context: Context, hourText: String): Bitmap {
            val density = context.resources.displayMetrics.density
            val typeface = try {
                ResourcesCompat.getFont(context, R.font.nothing_5_7)
            } catch (e: Exception) {
                null
            } ?: Typeface.DEFAULT_BOLD

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 32f * density
                this.typeface = typeface
            }

            val bounds = Rect()
            paint.getTextBounds(hourText, 0, hourText.length, bounds)
            val textWidth = paint.measureText(hourText).toInt()
            val textHeight = bounds.height()
            val padding = (4f * density).toInt().coerceAtLeast(1)

            val bitmap = Bitmap.createBitmap(
                (textWidth + (padding * 2)).coerceAtLeast(1),
                (textHeight + (padding * 2)).coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            val x = padding.toFloat()
            val y = padding.toFloat() - bounds.top
            canvas.drawText(hourText, x, y, paint)
            return bitmap
        }

        private fun resolveDrawableSize(drawable: Drawable): Int {
            val width = drawable.intrinsicWidth
            val height = drawable.intrinsicHeight
            return when {
                width > 0 && height > 0 -> maxOf(width, height)
                width > 0 -> width
                height > 0 -> height
                else -> 389
            }
        }
    }
}
