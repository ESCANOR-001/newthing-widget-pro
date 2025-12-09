package com.newthingwidgets.clone.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.WidgetDetailActivity

class WidgetCategoriesAdapter : RecyclerView.Adapter<WidgetCategoriesAdapter.NewlyAddedViewHolder>() {

    // Only show Newly Added card for now
    private val itemCount = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewlyAddedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_newly_added_card, parent, false)
        return NewlyAddedViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewlyAddedViewHolder, position: Int) {
        holder.itemView.setOnClickListener { view ->
            val context = view.context
            val intent = Intent(context, WidgetDetailActivity::class.java).apply {
                putExtra(WidgetDetailActivity.EXTRA_CATEGORY_NAME, "Newly Added")
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = itemCount

    class NewlyAddedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
