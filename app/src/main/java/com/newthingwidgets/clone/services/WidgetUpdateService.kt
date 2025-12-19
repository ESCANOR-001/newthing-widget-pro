package com.newthingwidgets.clone.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.newthingwidgets.clone.MainActivity
import com.newthingwidgets.clone.R

/**
 * Universal Widget Update Service
 * 
 * A reusable foreground service that provides real-time widget updates.
 * Can be used by any widget that needs frequent updates.
 * 
 * Usage:
 * 1. Register a widget class with registerWidget()
 * 2. Start the service when widget is added
 * 3. Stop the service when all widgets are removed
 * 
 * Features:
 * - Configurable update interval
 * - Multiple widget support
 * - Battery-efficient when no widgets registered
 * - Automatic notification management
 */
class WidgetUpdateService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    
    companion object {
        private const val CHANNEL_ID = "widget_update_channel"
        private const val NOTIFICATION_ID = 1001
        
        // Update interval in milliseconds (configurable)
        var updateIntervalMs: Long = 1000L // Default: 1 second
        
        // Registered widget update callbacks
        private val widgetCallbacks = mutableMapOf<Class<*>, WidgetUpdateCallback>()
        
        /**
         * Interface for widget update callbacks
         */
        interface WidgetUpdateCallback {
            fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, widgetIds: IntArray)
        }
        
        /**
         * Register a widget for updates
         * @param widgetClass The widget provider class
         * @param callback The update callback
         */
        fun registerWidget(widgetClass: Class<*>, callback: WidgetUpdateCallback) {
            widgetCallbacks[widgetClass] = callback
        }
        
        /**
         * Unregister a widget from updates
         * @param widgetClass The widget provider class
         */
        fun unregisterWidget(widgetClass: Class<*>) {
            widgetCallbacks.remove(widgetClass)
        }
        
        /**
         * Check if any widgets are registered
         */
        fun hasRegisteredWidgets(): Boolean = widgetCallbacks.isNotEmpty()
        
        /**
         * Start the service
         */
        fun start(context: Context) {
            val intent = Intent(context, WidgetUpdateService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        /**
         * Stop the service
         */
        fun stop(context: Context) {
            val intent = Intent(context, WidgetUpdateService::class.java)
            context.stopService(intent)
        }
        
        /**
         * Set the update interval
         * @param intervalMs Interval in milliseconds
         */
        fun setUpdateInterval(intervalMs: Long) {
            updateIntervalMs = intervalMs
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        startUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopUpdates()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Create notification channel for Android O+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Widget Updates",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps widgets updated in real-time"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create the foreground notification
     */
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Widget Updates Active")
            .setContentText("Keeping your widgets up to date")
            .setSmallIcon(R.drawable.ic_charging_bolt)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Start the update loop
     */
    private fun startUpdates() {
        updateRunnable = object : Runnable {
            override fun run() {
                updateAllWidgets()
                handler.postDelayed(this, updateIntervalMs)
            }
        }
        handler.post(updateRunnable!!)
    }

    /**
     * Stop the update loop
     */
    private fun stopUpdates() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
    }

    /**
     * Update all registered widgets
     */
    private fun updateAllWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        
        for ((widgetClass, callback) in widgetCallbacks) {
            try {
                val componentName = ComponentName(this, widgetClass)
                val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
                
                if (widgetIds.isNotEmpty()) {
                    callback.onUpdate(this, appWidgetManager, widgetIds)
                }
            } catch (e: Exception) {
                // Log error but continue updating other widgets
                e.printStackTrace()
            }
        }
    }
}
