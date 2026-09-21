package com.example.cambiazoapp.repository

import com.example.cambiazoapp.model.Producto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductoRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun obtenerProductosUsuario(
        usuarioId: String
    ): Result<List<Producto>> {

        return try {

            val resultado = db
                .collection("productos")
                .whereEqualTo("usuarioId", usuarioId)
                .get()
                .await()

            val productos = resultado.documents.map { documento ->

                Producto(
                    id = documento.id,
                    usuarioId = documento.getString("usuarioId") ?: "",
                    nombre = documento.getString("nombre") ?: "",
                    descripcion = documento.getString("descripcion") ?: "",
                    categoria = documento.getString("categoria") ?: "",
                    estado = documento.getString("estado") ?: "",
                    imagenUrl = documento.getString("imagenUrl") ?: "",
                    activo = documento.getBoolean("activo") ?: true
                )
            }

            Result.success(productos)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
    //función que permitirá guardar un producto desde la aplicación
    suspend fun crearProducto(
        producto: Producto
    ): Result<Boolean> {

        return try {

            val datosProducto = hashMapOf(
                "usuarioId" to producto.usuarioId,
                "nombre" to producto.nombre,
                "descripcion" to producto.descripcion,
                "categoria" to producto.categoria,
                "estado" to producto.estado,
                "imagenUrl" to producto.imagenUrl,
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

    suspend fun editarProducto(
        producto: Producto
    ): Result<Boolean> {

        return try {

            val datosProducto = hashMapOf(
                "usuarioId" to producto.usuarioId,
                "nombre" to producto.nombre,
                "descripcion" to producto.descripcion,
                "categoria" to producto.categoria,
                "estado" to producto.estado,
                "imagenUrl" to producto.imagenUrl,
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
    suspend fun cambiarEstadoProducto(
        productoId: String,
        activo: Boolean
    ): Result<Boolean> {

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
    suspend fun eliminarProducto(
        productoId: String
    ): Result<Boolean> {

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