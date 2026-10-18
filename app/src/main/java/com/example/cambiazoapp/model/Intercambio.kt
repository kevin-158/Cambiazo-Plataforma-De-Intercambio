package com.example.cambiazoapp.model

data class Intercambio(
    val id: String = "",
    val productoOfrecidoId: String = "",
    val productoOfrecidoNombre: String = "",
    val productoOfrecidoImagen: String = "",
    val productoSolicitadoId: String = "",
    val productoSolicitadoNombre: String = "",
    val productoSolicitadoImagen: String = "",
    val usuarioSolicitanteId: String = "",
    val usuarioSolicitanteNombre: String = "",
    val usuarioPropietarioId: String = "",
    val usuarioPropietarioNombre: String = "",
    val mensaje: String = "",
    val estado: String = "Pendiente", // "Pendiente", "Aceptada", "Rechazada", "Completada" (RF-17)
    val fechaCreacion: Long = System.currentTimeMillis()
)