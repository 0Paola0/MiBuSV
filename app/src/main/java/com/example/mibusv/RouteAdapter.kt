package com.example.mibusv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RouteAdapter(
    private var routes: List<Route>,
    private val onViewDetailsClicked: (Route) -> Unit,
    private val onDeleteClicked: (Route) -> Unit
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNumeroRuta: TextView = itemView.findViewById(R.id.tvNumeroRuta)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvNombreRuta: TextView = itemView.findViewById(R.id.tvNombreRuta)
        val tvParadaInicial: TextView = itemView.findViewById(R.id.tvParadaInicial)
        val tvParadasIntermedias: TextView = itemView.findViewById(R.id.tvParadasIntermedias)
        val tvParadaFinal: TextView = itemView.findViewById(R.id.tvParadaFinal)
        val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecio)
        val tvHorario: TextView = itemView.findViewById(R.id.tvHorario)
        val btnViewDetails: Button = itemView.findViewById(R.id.btnViewDetails)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val rightStatusDot: View = itemView.findViewById(R.id.rightStatusDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rutas, parent, false)
        return RouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        try {
            if (position >= routes.size) {
                android.util.Log.w("RouteAdapter", "Position $position is out of bounds for list size ${routes.size}")
                return
            }
            
            val route = routes[position]

            holder.tvNumeroRuta.text = "Ruta ${route.numeroRuta.ifEmpty { "Sin número" }}"
            holder.tvNombreRuta.text = route.nombreRuta.ifEmpty { "Sin nombre" }
            
            // Mostrar información de paradas
            holder.tvParadaInicial.text = route.paradaInicial.ifEmpty { "No especificada" }
            holder.tvParadasIntermedias.text = route.paradasIntermedias.ifEmpty { "Ninguna" }
            holder.tvParadaFinal.text = route.paradaFinal.ifEmpty { "No especificada" }
            
            holder.tvPrecio.text = "$${route.precio.ifEmpty { "0.00" }}"
            holder.tvHorario.text = route.horarioOperacion.ifEmpty { "Sin horario" }
            holder.tvStatus.text = if (route.estaOperativa) "Activa" else "Inactiva"

            holder.rightStatusDot.setBackgroundResource(
                if (route.estaOperativa) R.drawable.user_status_dot_active else R.drawable.user_status_dot_inactive
            )

            holder.btnViewDetails.setOnClickListener { onViewDetailsClicked(route) }
            holder.btnDelete.setOnClickListener { onDeleteClicked(route) }
        } catch (e: Exception) {
            android.util.Log.e("RouteAdapter", "Error al bind ruta en posición $position: ${e.message}")
        }
    }

    override fun getItemCount(): Int = routes.size

    fun submitList(newRoutes: List<Route>) {
        try {
            routes = newRoutes ?: emptyList()
            notifyDataSetChanged()
        } catch (e: Exception) {
            android.util.Log.e("RouteAdapter", "Error al actualizar lista: ${e.message}")
            routes = emptyList()
            notifyDataSetChanged()
        }
    }
}