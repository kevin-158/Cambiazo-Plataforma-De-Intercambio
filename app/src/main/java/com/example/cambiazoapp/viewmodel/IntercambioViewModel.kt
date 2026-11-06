package com.example.cambiazoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiazoapp.model.Intercambio
import com.example.cambiazoapp.model.MensajeChat
import com.example.cambiazoapp.model.Producto
import com.example.cambiazoapp.model.Resena
import com.example.cambiazoapp.repository.IntercambioRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel para Propuestas (RF-13, RF-17), Chat en Tiempo Real (RF-16),
 * Mapa de Puntos de Encuentro (RF-18) y Calificaciones Bilaterales (RF-25, RF-26, RF-27).
 */
class IntercambioViewModel : ViewModel() {

    private val repository = IntercambioRepository()
    private val db = FirebaseFirestore.getInstance()

    private val _propuestasRecibidas = MutableStateFlow(emptyList<Intercambio>())
    val propuestasRecibidas = _propuestasRecibidas.asStateFlow()

    private val _propuestasEnviadas = MutableStateFlow(emptyList<Intercambio>())
    val propuestasEnviadas = _propuestasEnviadas.asStateFlow()

    private val _intercambioActual = MutableStateFlow<Intercambio?>(null)
    val intercambioActual = _intercambioActual.asStateFlow()

    private val _mensajesChat = MutableStateFlow(emptyList<MensajeChat>())
    val mensajesChat = _mensajesChat.asStateFlow()

    private val _resenasUsuario = MutableStateFlow(emptyList<Resena>())
    val resenasUsuario = _resenasUsuario.asStateFlow()

    private val _totalNoLeidos = MutableStateFlow(0)
    val totalNoLeidos = _totalNoLeidos.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando = _cargando.asStateFlow()

    private val _mensaje = MutableStateFlow("")
    val mensaje = _mensaje.asStateFlow()

    private var listenerIntercambio: ListenerRegistration? = null
    private var listenerMensajes: ListenerRegistration? = null

    fun limpiarMensaje() {
        _mensaje.value = ""
    }

    /**
     * RF-16: Inicia la escucha en tiempo real del intercambio y sus mensajes de chat.
     */
    fun iniciarChatTiempoReal(intercambioId: String, usuarioActualId: String) {
        detenerChatTiempoReal()
        if (intercambioId.isBlank()) return

        listenerIntercambio = repository.escucharIntercambioTiempoReal(intercambioId) { inter ->
            _intercambioActual.value = inter
            if (inter != null && usuarioActualId.isNotBlank()) {
                val tieneNoLeidos = if (usuarioActualId == inter.usuarioSolicitanteId) {
                    inter.noLeidosSolicitante > 0
                } else {
                    inter.noLeidosPropietario > 0
                }
                if (tieneNoLeidos) {
                    viewModelScope.launch {
                        repository.marcarMensajesComoLeidos(inter, usuarioActualId)
                    }
                }
            }
        }

        listenerMensajes = repository.escucharMensajesTiempoReal(intercambioId) { lista ->
            _mensajesChat.value = lista
        }
    }

    fun detenerChatTiempoReal() {
        listenerIntercambio?.remove()
        listenerMensajes?.remove()
        listenerIntercambio = null
        listenerMensajes = null
    }

    override fun onCleared() {
        super.onCleared()
        detenerChatTiempoReal()
    }

    fun enviarMensaje(
        texto: String,
        usuarioActualId: String
    ) {
        val inter = _intercambioActual.value ?: return
        if (texto.isBlank() || usuarioActualId.isBlank()) return

        val nombreRemitente = if (usuarioActualId == inter.usuarioSolicitanteId) {
            inter.usuarioSolicitanteNombre
        } else {
            inter.usuarioPropietarioNombre
        }

        viewModelScope.launch {
            repository.enviarMensajeChat(
                intercambio = inter,
                remitenteId = usuarioActualId,
                remitenteNombre = nombreRemitente,
                texto = texto
            )
        }
    }

    /**
     * RF-18: Propone un punto de encuentro seguro en el mapa dentro del chat.
     */
    fun proponerPuntoEncuentro(
        usuarioActualId: String,
        nombreLugar: String,
        direccionLugar: String,
        latitud: Double,
        longitud: Double
    ) {
        val inter = _intercambioActual.value ?: return
        val nombreRemitente = if (usuarioActualId == inter.usuarioSolicitanteId) {
            inter.usuarioSolicitanteNombre
        } else {
            inter.usuarioPropietarioNombre
        }

        viewModelScope.launch {
            repository.proponerPuntoEncuentro(
                intercambio = inter,
                remitenteId = usuarioActualId,
                remitenteNombre = nombreRemitente,
                nombreLugar = nombreLugar,
                direccionLugar = direccionLugar,
                latitud = latitud,
                longitud = longitud
            )
        }
    }

    /**
     * RF-18: El otro usuario acepta y confirma el punto de encuentro propuesto en el mapa.
     */
    fun confirmarPuntoEncuentro(usuarioActualId: String) {
        val inter = _intercambioActual.value ?: return
        val nombreConfirmador = if (usuarioActualId == inter.usuarioSolicitanteId) {
            inter.usuarioSolicitanteNombre
        } else {
            inter.usuarioPropietarioNombre
        }

        viewModelScope.launch {
            repository.confirmarPuntoEncuentro(
                intercambio = inter,
                usuarioConfirmadorNombre = nombreConfirmador
            )
        }
    }

    /**
     * RF-25, RF-26 y RF-27: Envia la calificacion de 1 a 5 estrellas, comentario y etiquetas.
     */
    fun enviarCalificacion(
        usuarioActualId: String,
        estrellas: Int,
        comentario: String,
        etiquetas: List<String>,
        onExito: () -> Unit
    ) {
        val inter = _intercambioActual.value ?: return
        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""

            var nombreAutor = if (usuarioActualId == inter.usuarioSolicitanteId) {
                inter.usuarioSolicitanteNombre
            } else {
                inter.usuarioPropietarioNombre
            }
            var fotoAutor = ""

            try {
                val docAutor = db.collection("usuarios").document(usuarioActualId).get().await()
                if (docAutor.exists()) {
                    nombreAutor = docAutor.getString("nombre")?.takeIf { it.isNotBlank() } ?: nombreAutor
                    fotoAutor = docAutor.getString("fotoUrl") ?: ""
                }
            } catch (_: Exception) {
            }

            val destinatarioId = if (usuarioActualId == inter.usuarioSolicitanteId) {
                inter.usuarioPropietarioId
            } else {
                inter.usuarioSolicitanteId
            }

            val articuloIntercambiado = "${inter.productoOfrecidoNombre} ⇄ ${inter.productoSolicitadoNombre}"

            val nuevaResena = Resena(
                intercambioId = inter.id,
                autorId = usuarioActualId,
                autorNombre = nombreAutor,
                autorFotoUrl = fotoAutor,
                destinatarioId = destinatarioId,
                estrellas = estrellas,
                comentario = comentario.trim(),
                etiquetas = etiquetas,
                articuloIntercambiado = articuloIntercambiado
            )

            val resultado = repository.registrarCalificacionBilateral(inter, nuevaResena)
            _cargando.value = false

            if (resultado.isSuccess) {
                _mensaje.value = "¡Gracias por tu calificación!"
                onExito()
            } else {
                _mensaje.value = resultado.exceptionOrNull()?.message ?: "Error al enviar calificación"
            }
        }
    }

    fun cargarResenasUsuario(usuarioId: String) {
        if (usuarioId.isBlank()) return
        viewModelScope.launch {
            val res = repository.obtenerResenasDeUsuario(usuarioId)
            if (res.isSuccess) {
                _resenasUsuario.value = res.getOrNull() ?: emptyList()
            }
        }
    }

    fun enviarPropuestaTrueque(
        productoOfrecido: Producto,
        productoSolicitado: Producto,
        usuarioSolicitanteId: String,
        nombrePropietario: String,
        mensajeOpcional: String,
        onExito: () -> Unit
    ) {
        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""

            var nombreSolicitante = "Usuario Cambiazo"
            try {
                val docUser = db.collection("usuarios").document(usuarioSolicitanteId).get().await()
                if (docUser.exists()) {
                    nombreSolicitante = docUser.getString("nombre")?.takeIf { it.isNotBlank() }
                        ?: "Usuario Cambiazo"
                }
            } catch (_: Exception) {
            }

            val imgOfrecido = productoOfrecido.imagenUrl.ifBlank {
                productoOfrecido.imagenes.firstOrNull() ?: ""
            }
            val imgSolicitado = productoSolicitado.imagenUrl.ifBlank {
                productoSolicitado.imagenes.firstOrNull() ?: ""
            }

            val nuevaPropuesta = Intercambio(
                productoOfrecidoId = productoOfrecido.id,
                productoOfrecidoNombre = productoOfrecido.nombre,
                productoOfrecidoImagen = imgOfrecido,
                productoSolicitadoId = productoSolicitado.id,
                productoSolicitadoNombre = productoSolicitado.nombre,
                productoSolicitadoImagen = imgSolicitado,
                usuarioSolicitanteId = usuarioSolicitanteId,
                usuarioSolicitanteNombre = nombreSolicitante,
                usuarioPropietarioId = productoSolicitado.usuarioId,
                usuarioPropietarioNombre = nombrePropietario.ifBlank { "Usuario Cambiazo" },
                mensaje = mensajeOpcional.trim(),
                estado = "Pendiente"
            )

            val resultado = repository.crearPropuestaIntercambio(nuevaPropuesta)

            if (resultado.isSuccess) {
                _mensaje.value = "¡Propuesta de trueque enviada como Pendiente!"
                _cargando.value = false
                onExito()
            } else {
                _mensaje.value = resultado.exceptionOrNull()?.message
                    ?: "Error al enviar la propuesta de trueque"
                _cargando.value = false
            }
        }
    }

    fun cargarMisIntercambios(usuarioId: String) {
        if (usuarioId.isBlank()) return
        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""

            val resRecibidas = repository.obtenerPropuestasRecibidas(usuarioId)
            val resEnviadas = repository.obtenerPropuestasEnviadas(usuarioId)

            val listaRec = resRecibidas.getOrNull() ?: emptyList()
            val listaEnv = resEnviadas.getOrNull() ?: emptyList()

            _propuestasRecibidas.value = listaRec
            _propuestasEnviadas.value = listaEnv

            // Calcular total de mensajes nuevos sin leer (RF-16)
            val noLeidosRec = listaRec.sumOf { it.noLeidosPropietario }
            val noLeidosEnv = listaEnv.sumOf { it.noLeidosSolicitante }
            _totalNoLeidos.value = noLeidosRec + noLeidosEnv

            _cargando.value = false
        }
    }

    fun cambiarEstadoPropuesta(
        intercambioId: String,
        nuevoEstado: String,
        usuarioActualId: String
    ) {
        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""

            val resultado = repository.actualizarEstadoIntercambio(intercambioId, nuevoEstado)

            if (resultado.isSuccess) {
                _mensaje.value = "Estado actualizado a: $nuevoEstado"
                cargarMisIntercambios(usuarioActualId)
            } else {
                _mensaje.value = resultado.exceptionOrNull()?.message
                    ?: "No se pudo actualizar el estado de la propuesta"
                _cargando.value = false
            }
        }
    }
}