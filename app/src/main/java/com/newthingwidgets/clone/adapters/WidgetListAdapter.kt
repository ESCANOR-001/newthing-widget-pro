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

class WidgetListAdapter(
    private val widgets: List<WidgetItem>
) : RecyclerView.Adapter<WidgetListAdapter.WidgetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_widget_preview, parent, false)
        return WidgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {
        val widget = widgets[position]
        holder.bind(widget)
        
        holder.itemView.setOnClickListener { view ->
            val context = view.context
            val appWidgetManager = AppWidgetManager.getInstance(context)
            
            // Determine which provider to use based on widget size
            val componentName = if (widget.size == "1x1") {
                // App launcher widgets
                ComponentName(context, com.newthingwidgets.clone.widgets.AppLauncherWidgetProvider::class.java)
            } else {
                // Analog clock widgets
                ComponentName(context, AnalogClockWidgetProvider::class.java)
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
                            prefs.edit()
                                .putString("pending_app_name", widget.name)
                                .putInt("pending_drawable", appInfo.drawableRes)
                                .apply()
                            
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
            widgetPreview.setImageResource(widget.previewDrawable)
            widgetName.text = widget.name
            widgetSize.text = widget.size
            
            // Enforce uniform sizing for all app icons
            val context = itemView.context
            val density = context.resources.displayMetrics.density
            
            if (widget.size == "1x1") {
                // For 1x1 app icons: fixed size with consistent padding
                val iconSize = (80 * density).toInt() // Fixed icon display size
                val params = widgetPreview.layoutParams
                params.width = iconSize
                params.height = iconSize
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
                val padding = (16 * density).toInt()
                widgetPreview.setPadding(padding, padding, padding, padding)
            }
        }
    }
}
