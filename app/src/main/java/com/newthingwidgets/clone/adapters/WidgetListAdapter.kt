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
import com.newthingwidgets.clone.widgets.AnalogClockProvider

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
            
            // All analog clocks use the same provider
            val componentName = ComponentName(context, AnalogClockProvider::class.java)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    appWidgetManager.requestPinAppWidget(componentName, null, null)
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
        }
    }
}
