package com.newthingwidgets.clone.contacts

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.newthingwidgets.clone.R
import com.newthingwidgets.clone.widgets.SelectedContact
import java.util.Locale

class ContactSelectionAdapter(
    private val onContactClick: (SelectedContact) -> Unit
) : RecyclerView.Adapter<ContactSelectionAdapter.ContactViewHolder>() {

    private val allContacts = mutableListOf<SelectedContact>()
    private val filteredContacts = mutableListOf<SelectedContact>()

    fun submitContacts(contacts: List<SelectedContact>) {
        allContacts.clear()
        allContacts.addAll(contacts)

        filteredContacts.clear()
        filteredContacts.addAll(contacts)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val normalized = query.trim().lowercase(Locale.getDefault())
        filteredContacts.clear()

        if (normalized.isEmpty()) {
            filteredContacts.addAll(allContacts)
        } else {
            filteredContacts.addAll(
                allContacts.filter { contact ->
                    contact.name.lowercase(Locale.getDefault()).contains(normalized) ||
                        contact.phone.orEmpty().contains(normalized)
                }
            )
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = filteredContacts[position]
        holder.name.text = contact.name
        holder.phone.text = contact.phone.orEmpty()

        if (contact.photoUri.isNullOrBlank()) {
            holder.avatar.setImageResource(R.drawable.ic_person)
        } else {
            try {
                holder.avatar.setImageURI(Uri.parse(contact.photoUri))
            } catch (error: Exception) {
                holder.avatar.setImageResource(R.drawable.ic_person)
                Log.e("ContactSelectionAdapter", "Unable to load contact photo", error)
            }
        }

        holder.itemView.setOnClickListener {
            onContactClick(contact)
        }
    }

    override fun getItemCount(): Int = filteredContacts.size

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ShapeableImageView = itemView.findViewById(R.id.avatar_image)
        val name: TextView = itemView.findViewById(R.id.contact_name)
        val phone: TextView = itemView.findViewById(R.id.contact_phone)
    }
}