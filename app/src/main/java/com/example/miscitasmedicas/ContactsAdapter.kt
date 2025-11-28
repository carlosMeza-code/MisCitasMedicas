package com.example.miscitasmedicas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ContactsAdapter : ListAdapter<Contact, ContactsAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvContactName)
        private val tvRelationship: TextView = itemView.findViewById(R.id.tvContactRelationship)
        private val tvPhone: TextView = itemView.findViewById(R.id.tvContactPhone)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvContactEmail)

        fun bind(contact: Contact) {
            tvName.text = contact.fullName
            tvRelationship.text = contact.relationship
            tvPhone.text = contact.phone
            tvEmail.text = contact.email
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean =
            oldItem == newItem
    }
}
