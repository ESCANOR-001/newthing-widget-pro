package com.newthingwidgets.clone.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.WidgetDetailActivity

class WidgetCategoriesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_NEWLY_ADDED = 0
        private const val VIEW_TYPE_APPS = 1
        private const val VIEW_TYPE_BATTERY = 2
    }

    // Show Newly Added, Apps, and Battery cards
    private val itemCount = 3

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_NEWLY_ADDED
            1 -> VIEW_TYPE_APPS
            2 -> VIEW_TYPE_BATTERY
            else -> VIEW_TYPE_NEWLY_ADDED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_APPS -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_apps_card, parent, false)
                AppsViewHolder(view)
            }
            VIEW_TYPE_BATTERY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_battery_card, parent, false)
                BatteryViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_newly_added_card, parent, false)
                NewlyAddedViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NewlyAddedViewHolder -> {
                holder.itemView.setOnClickListener { view ->
                    val context = view.context
                    val intent = Intent(context, WidgetDetailActivity::class.java).apply {
                        putExtra(WidgetDetailActivity.EXTRA_CATEGORY_NAME, "Newly Added")
                    }
                    context.startActivity(intent)
                }
            }
            is AppsViewHolder -> {
                holder.itemView.setOnClickListener { view ->
                    val context = view.context
                    val intent = Intent(context, WidgetDetailActivity::class.java).apply {
                        putExtra(WidgetDetailActivity.EXTRA_CATEGORY_NAME, "Apps")
                    }
                    context.startActivity(intent)
                }
            }
            is BatteryViewHolder -> {
                holder.bind()
                holder.itemView.setOnClickListener { view ->
                    val context = view.context
                    val intent = Intent(context, WidgetDetailActivity::class.java).apply {
                        putExtra(WidgetDetailActivity.EXTRA_CATEGORY_NAME, "Battery")
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int = itemCount

    class NewlyAddedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class AppsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class BatteryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val batteryPreview: android.widget.ImageView = itemView.findViewById(R.id.battery_preview)
        
        fun bind() {
            // Dynamically render Battery Meter widget as preview
            val context = itemView.context
            try {
                val bitmap = com.newthingwidgets.clone.utils.LayoutToBitmapRenderer.renderWidgetPreview(
                    context,
                    "Battery Meter",
                    targetSizeDp = 150
                )
                if (bitmap != null) {
                    batteryPreview.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                // Fallback to static drawable
                batteryPreview.setImageResource(R.drawable.bat_preview)
            }
        }
    }
}
