package com.example.mibusv

import com.google.firebase.database.PropertyName

data class Parada(
    val id: String = "",
    val nombreParada: String = "",
    val direccion: String = "",
    val coordenadas: String = "",
    val estaOperativa: Boolean = false,
    val fechaRegistro: String = "",
    @get:PropertyName("role")
    @set:PropertyName("role")
    var rol: String = "Parada"
)
