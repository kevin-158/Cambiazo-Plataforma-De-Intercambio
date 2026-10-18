package com.example.cambiazoapp.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiazoapp.model.Producto
import com.example.cambiazoapp.model.Usuario
import com.example.cambiazoapp.repository.ProductoRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductoViewModel : ViewModel() {

    private val repository = ProductoRepository()
    private val db = FirebaseFirestore.getInstance()

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos

    private val _catalogo = MutableStateFlow<List<Producto>>(emptyList())
    val catalogo: StateFlow<List<Producto>> = _catalogo

    private val _usuariosMapa = MutableStateFlow<Map<String, Usuario>>(emptyMap())
    val usuariosMapa: StateFlow<Map<String, Usuario>> = _usuariosMapa

    private val _productoSeleccionado = MutableStateFlow<Producto?>(null)
    val productoSeleccionado: StateFlow<Producto?> = _productoSeleccionado

    private val _propietarioProducto = MutableStateFlow<Usuario?>(null)
    val propietarioProducto: StateFlow<Usuario?> = _propietarioProducto

    private val _favoritosIds = MutableStateFlow<Set<String>>(emptySet())
    val favoritosIds: StateFlow<Set<String>> = _favoritosIds

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _mensaje = MutableStateFlow("")
    val mensaje: StateFlow<String> = _mensaje

    fun alternarFavorito(productoId: String) {
        val actual = _favoritosIds.value.toMutableSet()
        if (actual.contains(productoId)) {
            actual.remove(productoId)
        } else {
            actual.add(productoId)
        }
        _favoritosIds.value = actual
    }

    fun cargarProductosUsuario(usuarioId: String) {
        if (usuarioId.isBlank()) return
        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""

            val resultado = repository.obtenerProductosUsuario(usuarioId)

            if (resultado.isSuccess) {
                _productos.value = resultado.getOrNull() ?: emptyList()
            } else {
                _mensaje.value = resultado.exceptionOrNull()?.message ?: "Error al cargar los productos"
            }

            _cargando.value = false
        }
    }

    // RF-07: Carga catálogo de otros usuarios (excluyendo propios y pausados) junto con los datos de sus dueños
    fun cargarCatalogoDisponible(usuarioActualId: String) {
        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""

            val resultado = repository.obtenerCatalogoDisponible(usuarioActualId)

            if (resultado.isSuccess) {
                val lista = resultado.getOrNull() ?: emptyList()
                _catalogo.value = lista

                // Cargar datos básicos de los dueños para mostrar nombre y ciudad en las Cards del Feed
                val uidsUnicos = lista.map { it.usuarioId }.filter { it.isNotBlank() }.distinct()
                val mapaTemp = _usuariosMapa.value.toMutableMap()

                for (uid in uidsUnicos) {
                    if (!mapaTemp.containsKey(uid)) {
                        try {
                            val doc = db.collection("usuarios").document(uid).get().await()
                            if (doc.exists()) {
                                mapaTemp[uid] = Usuario(
                                    uid = uid,
                                    nombre = doc.getString("nombre")?.takeIf { it.isNotBlank() } ?: "Usuario",
                                    correo = doc.getString("correo") ?: "",
                                    ciudad = doc.getString("ciudad")?.takeIf { it.isNotBlank() } ?: "Cajamarca",
                                    barrio = doc.getString("barrio") ?: ""
                                )
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
                _usuariosMapa.value = mapaTemp
            } else {
                _mensaje.value = resultado.exceptionOrNull()?.message ?: "Error al cargar el catálogo"
            }

            _cargando.value = false
        }
    }

    // RF-10 y RF-11: Carga el detalle del producto y la información del dueño
    fun cargarProductoPorId(productoId: String) {
        if (productoId.isBlank()) return
        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""

            val resultado = repository.obtenerProductoPorId(productoId)
            if (resultado.isSuccess) {
                val prod = resultado.getOrNull()
                _productoSeleccionado.value = prod

                val duenoId = prod?.usuarioId ?: ""
                if (duenoId.isNotBlank()) {
                    try {
                        val doc = db.collection("usuarios").document(duenoId).get().await()
                        _propietarioProducto.value = if (doc.exists()) {
                            Usuario(
                                uid = duenoId,
                                nombre = doc.getString("nombre")?.takeIf { it.isNotBlank() } ?: "Usuario Cambiazo",
                                correo = doc.getString("correo") ?: "",
                                ciudad = doc.getString("ciudad")?.takeIf { it.isNotBlank() } ?: "Cajamarca",
                                barrio = doc.getString("barrio") ?: ""
                            )
                        } else {
                            Usuario(uid = duenoId, nombre = "Usuario Cambiazo", ciudad = "Cajamarca")
                        }
                    } catch (_: Exception) {
                        _propietarioProducto.value = Usuario(uid = duenoId, nombre = "Usuario Cambiazo", ciudad = "Cajamarca")
                    }
                }
            } else {
                _mensaje.value = "No se encontró la información del artículo."
            }

            _cargando.value = false
        }
    }
// RF-19 y RF-20: Sube múltiples fotografías reales, asigna la portada y publica el artículo

    fun crearProductoConImagenes(
        context: Context,
        productoBase: Producto,
        urislocales: List<Uri>,
        indicePortada: Int,
        onExito: () -> Unit
    ) {
        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""

            val urlsSubidas = mutableListOf<String>()

            for (uri in urislocales) {
                val res = repository.procesarYSubirImagen(context, uri)
                if (res.isSuccess) {
                    res.getOrNull()?.let { urlsSubidas.add(it) }
                }
            }

            if (urislocales.isNotEmpty() && urlsSubidas.isEmpty()) {
                _mensaje.value = "No se pudieron procesar las imágenes seleccionadas."
                _cargando.value = false
                return@launch
            }

            // Asignar la imagen elegida como portada y ponerla al inicio de la galería

            val portadaElegida = urlsSubidas.getOrElse(indicePortada) { urlsSubidas.firstOrNull() ?: "" }
            val galeriaOrdenada = if (portadaElegida.isNotEmpty()) {
                listOf(portadaElegida) + urlsSubidas.filterIndexed { index, _ -> index != indicePortada }
            } else {
                emptyList()
            }

            val productoFinal = productoBase.copy(
                imagenUrl = portadaElegida,
                imagenes = galeriaOrdenada
            )

            val resultado = repository.crearProducto(productoFinal)

            if (resultado.isSuccess) {
                _mensaje.value = "Producto publicado correctamente"
                _cargando.value = false
                onExito()
            } else {
                _mensaje.value = resultado.exceptionOrNull()?.message ?: "Error al publicar el producto"
                _cargando.value = false
            }
        }
    }

    fun crearProducto(producto: Producto) {
        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""

            val resultado = repository.crearProducto(producto)

            if (resultado.isSuccess) {
                _mensaje.value = "Producto publicado correctamente"
            } else {
                _mensaje.value = resultado.exceptionOrNull()?.message ?: "Error al publicar el producto"
            }

            _cargando.value = false
        }
    }

    fun editarProducto(producto: Producto) {
        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""

            val resultado = repository.editarProducto(producto)

            if (resultado.isSuccess) {
                _mensaje.value = "Producto actualizado correctamente"
            } else {
                _mensaje.value = resultado.exceptionOrNull()?.message ?: "Error al actualizar el producto"
            }

            _cargando.value = false
        }
    }

    fun cambiarEstadoProducto(productoId: String, activo: Boolean) {
        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""

            val resultado = repository.cambiarEstadoProducto(productoId, activo)

            if (resultado.isSuccess) {
                _mensaje.value = if (activo) {
                    "Producto activado correctamente"
                } else {
                    "Producto pausado correctamente"
                }
            } else {
                _mensaje.value = resultado.exceptionOrNull()?.message ?: "Error al cambiar el estado del producto"
            }

            _cargando.value = false
        }
    }

    fun eliminarProducto(productoId: String) {
        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""

            val resultado = repository.eliminarProducto(productoId)

            if (resultado.isSuccess) {
                _mensaje.value = "Producto eliminado correctamente"
            } else {
                _mensaje.value = resultado.exceptionOrNull()?.message ?: "Error al eliminar el producto"
            }

            _cargando.value = false
        }
    }
}