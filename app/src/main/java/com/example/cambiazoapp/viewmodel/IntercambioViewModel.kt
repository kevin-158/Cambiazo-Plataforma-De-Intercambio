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

// ViewModel que gestiona propuestas (RF-13, RF-17), chat en vivo (RF-16), mapa (RF-18) y reseñas (RF-25)
class IntercambioViewModel : ViewModel() {

    private val repository = IntercambioRepository()
    private val db = FirebaseFirestore.getInstance()

    // Flujos de estado para propuestas recibidas y enviadas
    private val _propuestasRecibidas = MutableStateFlow(emptyList<Intercambio>())
    val propuestasRecibidas = _propuestasRecibidas.asStateFlow()

    private val _propuestasEnviadas = MutableStateFlow(emptyList<Intercambio>())
    val propuestasEnviadas = _propuestasEnviadas.asStateFlow()

    // Flujos para el chat activo y sus mensajes en tiempo real
    private val _intercambioActual = MutableStateFlow<Intercambio?>(null)
    val intercambioActual = _intercambioActual.asStateFlow()

    private val _mensajesChat = MutableStateFlow(emptyList<MensajeChat>())
    val mensajesChat = _mensajesChat.asStateFlow()

    // Flujo para las reseñas de reputación de un usuario
    private val _resenasUsuario = MutableStateFlow(emptyList<Resena>())
    val resenasUsuario = _resenasUsuario.asStateFlow()

    // Contador total de mensajes nuevos sin leer en tiempo real (RF-16)
    private val _totalNoLeidos = MutableStateFlow(0)
    val totalNoLeidos = _totalNoLeidos.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando = _cargando.asStateFlow()

    private val _mensaje = MutableStateFlow("")
    val mensaje = _mensaje.asStateFlow()

    // Listeners de Firestore para mantener todo actualizado en tiempo real
    private var listenerIntercambio: ListenerRegistration? = null
    private var listenerMensajes: ListenerRegistration? = null
    private var listenerRecibidas: ListenerRegistration? = null
    private var listenerEnviadas: ListenerRegistration? = null
    private var usuarioEscuchadoId: String = ""

    fun limpiarMensaje() {
        _mensaje.value = ""
    }

    // Recalcula automáticamente el total de mensajes no leídos para mostrar el aviso en vivo (RF-16)
    private fun recalcularTotalNoLeidos() {
        val noLeidosRec = _propuestasRecibidas.value.sumOf { it.noLeidosPropietario }
        val noLeidosEnv = _propuestasEnviadas.value.sumOf { it.noLeidosSolicitante }
        _totalNoLeidos.value = noLeidosRec + noLeidosEnv
    }

    // RF-16: Inicia la escucha en tiempo real de un chat específico y limpia los no leídos
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
        listenerRecibidas?.remove()
        listenerEnviadas?.remove()
    }

    // Envía un mensaje de texto en el chat en tiempo real (RF-16)
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

    // Propone un punto de encuentro seguro en el mapa dentro del chat (RF-18)
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

    // Confirma el punto de encuentro propuesto por el otro usuario (RF-18)
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

    // Registra la calificación de 1 a 5 estrellas, comentario y etiquetas (RF-25, RF-26, RF-27)
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

    // Carga las reseñas recibidas por un usuario específico (RF-11 y RF-24)
    fun cargarResenasUsuario(usuarioId: String) {
        if (usuarioId.isBlank()) return
        viewModelScope.launch {
            val res = repository.obtenerResenasDeUsuario(usuarioId)
            if (res.isSuccess) {
                _resenasUsuario.value = res.getOrNull() ?: emptyList()
            }
        }
    }

    // Crea y envía una nueva propuesta de trueque con estado inicial Pendiente (RF-13)
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

    // RF-16 y RF-17: Escucha en tiempo real las propuestas recibidas y enviadas y actualiza el aviso de mensajes nuevos
    fun cargarMisIntercambios(usuarioId: String) {
        if (usuarioId.isBlank()) return

        // Si ya estamos escuchando en tiempo real a este mismo usuario, no duplicar listeners
        if (usuarioEscuchadoId == usuarioId && listenerRecibidas != null && listenerEnviadas != null) {
            recalcularTotalNoLeidos()
            return
        }

        usuarioEscuchadoId = usuarioId
        listenerRecibidas?.remove()
        listenerEnviadas?.remove()

        _cargando.value = true

        // Listener en vivo para propuestas donde soy el propietario del artículo
        listenerRecibidas = db.collection("intercambios")
            .whereEqualTo("usuarioPropietarioId", usuarioId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val lista = snapshot.documents
                        .map { repository.mapearDocumentoAIntercambio(it) }
                        .sortedByDescending { maxOf(it.ultimoMensajeTimestamp, it.fechaCreacion) }
                    _propuestasRecibidas.value = lista
                    recalcularTotalNoLeidos()
                }
                _cargando.value = false
            }

        // Listener en vivo para propuestas donde soy el solicitante del trueque
        listenerEnviadas = db.collection("intercambios")
            .whereEqualTo("usuarioSolicitanteId", usuarioId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val lista = snapshot.documents
                        .map { repository.mapearDocumentoAIntercambio(it) }
                        .sortedByDescending { maxOf(it.ultimoMensajeTimestamp, it.fechaCreacion) }
                    _propuestasEnviadas.value = lista
                    recalcularTotalNoLeidos()
                }
                _cargando.value = false
            }
    }

    // Actualiza el estado de una propuesta (Aceptada, Rechazada, Completada) (RF-17)
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
            } else {
                _mensaje.value = resultado.exceptionOrNull()?.message
                    ?: "No se pudo actualizar el estado de la propuesta"
            }
            _cargando.value = false
        }
    }
}