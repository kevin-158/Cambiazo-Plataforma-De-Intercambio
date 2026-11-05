package com.example.cambiazoapp.model

/**
 * Modelo de datos del Usuario en Firestore.
 * Incluye foto de perfil, telefono y biografia para la edicion de perfil.
 */
data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val ciudad: String = "",
    val barrio: String = "",
    val telefono: String = "",
    val biografia: String = "",
    val fotoUrl: String = ""
)