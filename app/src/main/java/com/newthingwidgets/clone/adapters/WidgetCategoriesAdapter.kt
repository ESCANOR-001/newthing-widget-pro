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
    }

    // Show Newly Added and Apps cards
    private val itemCount = 2

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_NEWLY_ADDED
            1 -> VIEW_TYPE_APPS
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
        }
    }

    override fun getItemCount(): Int = itemCount

    class NewlyAddedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class AppsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
