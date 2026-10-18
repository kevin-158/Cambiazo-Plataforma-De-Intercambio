package com.example.cambiazoapp.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.cambiazoapp.model.Producto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class ProductoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private fun mapearDocumentoAProducto(documento: com.google.firebase.firestore.DocumentSnapshot): Producto {
        val imagenPortada = documento.getString("imagenUrl") ?: ""
        val listaImagenes = (documento.get("imagenes") as? List<*>)
            ?.mapNotNull { it as? String }
            ?.takeIf { it.isNotEmpty() }
            ?: if (imagenPortada.isNotEmpty()) listOf(imagenPortada) else emptyList()

        return Producto(
            id = documento.id,
            usuarioId = documento.getString("usuarioId") ?: "",
            nombre = documento.getString("nombre") ?: "",
            descripcion = documento.getString("descripcion") ?: "",
            categoria = documento.getString("categoria") ?: "",
            estado = documento.getString("estado") ?: "",
            imagenUrl = imagenPortada.ifEmpty { listaImagenes.firstOrNull() ?: "" },
            imagenes = listaImagenes,
            interesesCambio = documento.getString("interesesCambio") ?: "",
            activo = documento.getBoolean("activo") ?: true
        )
    }

    // Sube una imagen real del teléfono: intenta Firebase Storage y si no está activo usa compresión directa
    suspend fun procesarYSubirImagen(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bytesComprimidos = comprimirImagenDesdeUri(context, uri)
                ?: return@withContext Result.failure(Exception("No se pudo leer la imagen"))

            try {
                // Intento 1: Subir a Firebase Storage
                val nombreArchivo = "productos/${UUID.randomUUID()}.jpg"
                val referencia = storage.reference.child(nombreArchivo)
                referencia.putBytes(bytesComprimidos).await()
                val downloadUrl = referencia.downloadUrl.await().toString()
                Result.success(downloadUrl)
            } catch (_: Exception) {
                // Intento 2 (Respaldo automático sin tarjeta): Guardar imagen real comprimida como Data URI
                val base64 = Base64.encodeToString(bytesComprimidos, Base64.NO_WRAP)
                Result.success("data:image/jpeg;base64,$base64")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun comprimirImagenDesdeUri(context: Context, uri: Uri): ByteArray? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bitmapOriginal = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (bitmapOriginal == null) return null

        // Redimensionar a un máximo de 720px para carga rápida y ligera
        val maxDimension = 720
        val ratio = minOf(
            maxDimension.toFloat() / bitmapOriginal.width,
            maxDimension.toFloat() / bitmapOriginal.height,
            1f
        )
        val ancho = (bitmapOriginal.width * ratio).toInt().coerceAtLeast(1)
        val alto = (bitmapOriginal.height * ratio).toInt().coerceAtLeast(1)

        val bitmapEscalar = Bitmap.createScaledBitmap(bitmapOriginal, ancho, alto, true)
        val outputStream = ByteArrayOutputStream()
        bitmapEscalar.compress(Bitmap.CompressFormat.JPEG, 65, outputStream)
        return outputStream.toByteArray()
    }

    suspend fun obtenerProductosUsuario(usuarioId: String): Result<List<Producto>> {
        return try {
            val resultado = db
                .collection("productos")
                .whereEqualTo("usuarioId", usuarioId)
                .get()
                .await()

            val productos = resultado.documents.map { mapearDocumentoAProducto(it) }
            Result.success(productos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Para RF-07: Obtener catálogo de artículos de otros usuarios (excluyendo propios y pausados)
    suspend fun obtenerCatalogoDisponible(usuarioActualId: String): Result<List<Producto>> {
        return try {
            val resultado = db
                .collection("productos")
                .whereEqualTo("activo", true)
                .get()
                .await()

            val productos = resultado.documents
                .map { mapearDocumentoAProducto(it) }
                .filter { it.usuarioId != usuarioActualId }

            Result.success(productos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Para RF-10: Obtener un producto específico por su ID
    suspend fun obtenerProductoPorId(productoId: String): Result<Producto> {
        return try {
            val documento = db.collection("productos").document(productoId).get().await()
            if (documento.exists()) {
                Result.success(mapearDocumentoAProducto(documento))
            } else {
                Result.failure(Exception("El artículo no existe"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun crearProducto(producto: Producto): Result<Boolean> {
        return try {
            val datosProducto = hashMapOf(
                "usuarioId" to producto.usuarioId,
                "nombre" to producto.nombre,
                "descripcion" to producto.descripcion,
                "categoria" to producto.categoria,
                "estado" to producto.estado,
                "imagenUrl" to producto.imagenUrl,
                "imagenes" to producto.imagenes,
                "interesesCambio" to producto.interesesCambio,
                "activo" to producto.activo
            )

            db.collection("productos")
                .add(datosProducto)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editarProducto(producto: Producto): Result<Boolean> {
        return try {
            val datosProducto = hashMapOf(
                "usuarioId" to producto.usuarioId,
                "nombre" to producto.nombre,
                "descripcion" to producto.descripcion,
                "categoria" to producto.categoria,
                "estado" to producto.estado,
                "imagenUrl" to producto.imagenUrl,
                "imagenes" to producto.imagenes,
                "interesesCambio" to producto.interesesCambio,
                "activo" to producto.activo
            )

            db.collection("productos")
                .document(producto.id)
                .set(datosProducto)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cambiarEstadoProducto(productoId: String, activo: Boolean): Result<Boolean> {
        return try {
            db.collection("productos")
                .document(productoId)
                .update("activo", activo)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarProducto(productoId: String): Result<Boolean> {
        return try {
            db.collection("productos")
                .document(productoId)
                .delete()
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}