package com.example.mibusv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private var users: List<User>,
    private val onDeleteClicked: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvEmailUsers: TextView = itemView.findViewById(R.id.tvEmailUsers)
        val tvTelefonoUsers: TextView = itemView.findViewById(R.id.tvTelefonoUsers)
        val tvfechaRegistroUsers: TextView = itemView.findViewById(R.id.tvfechaRegistroUsers)
        val tvRoleUsers: TextView = itemView.findViewById(R.id.tvRoleUsers)
        val leftStatusDot: View = itemView.findViewById(R.id.leftStatusDot)
        val rightStatusDot: View = itemView.findViewById(R.id.rightStatusDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvName.text = user.nombre
        holder.tvEmailUsers.text = user.email
        holder.tvTelefonoUsers.text = user.telefono
        holder.tvfechaRegistroUsers.text = user.fechaRegistro
        holder.tvRoleUsers.text = user.rol
        holder.tvStatus.text = if (user.estaActivo) "Activo" else "Inactivo"
        holder.leftStatusDot.setBackgroundResource(
            if (user.estaActivo) R.drawable.user_status_dot_active else R.drawable.user_status_dot_inactive
        )
        holder.rightStatusDot.setBackgroundResource(
            if (user.estaActivo) R.drawable.user_status_dot_active else R.drawable.user_status_dot_inactive
        )
    }

    override fun getItemCount(): Int = users.size

    fun submitList(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}


