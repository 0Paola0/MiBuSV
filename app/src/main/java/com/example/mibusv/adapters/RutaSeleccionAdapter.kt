package com.example.mibusv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mibusv.R
import com.example.mibusv.Route

class RutaSeleccionAdapter(
    private var rutas: List<Route>,
    private val onRutaSeleccionada: (Route, Boolean) -> Unit
) : RecyclerView.Adapter<RutaSeleccionAdapter.RutaSeleccionViewHolder>() {

    private val rutasSeleccionadas = mutableSetOf<String>()

    class RutaSeleccionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNumeroRuta: TextView = itemView.findViewById(R.id.tvNumeroRuta)
        val tvNombreRuta: TextView = itemView.findViewById(R.id.tvNombreRuta)
        val cbSeleccionar: CheckBox = itemView.findViewById(R.id.cbSeleccionar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RutaSeleccionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ruta_seleccion, parent, false)
        return RutaSeleccionViewHolder(view)
    }

    override fun onBindViewHolder(holder: RutaSeleccionViewHolder, position: Int) {
        val ruta = rutas[position]

        holder.tvNumeroRuta.text = "Ruta ${ruta.numeroRuta}"
        holder.tvNombreRuta.text = ruta.nombreRuta
        holder.cbSeleccionar.isChecked = rutasSeleccionadas.contains(ruta.id)

        holder.cbSeleccionar.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rutasSeleccionadas.add(ruta.id)
            } else {
                rutasSeleccionadas.remove(ruta.id)
            }
            onRutaSeleccionada(ruta, isChecked)
        }
    }

    override fun getItemCount(): Int = rutas.size

    fun submitList(newRutas: List<Route>) {
        rutas = newRutas
        notifyDataSetChanged()
    }

    fun getRutasSeleccionadas(): List<Route> {
        return rutas.filter { rutasSeleccionadas.contains(it.id) }
    }

    fun getRutasSeleccionadasTexto(): String {
        val rutasSeleccionadas = getRutasSeleccionadas()
        return if (rutasSeleccionadas.isEmpty()) {
            "Ninguna"
        } else {
            rutasSeleccionadas.joinToString(", ") { "Ruta ${it.numeroRuta}" }
        }
    }
}
