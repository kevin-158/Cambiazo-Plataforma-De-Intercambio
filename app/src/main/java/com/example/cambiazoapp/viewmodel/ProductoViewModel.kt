package com.example.cambiazoapp.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiazoapp.model.Producto
import com.example.cambiazoapp.model.Usuario
import com.example.cambiazoapp.repository.ProductoRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.asStateFlow

class ProductoViewModel : ViewModel() {

    private val repository = ProductoRepository()
    private val db = FirebaseFirestore.getInstance()

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos

    private val _catalogo = MutableStateFlow<List<Producto>>(emptyList())
    val catalogo: StateFlow<List<Producto>> = _catalogo

    // Solución sin símbolos anidados que se borren al copiar:
    private val _usuariosMapa = MutableStateFlow(emptyMap<String, Usuario>())
    val usuariosMapa = _usuariosMapa.asStateFlow()
    private val _productoSeleccionado = MutableStateFlow<Producto?>(null)
    val productoSeleccionado: StateFlow<Producto?> = _productoSeleccionado

    private val _propietarioProducto = MutableStateFlow<Usuario?>(null)
    val propietarioProducto: StateFlow<Usuario?> = _propietarioProducto

    private val _perfilUsuarioActual = MutableStateFlow<Usuario?>(null)
    val perfilUsuarioActual: StateFlow<Usuario?> = _perfilUsuarioActual

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

    /**
     * Carga los datos completos del perfil del usuario autenticado desde Firestore.
     */
    fun cargarPerfilUsuario(usuarioId: String) {
        if (usuarioId.isBlank()) return
        viewModelScope.launch {
            try {
                val doc = db.collection("usuarios").document(usuarioId).get().await()
                if (doc.exists()) {
                    _perfilUsuarioActual.value = Usuario(
                        uid = usuarioId,
                        nombre = doc.getString("nombre")?.takeIf { it.isNotBlank() } ?: "Usuario",
                        correo = doc.getString("correo") ?: "",
                        ciudad = doc.getString("ciudad")?.takeIf { it.isNotBlank() } ?: "Cajamarca",
                        barrio = doc.getString("barrio") ?: "",
                        telefono = doc.getString("telefono") ?: "",
                        biografia = doc.getString("biografia") ?: "",
                        fotoUrl = doc.getString("fotoUrl") ?: ""
                    )
                }
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Actualiza el perfil del usuario (incluyendo nueva foto de perfil si fue seleccionada).
     */
    fun actualizarPerfilUsuario(
        context: Context,
        usuarioId: String,
        nombre: String,
        ciudad: String,
        barrio: String,
        telefono: String,
        biografia: String,
        fotoActualUrl: String,
        nuevaFotoUri: Uri?,
        onExito: () -> Unit
    ) {
        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""

            var urlFotoFinal = fotoActualUrl

            // Si selecciono una nueva foto de la galeria, procesarla y subirla
            if (nuevaFotoUri != null) {
                val resFoto = repository.procesarYSubirImagen(context, nuevaFotoUri)
                if (resFoto.isSuccess) {
                    urlFotoFinal = resFoto.getOrNull() ?: fotoActualUrl
                }
            }

            try {
                val datosActualizados = mapOf(
                    "uid" to usuarioId,
                    "nombre" to nombre.trim(),
                    "ciudad" to ciudad.trim(),
                    "barrio" to barrio.trim(),
                    "telefono" to telefono.trim(),
                    "biografia" to biografia.trim(),
                    "fotoUrl" to urlFotoFinal
                )

                db.collection("usuarios")
                    .document(usuarioId)
                    .set(datosActualizados, SetOptions.merge())
                    .await()

                cargarPerfilUsuario(usuarioId)
                _mensaje.value = "Perfil actualizado correctamente"
                _cargando.value = false
                onExito()
            } catch (e: Exception) {
                _mensaje.value = e.message ?: "Error al actualizar el perfil"
                _cargando.value = false
            }
        }
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

    fun cargarCatalogoDisponible(usuarioActualId: String) {
        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""

            val resultado = repository.obtenerCatalogoDisponible(usuarioActualId)

            if (resultado.isSuccess) {
                val lista = resultado.getOrNull() ?: emptyList()
                _catalogo.value = lista

                val uidsUnicos = lista.map { it.usuarioId }.filter { it.isNotBlank() }.distinct()
                val mapaTemp = _usuariosMapa.value.toMutableMap()

                for (uid in uidsUnicos) {
                    try {
                        val doc = db.collection("usuarios").document(uid).get().await()
                        if (doc.exists()) {
                            mapaTemp[uid] = Usuario(
                                uid = uid,
                                nombre = doc.getString("nombre")?.takeIf { it.isNotBlank() } ?: "Usuario",
                                correo = doc.getString("correo") ?: "",
                                ciudad = doc.getString("ciudad")?.takeIf { it.isNotBlank() } ?: "Cajamarca",
                                barrio = doc.getString("barrio") ?: "",
                                telefono = doc.getString("telefono") ?: "",
                                biografia = doc.getString("biografia") ?: "",
                                fotoUrl = doc.getString("fotoUrl") ?: ""
                            )
                        }
                    } catch (_: Exception) {
                    }
                }
                _usuariosMapa.value = mapaTemp
            } else {
                _mensaje.value = resultado.exceptionOrNull()?.message ?: "Error al cargar el catálogo"
            }

            _cargando.value = false
        }
    }

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
                                barrio = doc.getString("barrio") ?: "",
                                telefono = doc.getString("telefono") ?: "",
                                biografia = doc.getString("biografia") ?: "",
                                fotoUrl = doc.getString("fotoUrl") ?: ""
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

    /**
     * RF-22 + RF-19: Permite editar un producto reemplazando, eliminando o agregando nuevas
     * fotografias y actualizando cual es la imagen de portada.
     * Recibe una lista mixta (Strings de fotos que se conservaron o Uris de fotos nuevas).
     */
    fun editarProductoConImagenes(
        context: Context,
        productoBase: Producto,
        elementosImagen: List<Any>,
        indicePortada: Int,
        onExito: () -> Unit
    ) {
        viewModelScope.launch {
            _cargando.value = true
            _mensaje.value = ""

            val urlsFinales = mutableListOf<String>()

            for (item in elementosImagen) {
                when (item) {
                    is String -> {
                        if (item.isNotBlank()) urlsFinales.add(item)
                    }
                    is Uri -> {
                        val res = repository.procesarYSubirImagen(context, item)
                        if (res.isSuccess) {
                            res.getOrNull()?.let { urlsFinales.add(it) }
                        }
                    }
                }
            }

            if (urlsFinales.isEmpty()) {
                _mensaje.value = "El artículo debe tener al menos 1 fotografía válida."
                _cargando.value = false
                return@launch
            }

            val portadaElegida = urlsFinales.getOrElse(indicePortada) { urlsFinales.first() }
            val galeriaOrdenada = listOf(portadaElegida) +
                    urlsFinales.filterIndexed { index, _ -> index != indicePortada }

            val productoActualizado = productoBase.copy(
                imagenUrl = portadaElegida,
                imagenes = galeriaOrdenada
            )

            val resultado = repository.editarProducto(productoActualizado)

            if (resultado.isSuccess) {
                _mensaje.value = "Producto actualizado correctamente"
                _cargando.value = false
                onExito()
            } else {
                _mensaje.value = resultado.exceptionOrNull()?.message ?: "Error al actualizar el producto"
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