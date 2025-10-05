package com.example.mibusv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ParadaAdapter(
    private var paradas: List<Parada>,
    private val onViewDetailsClicked: (Parada) -> Unit,
    private val onDeleteClicked: (Parada) -> Unit
) : RecyclerView.Adapter<ParadaAdapter.ParadaViewHolder>() {

    class ParadaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreParada: TextView = itemView.findViewById(R.id.tvNombreParada)
        val btnViewDetails: Button = itemView.findViewById(R.id.btnViewDetails)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val tvDireccion: TextView = itemView.findViewById(R.id.tvDireccion)
        val tvCoordenadas: TextView = itemView.findViewById(R.id.tvCoordenadas)
        val tvFechaRegistro: TextView = itemView.findViewById(R.id.tvFechaRegistro)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParadaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_paradas, parent, false)
        return ParadaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParadaViewHolder, position: Int) {
        try {
            if (position >= paradas.size) {
                android.util.Log.w("ParadaAdapter", "Position $position is out of bounds for list size ${paradas.size}")
                return
            }
            
            val parada = paradas[position]

            holder.tvNombreParada.text = parada.nombreParada.ifEmpty { "Sin nombre" }
            holder.tvDireccion.text = parada.direccion.ifEmpty { "Sin dirección" }
            holder.tvCoordenadas.text = parada.coordenadas.ifEmpty { "Sin coordenadas" }
            holder.tvFechaRegistro.text = parada.fechaRegistro.ifEmpty { "Sin fecha" }

            holder.btnViewDetails.setOnClickListener { onViewDetailsClicked(parada) }
            holder.btnDelete.setOnClickListener { onDeleteClicked(parada) }
        } catch (e: Exception) {
            android.util.Log.e("ParadaAdapter", "Error al bind parada en posición $position: ${e.message}")
        }
    }

    override fun getItemCount(): Int = paradas.size

    fun submitList(newParadas: List<Parada>) {
        try {
            paradas = newParadas ?: emptyList()
            notifyDataSetChanged()
        } catch (e: Exception) {
            android.util.Log.e("ParadaAdapter", "Error al actualizar lista: ${e.message}")
            paradas = emptyList()
            notifyDataSetChanged()
        }
    }
}
