package com.example.miscitasmedicas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DoctorAdapter(
    private var items: List<Doctor>
) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Doctor>) {
        items = newItems
        notifyDataSetChanged()
    }

    class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvEmoji: TextView = itemView.findViewById(R.id.tvDocEmoji)
        private val tvName: TextView = itemView.findViewById(R.id.tvDocName)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvDocSubtitle)

        fun bind(item: Doctor) {
            tvEmoji.text = item.emoji
            tvName.text = item.name
            tvSubtitle.text = item.cmp
        }
    }
}
