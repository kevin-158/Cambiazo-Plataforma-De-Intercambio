package com.example.cambiazoapp.model
/**
 * Representa un mensaje individual en tiempo real dentro de una negociacion (RF-16).
 * Tambien puede representar un mensaje especial con punto de encuentro en el mapa (RF-18).
 */
data class MensajeChat(
    val id: String = "",
    val intercambioId: String = "",
    val remitenteId: String = "",
    val remitenteNombre: String = "",
    val texto: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val esPuntoEncuentro: Boolean = false,
    val nombreLugar: String = "",
    val direccionLugar: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)