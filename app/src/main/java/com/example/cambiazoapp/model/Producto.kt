package com.example.cambiazoapp.model

/**
 * Modelo de Producto con soporte para multiples fotos (RF-19)
 * y coordenadas geograficas reales para el calculo de distancia en km (RF-09).
 */
data class Producto(
    val id: String = "",
    val usuarioId: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val estado: String = "",
    val imagenUrl: String = "",
    val imagenes: List<String> = emptyList(),
    val interesesCambio: String = "",
    val activo: Boolean = true,
    val latitud: Double = -7.1617, // Coordenada por defecto (Plaza de Armas Cajamarca)
    val longitud: Double = -78.5128,
    val direccionReferencia: String = "Cajamarca Centro"
)