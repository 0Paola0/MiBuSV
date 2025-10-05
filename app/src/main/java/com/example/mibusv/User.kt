package com.example.mibusv

import com.google.firebase.database.PropertyName

data class User(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val fechaRegistro: String = "",
    val estaActivo: Boolean = false,
    @get:PropertyName("role")
    @set:PropertyName("role")
    var rol: String = "Pasajero"
)


