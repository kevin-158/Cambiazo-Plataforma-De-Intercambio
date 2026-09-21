package com.example.cambiazoapp.model

data class Intercambio(
    val id: String = "",
    val productoOfrecidoId: String = "",
    val productoSolicitadoId: String = "",
    val usuarioSolicitanteId: String = "",
    val usuarioPropietarioId: String = "",
    val estado: String = ""
)