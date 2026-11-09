package com.example.cambiazoapp.model

/**
 * Modelo de datos del Usuario en Firestore.
 * Incluye foto de perfil, telefono y biografia para la edicion de perfil.
 */
/**
 * Modelo de Usuario con soporte para reputacion en estrellas (RF-11, RF-26)
 * y coordenadas de ubicacion para calcular distancia real en km (RF-09).
 */
data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val ciudad: String = "",
    val barrio: String = "",
    val telefono: String = "",
    val biografia: String = "",
    val fotoUrl: String = "",
    val promedioEstrellas: Double = 5.0,
    val totalResenas: Int = 0,
    val truequesCompletados: Int = 0,
    val latitud: Double = -7.1617,
    val longitud: Double = -78.5128,
    val favoritosIds: List<String> = emptyList() // RF-12: IDs de artículos guardados en favoritos
)