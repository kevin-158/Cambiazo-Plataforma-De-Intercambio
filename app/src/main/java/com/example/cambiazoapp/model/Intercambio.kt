package com.example.cambiazoapp.model

/**
 * Modelo de Intercambio enriquecido para soportar:
 * - RF-16: Ultimo mensaje y aviso de mensajes nuevos para cada usuario.
 * - RF-18: Punto de encuentro propuesto/acordado con coordenadas de mapa real.
 * - RF-25: Control de evaluacion bilateral (una sola vez por cada usuario).
 */
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
    val estado: String = "Pendiente", // "Pendiente", "Aceptada", "Rechazada", "Completada"
    val fechaCreacion: Long = System.currentTimeMillis(),
    // RF-16: Control de mensajeria en tiempo real y aviso de nuevos mensajes
    val ultimoMensaje: String = "",
    val ultimoMensajeTimestamp: Long = 0L,
    val ultimoMensajeAutorId: String = "",
    val noLeidosSolicitante: Int = 0,
    val noLeidosPropietario: Int = 0,
    // RF-18: Punto de encuentro seguro en el mapa
    val puntoEncuentroNombre: String = "",
    val puntoEncuentroDireccion: String = "",
    val puntoEncuentroLat: Double = 0.0,
    val puntoEncuentroLng: Double = 0.0,
    val puntoEncuentroPropuestoPor: String = "",
    val puntoEncuentroConfirmado: Boolean = false,
    // RF-25: Control para que cada usuario califique una sola vez al completar
    val calificadoPorSolicitante: Boolean = false,
    val calificadoPorPropietario: Boolean = false
)