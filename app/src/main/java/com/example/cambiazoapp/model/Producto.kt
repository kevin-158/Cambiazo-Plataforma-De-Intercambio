package com.example.cambiazoapp.model

data class Producto(
    val id: String = "",
    val usuarioId: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val estado: String = "",
    val imagenUrl: String = "", // Funciona como la URL de la Foto de Portada (RF-19)
    val imagenes: List<String> = emptyList(), // Lista completa de fotos para la galería (RF-19, RF-10)
    val interesesCambio: String = "", // Lo que busca a cambio (RF-21 / Pantallas 3, 4 y 7)
    val activo: Boolean = true
)