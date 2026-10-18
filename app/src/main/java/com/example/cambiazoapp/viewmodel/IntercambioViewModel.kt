package com.example.cambiazoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiazoapp.model.Intercambio
import com.example.cambiazoapp.model.Producto
import com.example.cambiazoapp.repository.IntercambioRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel que conecta la logica de negocio de los trueques con las pantallas de UI.
 * Maneja el envio de propuestas (RF-13) y la actualizacion de estados (RF-17).
 */
class IntercambioViewModel : ViewModel() {

    private val repository = IntercambioRepository()
    private val db = FirebaseFirestore.getInstance()

    // Lista de propuestas recibidas por el usuario actual (soy el dueño del producto)
    private val _propuestasRecibidas = MutableStateFlow<List<Intercambio>>(emptyList())
    val propuestasRecibidas: StateFlow<List<Intercambio>> = _propuestasRecibidas

    // Lista de propuestas enviadas por el usuario actual (soy quien postula al trueque)
    private val _propuestasEnviadas = MutableStateFlow<List<Intercambio>>(emptyList())
    val propuestasEnviadas: StateFlow<List<Intercambio>> = _propuestasEnviadas

    // Estado de carga para mostrar indicadores visuales (CircularProgressIndicator)
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    // Mensajes de exito o error para mostrar en la UI
    private val _mensaje = MutableStateFlow("")
    val mensaje: StateFlow<String> = _mensaje

    fun limpiarMensaje() {
        _mensaje.value = ""
    }

    /**
     * RF-13 y RF-14: Crea una propuesta de trueque tomando el articulo propio seleccionado,
     * el articulo que se desea solicitar y un mensaje personalizado opcional.
     */
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

            // Obtener el nombre real del usuario solicitante desde Firestore
            var nombreSolicitante = "Usuario Cambiazo"
            try {
                val docUser = db.collection("usuarios").document(usuarioSolicitanteId).get().await()
                if (docUser.exists()) {
                    nombreSolicitante = docUser.getString("nombre")?.takeIf { it.isNotBlank() }
                        ?: "Usuario Cambiazo"
                }
            } catch (_: Exception) {
            }

            // Determinar las imagenes de portada de ambos productos para mostrarlas en las tarjetas de trueque
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

    /**
     * RF-17: Carga tanto las propuestas recibidas como las enviadas del usuario actual.
     */
    fun cargarMisIntercambios(usuarioId: String) {
        if (usuarioId.isBlank()) return
        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""

            val resRecibidas = repository.obtenerPropuestasRecibidas(usuarioId)
            val resEnviadas = repository.obtenerPropuestasEnviadas(usuarioId)

            if (resRecibidas.isSuccess) {
                _propuestasRecibidas.value = resRecibidas.getOrNull() ?: emptyList()
            }
            if (resEnviadas.isSuccess) {
                _propuestasEnviadas.value = resEnviadas.getOrNull() ?: emptyList()
            }

            _cargando.value = false
        }
    }

    /**
     * RF-17: Cambia el estado de una propuesta ("Aceptada", "Rechazada" o "Completada")
     * y recarga las listas para que ambos usuarios vean el estado actualizado al instante.
     */
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