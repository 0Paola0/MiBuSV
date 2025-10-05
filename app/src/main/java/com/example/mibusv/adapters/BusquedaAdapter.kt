package com.example.mibusv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mibusv.R

class BusquedaAdapter(
    private var items: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<BusquedaAdapter.SearchableViewHolder>() {

    class SearchableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItem: TextView = itemView.findViewById(R.id.tvSearchItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchableViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_searchable, parent, false)
        return SearchableViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchableViewHolder, position: Int) {
        val item = items[position]
        holder.tvItem.text = item
        
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }
}
