package com.example.cambiazoapp.repository

import com.example.cambiazoapp.model.Intercambio
import com.example.cambiazoapp.model.MensajeChat
import com.example.cambiazoapp.model.Resena
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para gestionar Propuestas (RF-13, RF-17), Mensajeria en Tiempo Real (RF-16),
 * Puntos de Encuentro en Mapa Real (RF-18) y Evaluaciones Bilaterales (RF-25, RF-26, RF-27).
 */
class IntercambioRepository {

    private val db = FirebaseFirestore.getInstance()

    /**
     * Convierte un documento de Firestore en un objeto [Intercambio] completo.
     */
    fun mapearDocumentoAIntercambio(doc: DocumentSnapshot): Intercambio {
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
            fechaCreacion = doc.getLong("fechaCreacion") ?: 0L,
            ultimoMensaje = doc.getString("ultimoMensaje") ?: (doc.getString("mensaje") ?: ""),
            ultimoMensajeTimestamp = doc.getLong("ultimoMensajeTimestamp") ?: (doc.getLong("fechaCreacion") ?: 0L),
            ultimoMensajeAutorId = doc.getString("ultimoMensajeAutorId") ?: "",
            noLeidosSolicitante = (doc.getLong("noLeidosSolicitante") ?: 0L).toInt(),
            noLeidosPropietario = (doc.getLong("noLeidosPropietario") ?: 0L).toInt(),
            puntoEncuentroNombre = doc.getString("puntoEncuentroNombre") ?: "",
            puntoEncuentroDireccion = doc.getString("puntoEncuentroDireccion") ?: "",
            puntoEncuentroLat = doc.getDouble("puntoEncuentroLat") ?: 0.0,
            puntoEncuentroLng = doc.getDouble("puntoEncuentroLng") ?: 0.0,
            puntoEncuentroPropuestoPor = doc.getString("puntoEncuentroPropuestoPor") ?: "",
            puntoEncuentroConfirmado = doc.getBoolean("puntoEncuentroConfirmado") ?: false,
            calificadoPorSolicitante = doc.getBoolean("calificadoPorSolicitante") ?: false,
            calificadoPorPropietario = doc.getBoolean("calificadoPorPropietario") ?: false
        )
    }

    /**
     * RF-13: Crea una propuesta de trueque y guarda el primer mensaje en la subcoleccion del chat (RF-16).
     */
    suspend fun crearPropuestaIntercambio(intercambio: Intercambio): Result<Boolean> {
        return try {
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

            val ahora = System.currentTimeMillis()
            val textoInicial = intercambio.mensaje.ifBlank {
                "¡Hola! Te propongo cambiar mi ${intercambio.productoOfrecidoNombre} por tu ${intercambio.productoSolicitadoNombre}."
            }

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
                "estado" to "Pendiente",
                "fechaCreacion" to ahora,
                "ultimoMensaje" to textoInicial,
                "ultimoMensajeTimestamp" to ahora,
                "ultimoMensajeAutorId" to intercambio.usuarioSolicitanteId,
                "noLeidosSolicitante" to 0,
                "noLeidosPropietario" to 1, // Aviso de mensaje nuevo para el dueño (RF-16)
                "puntoEncuentroNombre" to "",
                "puntoEncuentroDireccion" to "",
                "puntoEncuentroLat" to 0.0,
                "puntoEncuentroLng" to 0.0,
                "puntoEncuentroPropuestoPor" to "",
                "puntoEncuentroConfirmado" to false,
                "calificadoPorSolicitante" to false,
                "calificadoPorPropietario" to false
            )

            val docRef = db.collection("intercambios").add(datos).await()

            // Guardar el mensaje inicial dentro del historial del chat en tiempo real (RF-16)
            val primerMensaje = hashMapOf(
                "intercambioId" to docRef.id,
                "remitenteId" to intercambio.usuarioSolicitanteId,
                "remitenteNombre" to intercambio.usuarioSolicitanteNombre,
                "texto" to textoInicial,
                "timestamp" to ahora,
                "esPuntoEncuentro" to false,
                "nombreLugar" to "",
                "direccionLugar" to "",
                "latitud" to 0.0,
                "longitud" to 0.0
            )
            docRef.collection("mensajes").add(primerMensaje).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerPropuestasRecibidas(usuarioId: String): Result<List<Intercambio>> {
        return try {
            val snapshot = db.collection("intercambios")
                .whereEqualTo("usuarioPropietarioId", usuarioId)
                .get()
                .await()

            val lista = snapshot.documents
                .map { mapearDocumentoAIntercambio(it) }
                .sortedByDescending { maxOf(it.ultimoMensajeTimestamp, it.fechaCreacion) }

            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerPropuestasEnviadas(usuarioId: String): Result<List<Intercambio>> {
        return try {
            val snapshot = db.collection("intercambios")
                .whereEqualTo("usuarioSolicitanteId", usuarioId)
                .get()
                .await()

            val lista = snapshot.documents
                .map { mapearDocumentoAIntercambio(it) }
                .sortedByDescending { maxOf(it.ultimoMensajeTimestamp, it.fechaCreacion) }

            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    /**
     * RF-16: Escucha en tiempo real los cambios del documento de intercambio (estado, punto de encuentro, etc.).
     */
    fun escucharIntercambioTiempoReal(
        intercambioId: String,
        onCambio: (Intercambio?) -> Unit
    ): ListenerRegistration {
        return db.collection("intercambios")
            .document(intercambioId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    onCambio(null)
                    return@addSnapshotListener
                }
                onCambio(mapearDocumentoAIntercambio(snapshot))
            }
    }

    /**
     * RF-16: Escucha en tiempo real todos los mensajes de la negociacion ordenados cronologicamente.
     */
    fun escucharMensajesTiempoReal(
        intercambioId: String,
        onMensajesActualizados: (List<MensajeChat>) -> Unit
    ): ListenerRegistration {
        return db.collection("intercambios")
            .document(intercambioId)
            .collection("mensajes")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onMensajesActualizados(emptyList())
                    return@addSnapshotListener
                }

                val lista = snapshot.documents.map { doc ->
                    MensajeChat(
                        id = doc.id,
                        intercambioId = doc.getString("intercambioId") ?: intercambioId,
                        remitenteId = doc.getString("remitenteId") ?: "",
                        remitenteNombre = doc.getString("remitenteNombre") ?: "",
                        texto = doc.getString("texto") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        esPuntoEncuentro = doc.getBoolean("esPuntoEncuentro") ?: false,
                        nombreLugar = doc.getString("nombreLugar") ?: "",
                        direccionLugar = doc.getString("direccionLugar") ?: "",
                        latitud = doc.getDouble("latitud") ?: 0.0,
                        longitud = doc.getDouble("longitud") ?: 0.0
                    )
                }.sortedBy { it.timestamp }

                onMensajesActualizados(lista)
            }
    }

    /**
     * RF-16: Envia un mensaje en tiempo real y actualiza el contador de mensajes no leidos del otro usuario.
     */
    suspend fun enviarMensajeChat(
        intercambio: Intercambio,
        remitenteId: String,
        remitenteNombre: String,
        texto: String
    ): Result<Boolean> {
        return try {
            val ahora = System.currentTimeMillis()
            val datosMensaje = hashMapOf(
                "intercambioId" to intercambio.id,
                "remitenteId" to remitenteId,
                "remitenteNombre" to remitenteNombre,
                "texto" to texto.trim(),
                "timestamp" to ahora,
                "esPuntoEncuentro" to false,
                "nombreLugar" to "",
                "direccionLugar" to "",
                "latitud" to 0.0,
                "longitud" to 0.0
            )

            val refIntercambio = db.collection("intercambios").document(intercambio.id)
            refIntercambio.collection("mensajes").add(datosMensaje).await()

            val soySolicitante = remitenteId == intercambio.usuarioSolicitanteId
            val actualizacion = if (soySolicitante) {
                mapOf(
                    "ultimoMensaje" to texto.trim(),
                    "ultimoMensajeTimestamp" to ahora,
                    "ultimoMensajeAutorId" to remitenteId,
                    "noLeidosPropietario" to (intercambio.noLeidosPropietario + 1)
                )
            } else {
                mapOf(
                    "ultimoMensaje" to texto.trim(),
                    "ultimoMensajeTimestamp" to ahora,
                    "ultimoMensajeAutorId" to remitenteId,
                    "noLeidosSolicitante" to (intercambio.noLeidosSolicitante + 1)
                )
            }

            refIntercambio.update(actualizacion).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * RF-16: Marca como leidos los mensajes pendientes cuando el usuario abre el chat.
     */
    suspend fun marcarMensajesComoLeidos(intercambio: Intercambio, usuarioActualId: String) {
        try {
            val campoActualizar = if (usuarioActualId == intercambio.usuarioSolicitanteId) {
                "noLeidosSolicitante"
            } else {
                "noLeidosPropietario"
            }
            db.collection("intercambios")
                .document(intercambio.id)
                .update(campoActualizar, 0)
                .await()
        } catch (_: Exception) {
        }
    }

    /**
     * RF-18: Propone un punto de encuentro seguro en el mapa y envia una tarjeta interactiva al chat.
     */
    suspend fun proponerPuntoEncuentro(
        intercambio: Intercambio,
        remitenteId: String,
        remitenteNombre: String,
        nombreLugar: String,
        direccionLugar: String,
        latitud: Double,
        longitud: Double
    ): Result<Boolean> {
        return try {
            val ahora = System.currentTimeMillis()
            val textoResumen = "📍 Punto de encuentro propuesto: $nombreLugar"

            val datosMensajeMapa = hashMapOf(
                "intercambioId" to intercambio.id,
                "remitenteId" to remitenteId,
                "remitenteNombre" to remitenteNombre,
                "texto" to textoResumen,
                "timestamp" to ahora,
                "esPuntoEncuentro" to true,
                "nombreLugar" to nombreLugar,
                "direccionLugar" to direccionLugar,
                "latitud" to latitud,
                "longitud" to longitud
            )

            val refIntercambio = db.collection("intercambios").document(intercambio.id)
            refIntercambio.collection("mensajes").add(datosMensajeMapa).await()

            val soySolicitante = remitenteId == intercambio.usuarioSolicitanteId
            val actualizacion = mutableMapOf<String, Any>(
                "puntoEncuentroNombre" to nombreLugar,
                "puntoEncuentroDireccion" to direccionLugar,
                "puntoEncuentroLat" to latitud,
                "puntoEncuentroLng" to longitud,
                "puntoEncuentroPropuestoPor" to remitenteId,
                "puntoEncuentroConfirmado" to false,
                "ultimoMensaje" to textoResumen,
                "ultimoMensajeTimestamp" to ahora,
                "ultimoMensajeAutorId" to remitenteId
            )

            if (soySolicitante) {
                actualizacion["noLeidosPropietario"] = intercambio.noLeidosPropietario + 1
            } else {
                actualizacion["noLeidosSolicitante"] = intercambio.noLeidosSolicitante + 1
            }

            refIntercambio.update(actualizacion).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * RF-18: Confirma el punto de encuentro propuesto para que ambos usuarios lo vean como "Acordado".
     */
    suspend fun confirmarPuntoEncuentro(
        intercambio: Intercambio,
        usuarioConfirmadorNombre: String
    ): Result<Boolean> {
        return try {
            val ahora = System.currentTimeMillis()
            val refIntercambio = db.collection("intercambios").document(intercambio.id)

            refIntercambio.update(
                mapOf(
                    "puntoEncuentroConfirmado" to true,
                    "ultimoMensaje" to "✅ Punto de encuentro acordado: ${intercambio.puntoEncuentroNombre}",
                    "ultimoMensajeTimestamp" to ahora
                )
            ).await()

            val msgConfirmacion = hashMapOf(
                "intercambioId" to intercambio.id,
                "remitenteId" to "",
                "remitenteNombre" to usuarioConfirmadorNombre,
                "texto" to "✅ $usuarioConfirmadorNombre confirmó el punto de encuentro en ${intercambio.puntoEncuentroNombre}.",
                "timestamp" to ahora,
                "esPuntoEncuentro" to false,
                "nombreLugar" to "",
                "direccionLugar" to "",
                "latitud" to 0.0,
                "longitud" to 0.0
            )
            refIntercambio.collection("mensajes").add(msgConfirmacion).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * RF-25, RF-26 y RF-27: Registra la evaluacion bilateral de 1 a 5 estrellas, comentario y etiquetas,
     * bloquea que el usuario vuelva a calificar el mismo trueque y recalcula el promedio del destinatario.
     */
    suspend fun registrarCalificacionBilateral(
        intercambio: Intercambio,
        resena: Resena
    ): Result<Boolean> {
        return try {
            val soySolicitante = resena.autorId == intercambio.usuarioSolicitanteId
            if (soySolicitante && intercambio.calificadoPorSolicitante) {
                return Result.failure(Exception("Ya calificaste este intercambio."))
            }
            if (!soySolicitante && intercambio.calificadoPorPropietario) {
                return Result.failure(Exception("Ya calificaste este intercambio."))
            }

            // 1. Guardar la reseña en la coleccion "resenas"
            val datosResena = hashMapOf(
                "intercambioId" to resena.intercambioId,
                "autorId" to resena.autorId,
                "autorNombre" to resena.autorNombre,
                "autorFotoUrl" to resena.autorFotoUrl,
                "destinatarioId" to resena.destinatarioId,
                "estrellas" to resena.estrellas,
                "comentario" to resena.comentario,
                "etiquetas" to resena.etiquetas,
                "articuloIntercambiado" to resena.articuloIntercambiado,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("resenas").add(datosResena).await()

            // 2. Marcar en el intercambio que este usuario ya emitio su calificacion (1 sola vez por trueque)
            val campoCalificado = if (soySolicitante) {
                "calificadoPorSolicitante"
            } else {
                "calificadoPorPropietario"
            }
            db.collection("intercambios")
                .document(intercambio.id)
                .update(campoCalificado, true)
                .await()

            // 3. Recalcular el promedio de estrellas y total de reseñas del usuario destinatario (RF-11)
            val todasResenasSnapshot = db.collection("resenas")
                .whereEqualTo("destinatarioId", resena.destinatarioId)
                .get()
                .await()

            val total = todasResenasSnapshot.size()
            val sumaEstrellas = todasResenasSnapshot.documents.sumOf {
                (it.getLong("estrellas") ?: 5L).toInt()
            }
            val nuevoPromedio = if (total > 0) {
                sumaEstrellas.toDouble() / total.toDouble()
            } else {
                resena.estrellas.toDouble()
            }

            db.collection("usuarios")
                .document(resena.destinatarioId)
                .set(
                    mapOf(
                        "promedioEstrellas" to nuevoPromedio,
                        "totalResenas" to total
                    ),
                    SetOptions.merge()
                )
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * RF-11 y RF-24: Obtiene todas las reseñas recibidas por un usuario especifico.
     */
    suspend fun obtenerResenasDeUsuario(usuarioId: String): Result<List<Resena>> {
        return try {
            val snapshot = db.collection("resenas")
                .whereEqualTo("destinatarioId", usuarioId)
                .get()
                .await()

            val lista = snapshot.documents.map { doc ->
                val tags = (doc.get("etiquetas") as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?: emptyList()

                Resena(
                    id = doc.id,
                    intercambioId = doc.getString("intercambioId") ?: "",
                    autorId = doc.getString("autorId") ?: "",
                    autorNombre = doc.getString("autorNombre") ?: "Usuario",
                    autorFotoUrl = doc.getString("autorFotoUrl") ?: "",
                    destinatarioId = doc.getString("destinatarioId") ?: usuarioId,
                    estrellas = (doc.getLong("estrellas") ?: 5L).toInt(),
                    comentario = doc.getString("comentario") ?: "",
                    etiquetas = tags,
                    articuloIntercambiado = doc.getString("articuloIntercambiado") ?: "",
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            }.sortedByDescending { it.timestamp }

            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}