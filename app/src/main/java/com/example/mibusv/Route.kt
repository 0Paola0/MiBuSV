package com.example.mibusv

import com.google.firebase.database.PropertyName

data class Route(
    val id: String = "",
    val numeroRuta: String = "",
    val nombreRuta: String = "",
    val distancia: String = "",
    val duracion: String = "",
    val frecuencia: String = "",
    val paradaInicial: String = "",
    val paradasIntermedias: String = "",
    val paradaFinal: String = "",
    val precio: String = "",
    val horarioOperacion: String = "",
    val estaOperativa: Boolean = false,
    val fechaRegistro: String = "",
    @get:PropertyName("role")
    @set:PropertyName("role")
    var rol: String = "Ruta"
)


