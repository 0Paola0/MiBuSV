package com.example.mibusv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView // Importar CardView

class UserRouteAdapter(
    private val routes: List<Route>,
    private val onItemClicked: (Route) -> Unit // Función lambda para el clic
) : RecyclerView.Adapter<UserRouteAdapter.RouteViewHolder>() {

    class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.routeCard) // Referencia a la tarjeta
        val routeNumber: TextView = itemView.findViewById(R.id.tvRouteNumber)
        val routeName: TextView = itemView.findViewById(R.id.tvRouteName)
        val routeSchedule: TextView = itemView.findViewById(R.id.tvRouteSchedule)
        val routeFrequency: TextView = itemView.findViewById(R.id.tvRouteFrequency)
        val routeStops: TextView = itemView.findViewById(R.id.tvRouteStops)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_route_user, parent, false)
        return RouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val route = routes[position]
        holder.routeNumber.text = route.numeroRuta
        holder.routeName.text = route.nombreRuta
        holder.routeSchedule.text = "Horario: ${route.horarioOperacion}"
        holder.routeFrequency.text = "Frecuencia: ${route.frecuencia}" // Asumiendo que tienes 'frecuencia' en Route.kt
        holder.routeStops.text = "Inicio: ${route.paradaInicial} - Fin: ${route.paradaFinal}"

        // Configurar el clic en la tarjeta
        holder.cardView.setOnClickListener {
            onItemClicked(route)
        }
    }

    override fun getItemCount() = routes.size
}