package com.example.cambiazoapp.model

data class Producto(
    val id: String = "",
    val usuarioId: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val estado: String = "",
    val imagenUrl: String = "",
    val activo: Boolean = true
)