package com.example.mibusv.utils

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable

class SearchableSpinnerAdapter(
    context: Context,
    resource: Int,
    private val items: MutableList<String>
) : ArrayAdapter<String>(context, resource, items), Filterable {

    private var filteredItems: MutableList<String> = items.toMutableList()
    private var originalItems: MutableList<String> = items.toMutableList()

    override fun getCount(): Int = filteredItems.size

    override fun getItem(position: Int): String = filteredItems[position]

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val filteredList = mutableListOf<String>()

                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(originalItems)
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    for (item in originalItems) {
                        if (item.lowercase().contains(filterPattern)) {
                            filteredList.add(item)
                        }
                    }
                }

                results.values = filteredList
                results.count = filteredList.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredItems.clear()
                if (results != null) {
                    @Suppress("UNCHECKED_CAST")
                    filteredItems.addAll(results.values as List<String>)
                }
                notifyDataSetChanged()
            }
        }
    }

    fun updateItems(newItems: List<String>) {
        originalItems.clear()
        originalItems.addAll(newItems)
        filteredItems.clear()
        filteredItems.addAll(newItems)
        notifyDataSetChanged()
    }
}
