package com.example.miscitasmedicas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SpecialtyAdapter(
    private var items: List<Specialty>,
    private val onClick: (Specialty) -> Unit
) : RecyclerView.Adapter<SpecialtyAdapter.SpecialtyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecialtyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_specialty, parent, false)
        return SpecialtyViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpecialtyViewHolder, position: Int) {
        val specialty = items[position]
        holder.bind(specialty)
        holder.itemView.setOnClickListener { onClick(specialty) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Specialty>) {
        items = newItems
        notifyDataSetChanged()
    }

    class SpecialtyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvEmoji: TextView = itemView.findViewById(R.id.tvEmoji)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)

        fun bind(item: Specialty) {
            tvEmoji.text = item.emoji
            tvName.text = item.name
        }
    }
}
