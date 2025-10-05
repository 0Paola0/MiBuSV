package com.example.mibusv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ConductorAdapter(
    private var conductors: List<Conductor>,
    private val onViewDetailsClicked: (Conductor) -> Unit,
    private val onDeleteClicked: (Conductor) -> Unit
) : RecyclerView.Adapter<ConductorAdapter.ConductorViewHolder>() {

    class ConductorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvConductorName: TextView = itemView.findViewById(R.id.tvConductorName)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val btnViewDetails: Button = itemView.findViewById(R.id.btnViewDetails)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val tvEmailValue: TextView = itemView.findViewById(R.id.tvEmailValue)
        val tvTelefono: TextView = itemView.findViewById(R.id.tvTelefono)
        val tvRutasLabel: TextView = itemView.findViewById(R.id.tvRutasLabel)
        val tvRegistro: TextView = itemView.findViewById(R.id.tvRegistro)
        val tvRoleConductor: TextView = itemView.findViewById(R.id.tvRoleConductor)
        val leftStatusDot: View = itemView.findViewById(R.id.leftStatusDot)
        val rightStatusDot: View = itemView.findViewById(R.id.rightStatusDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConductorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_conductores, parent, false)
        return ConductorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConductorViewHolder, position: Int) {
        try {
            if (position >= conductors.size) {
                android.util.Log.w("ConductorAdapter", "Position $position is out of bounds for list size ${conductors.size}")
                return
            }
            
            val conductor = conductors[position]

            holder.tvConductorName.text = conductor.nombreConductor.ifEmpty { "Sin nombre" }
            holder.tvEmailValue.text = conductor.email.ifEmpty { "Sin email" }
            holder.tvTelefono.text = conductor.telefono.ifEmpty { "Sin teléfono" }
            holder.tvRutasLabel.text = conductor.rutas.ifEmpty { "Sin rutas asignadas" }
            holder.tvRegistro.text = conductor.fechaRegistro.ifEmpty { "Sin fecha" }
            holder.tvRoleConductor.text = conductor.rol.ifEmpty { "Conductor" }
            holder.tvStatus.text = if (conductor.estaOperativo) "Activo" else "Inactivo"

            holder.leftStatusDot.setBackgroundResource(
                if (conductor.estaOperativo) R.drawable.user_status_dot_active else R.drawable.user_status_dot_inactive
            )
            holder.rightStatusDot.setBackgroundResource(
                if (conductor.estaOperativo) R.drawable.user_status_dot_active else R.drawable.user_status_dot_inactive
            )

            holder.btnViewDetails.setOnClickListener { onViewDetailsClicked(conductor) }
            holder.btnDelete.setOnClickListener { onDeleteClicked(conductor) }
        } catch (e: Exception) {
            android.util.Log.e("ConductorAdapter", "Error al bind conductor en posición $position: ${e.message}")
        }
    }

    override fun getItemCount(): Int = conductors.size

    fun submitList(newConductors: List<Conductor>) {
        try {
            conductors = newConductors ?: emptyList()
            notifyDataSetChanged()
        } catch (e: Exception) {
            android.util.Log.e("ConductorAdapter", "Error al actualizar lista: ${e.message}")
            conductors = emptyList()
            notifyDataSetChanged()
        }
    }
}
