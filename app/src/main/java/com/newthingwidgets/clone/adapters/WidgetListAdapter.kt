package com.newthingwidgets.clone.adapters

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.WidgetItem
import com.newthingwidgets.clone.widgets.AnalogClockWidgetProvider
import androidx.core.content.edit

class WidgetListAdapter(
    private val widgets: List<WidgetItem>,
    private val isCustomAppsCategory: Boolean = false
) : RecyclerView.Adapter<WidgetListAdapter.WidgetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                if (isCustomAppsCategory) R.layout.item_widget_preview_custom_apps else R.layout.item_widget_preview,
                parent,
                false
            )
        return WidgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {
        val widget = widgets[position]
        holder.bind(widget)
        
        holder.itemView.setOnClickListener { view ->
            val context = view.context
            val appWidgetManager = AppWidgetManager.getInstance(context)
            
            // Determine which provider to use based on widget name/size
            val componentName = when {
                widget.size == "1x1" -> {
                    // App launcher widgets
                    ComponentName(context, com.newthingwidgets.clone.widgets.AppLauncherWidgetProvider::class.java)
                }
                widget.name == "Charging" -> {
                    // Charging widget with real-time battery updates
                    ComponentName(context, com.newthingwidgets.clone.widgets.ChargingWidgetProvider::class.java)
                }
                widget.name == "Battery Square" -> {
                    // Square battery widget with segmented bars
                    ComponentName(context, com.newthingwidgets.clone.widgets.SquareBatteryWidgetProvider::class.java)
                }
                widget.name == "Battery Bolt" -> {
                    // Battery Bolt with lightning bolt fill
                    ComponentName(context, com.newthingwidgets.clone.widgets.BatteryBoltWidgetProvider::class.java)
                }
                widget.name == "Battery Status" -> {
                    // Battery Status with progress bar
                    ComponentName(context, com.newthingwidgets.clone.widgets.BatteryStatusWidgetProvider::class.java)
                }
                widget.name == "Battery Meter" -> {
                    // Battery Meter with horizontal segments
                    ComponentName(context, com.newthingwidgets.clone.widgets.BatteryMeterWidgetProvider::class.java)
                }
                widget.name == "Battery Dot Matrix" -> {
                    // Battery Dot Matrix with dot grid
                    ComponentName(context, com.newthingwidgets.clone.widgets.BatteryDotMatrixWidgetProvider::class.java)
                }
                widget.name == "Date Time Matrix" -> {
                    // Date Time Matrix calendar widget
                    ComponentName(context, com.newthingwidgets.clone.widgets.DateTimeMatrixWidgetProvider::class.java)
                }
                widget.name == "Date Clock Widget" -> {
                    // Date Clock Widget
                    ComponentName(context, com.newthingwidgets.clone.widgets.DateClockWidgetProvider::class.java)
                }
                widget.name == "Calendar Widget" -> {
                    // Calendar Widget
                    ComponentName(context, com.newthingwidgets.clone.widgets.CalendarWidgetProvider::class.java)
                }
                widget.name == "Dot Matrix Clock" -> {
                    // Dot Matrix Clock Widget
                    ComponentName(context, com.newthingwidgets.clone.widgets.DotMatrixClockWidgetProvider::class.java)
                }
                widget.name == "Minimalist Analog Clock" -> {
                    // Minimalist Analog Clock Widget
                    ComponentName(context, com.newthingwidgets.clone.widgets.MinimalistClockWidgetProvider::class.java)
                }
                widget.name == "Classic Analog Clock" -> {
                    // Classic Analog Clock Widget
                    ComponentName(context, com.newthingwidgets.clone.widgets.ClassicClockWidgetProvider::class.java)
                }
                widget.name == "Square Analog Clock" -> {
                    // Square Analog Clock Widget
                    ComponentName(context, com.newthingwidgets.clone.widgets.SquareAnalogClockWidgetProvider::class.java)
                }
                widget.name == "Glow Circle Analog Clock" -> {
                    // Glow Circle Analog Clock Widget (analog12 style)
                    ComponentName(context, com.newthingwidgets.clone.widgets.GlowCircleAnalogClockWidgetProvider::class.java)
                }
                widget.name == "Drop Pulse Analog Clock" || widget.name == "Glow Stroke Analog Clock" -> {
                    // Drop Pulse Analog Clock Widget (legacy Glow Stroke name supported)
                    ComponentName(context, com.newthingwidgets.clone.widgets.DropPulseAnalogClockWidgetProvider::class.java)
                }
                widget.name == "Social App Combo" -> {
                    ComponentName(context, com.newthingwidgets.clone.widgets.SocialAppComboWidgetProvider::class.java)
                }
                widget.name == "Social App Combo Glass" -> {
                    ComponentName(context, com.newthingwidgets.clone.widgets.SocialAppComboGlassWidgetProvider::class.java)
                }
                widget.name == "Google App Combo" -> {
                    ComponentName(context, com.newthingwidgets.clone.widgets.GoogleAppComboWidgetProvider::class.java)
                }
                widget.name == "Google App Combo Glass" -> {
                    ComponentName(context, com.newthingwidgets.clone.widgets.GoogleAppComboGlassWidgetProvider::class.java)
                }
                widget.name == "AI App Combo" -> {
                    ComponentName(context, com.newthingwidgets.clone.widgets.AiAppComboWidgetProvider::class.java)
                }
                widget.name == "AI App Combo Glass" -> {
                    ComponentName(context, com.newthingwidgets.clone.widgets.AiAppComboGlassWidgetProvider::class.java)
                }
                else -> {
                    // Analog clock widgets
                    ComponentName(context, AnalogClockWidgetProvider::class.java)
                }
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    if (widget.size == "1x1") {
                        // For app launcher widgets, save config and request pin
                        val appInfo = com.newthingwidgets.clone.AppPackages.getAppInfo(widget.name)
                        if (appInfo != null) {
                            // Create callback to save widget configuration after pinning
                            val callbackIntent = android.content.Intent(context, com.newthingwidgets.clone.widgets.AppLauncherWidgetProvider::class.java)
                            callbackIntent.action = "android.appwidget.action.APPWIDGET_UPDATE"
                            
                            // Store pending config in SharedPreferences with app name
                            val prefs = context.getSharedPreferences("PendingWidgetConfig", android.content.Context.MODE_PRIVATE)
                            prefs.edit {
                                putString("pending_app_name", widget.name)
                                    .putInt("pending_drawable", appInfo.drawableRes)
                            }
                            
                            appWidgetManager.requestPinAppWidget(componentName, null, null)
                        }
                    } else {
                        appWidgetManager.requestPinAppWidget(componentName, null, null)
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Widget pinning not supported by your launcher",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    context,
                    "Please add widget manually from home screen",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun getItemCount(): Int = widgets.size

    class WidgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val widgetPreview: ImageView = itemView.findViewById(R.id.widget_preview)
        private val widgetName: TextView = itemView.findViewById(R.id.widget_name)
        private val widgetSize: TextView = itemView.findViewById(R.id.widget_size)

        fun bind(widget: WidgetItem) {
            val context = itemView.context
            val density = context.resources.displayMetrics.density
            val isComboWidget = widget.size == "4x1"
            val targetPreviewSize = if (isComboWidget) 300 else 140
            
            // Check if widget has a dynamic layout preview
            if (com.newthingwidgets.clone.utils.LayoutToBitmapRenderer.hasDynamicPreview(widget.name)) {
                // Render layout as bitmap for preview with proper proportions
                try {
                    val bitmap = com.newthingwidgets.clone.utils.LayoutToBitmapRenderer.renderWidgetPreview(
                        context,
                        widget.name,
                        targetSizeDp = targetPreviewSize
                    )
                    if (bitmap != null) {
                        widgetPreview.setImageBitmap(bitmap)
                        widgetPreview.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                    } else {
                        widgetPreview.setImageResource(widget.previewDrawable)
                    }
                } catch (_: Exception) {
                    // Fallback to static drawable if rendering fails
                    widgetPreview.setImageResource(widget.previewDrawable)
                }
            } else {
                // Use static drawable for widgets without layout (Battery 3, 4, etc.)
                widgetPreview.setImageResource(widget.previewDrawable)
            }
            
            widgetName.text = widget.name
            widgetSize.text = widget.size
            
            // Enforce uniform sizing for all app icons
            if (widget.size == "1x1") {
                // For 1x1 app icons: fixed size with consistent padding
                val iconSize = (80 * density).toInt() // Fixed icon display size
                val params = widgetPreview.layoutParams
                params.width = iconSize
                params.height = iconSize
                widgetPreview.layoutParams = params
                widgetPreview.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                widgetPreview.setPadding(0, 0, 0, 0)
            } else if (isComboWidget) {
                // For combo app widgets: keep wide banner proportions.
                val previewWidth = (300 * density).toInt()
                val previewHeight = (90 * density).toInt()
                val params = widgetPreview.layoutParams
                params.width = previewWidth
                params.height = previewHeight
                widgetPreview.layoutParams = params
                widgetPreview.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                widgetPreview.setPadding(0, 0, 0, 0)
            } else {
                // For larger widgets: use full container size
                val containerSize = (145 * density).toInt()
                val params = widgetPreview.layoutParams
                params.width = containerSize
                params.height = containerSize
                widgetPreview.layoutParams = params
                widgetPreview.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                val padding = (8 * density).toInt()
                widgetPreview.setPadding(padding, padding, padding, padding)
            }
        }
    }
}
