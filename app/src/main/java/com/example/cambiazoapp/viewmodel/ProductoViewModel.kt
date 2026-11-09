package com.example.cambiazoapp.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiazoapp.model.Producto
import com.example.cambiazoapp.model.Usuario
import com.example.cambiazoapp.repository.ProductoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductoViewModel : ViewModel() {

    private val repository = ProductoRepository()
    private val db = FirebaseFirestore.getInstance()

    private val _productos = MutableStateFlow(emptyList<Producto>())
    val productos = _productos.asStateFlow()

    private val _catalogo = MutableStateFlow(emptyList<Producto>())
    val catalogo = _catalogo.asStateFlow()

    // Lista de artículos completos marcados como favoritos por el usuario (RF-12)
    private val _productosFavoritos = MutableStateFlow(emptyList<Producto>())
    val productosFavoritos = _productosFavoritos.asStateFlow()

    private val _usuariosMapa = MutableStateFlow(emptyMap<String, Usuario>())
    val usuariosMapa = _usuariosMapa.asStateFlow()

    private val _productoSeleccionado = MutableStateFlow<Producto?>(null)
    val productoSeleccionado = _productoSeleccionado.asStateFlow()

    private val _propietarioProducto = MutableStateFlow<Usuario?>(null)
    val propietarioProducto = _propietarioProducto.asStateFlow()

    private val _perfilUsuarioActual = MutableStateFlow<Usuario?>(null)
    val perfilUsuarioActual = _perfilUsuarioActual.asStateFlow()

    private val _favoritosIds = MutableStateFlow(emptySet<String>())
    val favoritosIds = _favoritosIds.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando = _cargando.asStateFlow()

    private val _mensaje = MutableStateFlow("")
    val mensaje = _mensaje.asStateFlow()

    private fun mapearDocAUsuario(uid: String, doc: DocumentSnapshot): Usuario {
        val favs = (doc.get("favoritosIds") as? List<*>)
            ?.mapNotNull { it as? String }
            ?: emptyList()

        return Usuario(
            uid = uid,
            nombre = doc.getString("nombre")?.takeIf { it.isNotBlank() } ?: "Usuario Cambiazo",
            correo = doc.getString("correo") ?: "",
            ciudad = doc.getString("ciudad")?.takeIf { it.isNotBlank() } ?: "Cajamarca",
            barrio = doc.getString("barrio") ?: "",
            telefono = doc.getString("telefono") ?: "",
            biografia = doc.getString("biografia") ?: "",
            fotoUrl = doc.getString("fotoUrl") ?: "",
            promedioEstrellas = doc.getDouble("promedioEstrellas") ?: 5.0,
            totalResenas = (doc.getLong("totalResenas") ?: 0L).toInt(),
            truequesCompletados = (doc.getLong("truequesCompletados") ?: 0L).toInt(),
            latitud = doc.getDouble("latitud") ?: -7.1569,
            longitud = doc.getDouble("longitud") ?: -78.5175,
            favoritosIds = favs
        )
    }

    // RF-12: Guarda o desmarca un artículo de la lista de favoritos y lo persiste en Firestore
    fun alternarFavorito(productoId: String) {
        if (productoId.isBlank()) return
        val uidActual = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val actual = _favoritosIds.value.toMutableSet()
        val seAgrego = if (actual.contains(productoId)) {
            actual.remove(productoId)
            false
        } else {
            actual.add(productoId)
            true
        }
        _favoritosIds.value = actual
        actualizarListaProductosFavoritos()

        if (uidActual.isNotBlank()) {
            viewModelScope.launch {
                try {
                    db.collection("usuarios")
                        .document(uidActual)
                        .set(mapOf("favoritosIds" to actual.toList()), SetOptions.merge())
                        .await()
                    _mensaje.value = if (seAgrego) {
                        "Artículo guardado en tus favoritos"
                    } else {
                        "Artículo eliminado de tus favoritos"
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    // RF-12: Carga los objetos Producto correspondientes a los IDs guardados en favoritos
    fun cargarArticulosFavoritos(usuarioId: String) {
        if (usuarioId.isBlank()) return
        viewModelScope.launch {
            try {
                val docUser = db.collection("usuarios").document(usuarioId).get().await()
                val favs = (docUser.get("favoritosIds") as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?.toSet()
                    ?: emptySet()
                _favoritosIds.value = favs

                if (favs.isEmpty()) {
                    _productosFavoritos.value = emptyList()
                    return@launch
                }

                val resCatalogo = repository.obtenerCatalogoDisponible(usuarioId)
                val disponibles = resCatalogo.getOrNull() ?: emptyList()
                _productosFavoritos.value = disponibles.filter { favs.contains(it.id) }
            } catch (_: Exception) {
            }
        }
    }

    private fun actualizarListaProductosFavoritos() {
        val ids = _favoritosIds.value
        val base = _catalogo.value
        if (base.isNotEmpty()) {
            _productosFavoritos.value = base.filter { ids.contains(it.id) }
        } else {
            _productosFavoritos.value = _productosFavoritos.value.filter { ids.contains(it.id) }
        }
    }

    fun cargarPerfilUsuario(usuarioId: String) {
        if (usuarioId.isBlank()) return
        viewModelScope.launch {
            try {
                val doc = db.collection("usuarios").document(usuarioId).get().await()
                if (doc.exists()) {
                    val user = mapearDocAUsuario(usuarioId, doc)
                    _perfilUsuarioActual.value = user
                    // Si es el usuario autenticado, sincronizar su lista de favoritos (RF-12)
                    val uidAutenticado = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    if (usuarioId == uidAutenticado) {
                        _favoritosIds.value = user.favoritosIds.toSet()
                        actualizarListaProductosFavoritos()
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun actualizarUbicacionGpsUsuario(usuarioId: String, latitud: Double, longitud: Double) {
        if (usuarioId.isBlank()) return
        viewModelScope.launch {
            try {
                db.collection("usuarios")
                    .document(usuarioId)
                    .set(mapOf("latitud" to latitud, "longitud" to longitud), SetOptions.merge())
                    .await()
                cargarPerfilUsuario(usuarioId)
            } catch (_: Exception) {
            }
        }
    }

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

            // Asegurar que tenemos los favoritos del usuario actual cargados desde Firestore
            try {
                if (usuarioActualId.isNotBlank()) {
                    val docMiUser = db.collection("usuarios").document(usuarioActualId).get().await()
                    if (docMiUser.exists()) {
                        val misFavs = (docMiUser.get("favoritosIds") as? List<*>)
                            ?.mapNotNull { it as? String }
                            ?.toSet()
                            ?: emptySet()
                        _favoritosIds.value = misFavs
                    }
                }
            } catch (_: Exception) {
            }

            val resultado = repository.obtenerCatalogoDisponible(usuarioActualId)

            if (resultado.isSuccess) {
                val lista = resultado.getOrNull() ?: emptyList()
                _catalogo.value = lista
                actualizarListaProductosFavoritos()

                val uidsUnicos = lista.map { it.usuarioId }.filter { it.isNotBlank() }.distinct()
                val mapaTemp = _usuariosMapa.value.toMutableMap()

                for (uid in uidsUnicos) {
                    try {
                        val doc = db.collection("usuarios").document(uid).get().await()
                        if (doc.exists()) {
                            mapaTemp[uid] = mapearDocAUsuario(uid, doc)
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

            // Sincronizar favoritos del usuario actual para que el corazón en Detalle aparezca marcado si ya es favorito
            val uidActual = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (uidActual.isNotBlank() && _favoritosIds.value.isEmpty()) {
                try {
                    val docActual = db.collection("usuarios").document(uidActual).get().await()
                    if (docActual.exists()) {
                        val favs = (docActual.get("favoritosIds") as? List<*>)
                            ?.mapNotNull { it as? String }
                            ?.toSet()
                            ?: emptySet()
                        _favoritosIds.value = favs
                    }
                } catch (_: Exception) {
                }
            }

            val resultado = repository.obtenerProductoPorId(productoId)
            if (resultado.isSuccess) {
                val prod = resultado.getOrNull()
                _productoSeleccionado.value = prod

                val duenoId = prod?.usuarioId ?: ""
                if (duenoId.isNotBlank()) {
                    try {
                        val doc = db.collection("usuarios").document(duenoId).get().await()
                        _propietarioProducto.value = if (doc.exists()) {
                            mapearDocAUsuario(duenoId, doc)
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
                _mensaje.value = if (activo) "Producto activado correctamente" else "Producto pausado correctamente"
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