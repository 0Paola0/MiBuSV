package com.example.mibusv

import com.google.firebase.database.PropertyName

data class Conductor(
    val id: String = "",
    val nombreConductor: String = "",
    val email: String = "",
    val telefono: String = "",
    val rutas: String = "",
    val fechaRegistro: String = "",
    val estaOperativo: Boolean = false,
    @get:PropertyName("role")
    @set:PropertyName("role")
    var rol: String = "Conductor"
)
