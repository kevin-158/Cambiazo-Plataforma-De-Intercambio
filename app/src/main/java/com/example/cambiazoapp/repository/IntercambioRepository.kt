package com.example.cambiazoapp.repository

import com.example.cambiazoapp.model.Intercambio
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio encargado de gestionar las operaciones en Cloud Firestore
 * para la coleccion "intercambios" (Cumple con RF-13 y RF-17).
 */
class IntercambioRepository {

    // Instancia de la base de datos Firestore
    private val db = FirebaseFirestore.getInstance()

    /**
     * Funcion auxiliar que convierte un documento de Firestore en un objeto [Intercambio].
     */
    private fun mapearDocumentoAIntercambio(doc: DocumentSnapshot): Intercambio {
        return Intercambio(
            id = doc.id,
            productoOfrecidoId = doc.getString("productoOfrecidoId") ?: "",
            productoOfrecidoNombre = doc.getString("productoOfrecidoNombre") ?: "",
            productoOfrecidoImagen = doc.getString("productoOfrecidoImagen") ?: "",
            productoSolicitadoId = doc.getString("productoSolicitadoId") ?: "",
            productoSolicitadoNombre = doc.getString("productoSolicitadoNombre") ?: "",
            productoSolicitadoImagen = doc.getString("productoSolicitadoImagen") ?: "",
            usuarioSolicitanteId = doc.getString("usuarioSolicitanteId") ?: "",
            usuarioSolicitanteNombre = doc.getString("usuarioSolicitanteNombre") ?: "",
            usuarioPropietarioId = doc.getString("usuarioPropietarioId") ?: "",
            usuarioPropietarioNombre = doc.getString("usuarioPropietarioNombre") ?: "",
            mensaje = doc.getString("mensaje") ?: "",
            estado = doc.getString("estado") ?: "Pendiente",
            fechaCreacion = doc.getLong("fechaCreacion") ?: 0L
        )
    }

    /**
     * RF-13: Registra una nueva propuesta de trueque en Firestore con estado inicial "Pendiente".
     * Incluye validacion previa para evitar enviar dos veces la misma propuesta pendiente.
     */
    suspend fun crearPropuestaIntercambio(intercambio: Intercambio): Result<Boolean> {
        return try {
            // Validacion: Verificar si ya existe una propuesta "Pendiente" para estos mismos articulos
            val duplicados = db.collection("intercambios")
                .whereEqualTo("usuarioSolicitanteId", intercambio.usuarioSolicitanteId)
                .whereEqualTo("productoSolicitadoId", intercambio.productoSolicitadoId)
                .whereEqualTo("estado", "Pendiente")
                .get()
                .await()

            if (!duplicados.isEmpty) {
                return Result.failure(
                    Exception("Ya tienes una propuesta pendiente para este artículo.")
                )
            }

            // Mapa de datos que se guardara en la coleccion "intercambios"
            val datos = hashMapOf(
                "productoOfrecidoId" to intercambio.productoOfrecidoId,
                "productoOfrecidoNombre" to intercambio.productoOfrecidoNombre,
                "productoOfrecidoImagen" to intercambio.productoOfrecidoImagen,
                "productoSolicitadoId" to intercambio.productoSolicitadoId,
                "productoSolicitadoNombre" to intercambio.productoSolicitadoNombre,
                "productoSolicitadoImagen" to intercambio.productoSolicitadoImagen,
                "usuarioSolicitanteId" to intercambio.usuarioSolicitanteId,
                "usuarioSolicitanteNombre" to intercambio.usuarioSolicitanteNombre,
                "usuarioPropietarioId" to intercambio.usuarioPropietarioId,
                "usuarioPropietarioNombre" to intercambio.usuarioPropietarioNombre,
                "mensaje" to intercambio.mensaje,
                "estado" to "Pendiente", // RF-13: Siempre inicia como Pendiente
                "fechaCreacion" to System.currentTimeMillis()
            )

            db.collection("intercambios").add(datos).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * RF-17: Obtiene las propuestas de trueque que otros usuarios me han enviado
     * (donde yo soy el dueño del articulo solicitado: usuarioPropietarioId).
     */
    suspend fun obtenerPropuestasRecibidas(usuarioId: String): Result<List<Intercambio>> {
        return try {
            val snapshot = db.collection("intercambios")
                .whereEqualTo("usuarioPropietarioId", usuarioId)
                .get()
                .await()

            val lista = snapshot.documents
                .map { mapearDocumentoAIntercambio(it) }
                .sortedByDescending { it.fechaCreacion }

            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * RF-17: Obtiene las propuestas de trueque que yo he enviado a otros usuarios
     * (donde yo soy el usuarioSolicitanteId).
     */
    suspend fun obtenerPropuestasEnviadas(usuarioId: String): Result<List<Intercambio>> {
        return try {
            val snapshot = db.collection("intercambios")
                .whereEqualTo("usuarioSolicitanteId", usuarioId)
                .get()
                .await()

            val lista = snapshot.documents
                .map { mapearDocumentoAIntercambio(it) }
                .sortedByDescending { it.fechaCreacion }

            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * RF-17: Actualiza el estado de una propuesta ("Pendiente", "Aceptada", "Rechazada", "Completada").
     */
    suspend fun actualizarEstadoIntercambio(
        intercambioId: String,
        nuevoEstado: String
    ): Result<Boolean> {
        return try {
            db.collection("intercambios")
                .document(intercambioId)
                .update("estado", nuevoEstado)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}