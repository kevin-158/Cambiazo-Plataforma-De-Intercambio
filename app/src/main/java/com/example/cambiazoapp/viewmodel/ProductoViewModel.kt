package com.example.cambiazoapp.viewmodel

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

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _mensaje = MutableStateFlow("")
    val mensaje: StateFlow<String> = _mensaje

    fun cargarProductosUsuario(usuarioId: String) {

        viewModelScope.launch {

            _cargando.value = true
            _mensaje.value = ""

            val resultado =
                repository.obtenerProductosUsuario(usuarioId)

            if (resultado.isSuccess) {

                _productos.value =
                    resultado.getOrNull() ?: emptyList()

            } else {

                _mensaje.value =
                    resultado.exceptionOrNull()?.message
                        ?: "Error al cargar los productos"
            }

            _cargando.value = false
        }
    }
    //Ahora conectamos crearProducto() con el ProductoViewModel
    fun crearProducto(
        producto: Producto
    ) {

        viewModelScope.launch {

            _cargando.value = true
            _mensaje.value = ""

            val resultado =
                repository.crearProducto(producto)

            if (resultado.isSuccess) {

                _mensaje.value =
                    "Producto publicado correctamente"

            } else {

                _mensaje.value =
                    resultado.exceptionOrNull()?.message
                        ?: "Error al publicar el producto"
            }

            _cargando.value = false
        }
    }

    fun editarProducto(producto: Producto) {

        viewModelScope.launch {

            _cargando.value = true
            _mensaje.value = ""

            val resultado =
                repository.editarProducto(producto)

            if (resultado.isSuccess) {

                _mensaje.value = "Producto actualizado correctamente"

            } else {

                _mensaje.value =
                    resultado.exceptionOrNull()?.message
                        ?: "Error al actualizar el producto"
            }

            _cargando.value = false
        }
    }

    fun cambiarEstadoProducto(
        productoId: String,
        activo: Boolean
    ) {

        viewModelScope.launch {

            _cargando.value = true
            _mensaje.value = ""

            val resultado =
                repository.cambiarEstadoProducto(
                    productoId,
                    activo
                )

            if (resultado.isSuccess) {

                _mensaje.value =
                    if (activo) {
                        "Producto activado correctamente"
                    } else {
                        "Producto pausado correctamente"
                    }

            } else {

                _mensaje.value =
                    resultado.exceptionOrNull()?.message
                        ?: "Error al cambiar el estado del producto"
            }

            _cargando.value = false
        }
    }

    fun eliminarProducto(
        productoId: String
    ) {

        viewModelScope.launch {

            _cargando.value = true
            _mensaje.value = ""

            val resultado =
                repository.eliminarProducto(productoId)

            if (resultado.isSuccess) {

                _mensaje.value =
                    "Producto eliminado correctamente"

            } else {

                _mensaje.value =
                    resultado.exceptionOrNull()?.message
                        ?: "Error al eliminar el producto"
            }

            _cargando.value = false
        }
    }
}