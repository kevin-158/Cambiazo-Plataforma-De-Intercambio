package com.example.cambiazoapp.model

/**
 * Representa una calificacion y reseña bilateral realizada al completar un trueque (RF-25, RF-26, RF-27).
 */
data class Resena(
    val id: String = "",
    val intercambioId: String = "",
    val autorId: String = "",
    val autorNombre: String = "",
    val autorFotoUrl: String = "",
    val destinatarioId: String = "",
    val estrellas: Int = 5, // Puntaje de 1 a 5 estrellas (RF-26)
    val comentario: String = "", // Comentario cualitativo (RF-26)
    val etiquetas: List<String> = emptyList(), // Chips: "Puntual", "Amigable", etc. (RF-27)
    val articuloIntercambiado: String = "",
    val timestamp: Long = System.currentTimeMillis()
)