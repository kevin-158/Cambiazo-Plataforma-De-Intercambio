package com.example.cambiazoapp.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiazoapp.model.Producto
import com.example.cambiazoapp.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductoViewModel : ViewModel() {

    private val repository = ProductoRepository()

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos

    private val _catalogo = MutableStateFlow<List<Producto>>(emptyList())
    val catalogo: StateFlow<List<Producto>> = _catalogo

    private val _productoSeleccionado = MutableStateFlow<Producto?>(null)
    val productoSeleccionado: StateFlow<Producto?> = _productoSeleccionado

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _mensaje = MutableStateFlow("")
    val mensaje: StateFlow<String> = _mensaje

    fun cargarProductosUsuario(usuarioId: String) {
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

    fun cargarCatalogoDisponible(usuarioActualId: String) {
        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""

            val resultado = repository.obtenerCatalogoDisponible(usuarioActualId)

            if (resultado.isSuccess) {
                _catalogo.value = resultado.getOrNull() ?: emptyList()
            } else {
                _mensaje.value = resultado.exceptionOrNull()?.message ?: "Error al cargar el catálogo"
            }

            _cargando.value = false
        }
    }

    fun cargarProductoPorId(productoId: String) {
        viewModelScope.launch {
            _cargando.value = true
            val resultado = repository.obtenerProductoPorId(productoId)
            if (resultado.isSuccess) {
                _productoSeleccionado.value = resultado.getOrNull()
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