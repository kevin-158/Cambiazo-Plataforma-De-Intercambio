package com.example.cambiazoapp.repository

import android.content.Context
import com.example.cambiazoapp.model.Usuario
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UsuarioRepository(
    private val context: Context
) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun obtenerGoogleSignInClient(): GoogleSignInClient {
        val opciones = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(
                context.getString(
                    com.example.cambiazoapp.R.string.default_web_client_id
                )
            )
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, opciones)
    }

    suspend fun iniciarSesionConGoogle(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val resultado = auth.signInWithCredential(credential).await()
            val user = resultado.user
            val uid = user?.uid ?: ""

            // Validación: si entra con Google por primera vez, asegurar su documento en Firestore
            if (uid.isNotEmpty()) {
                val doc = db.collection("usuarios").document(uid).get().await()
                if (!doc.exists()) {
                    guardarUsuario(
                        uid = uid,
                        nombre = user?.displayName ?: "Usuario Cambiazo",
                        correo = user?.email ?: "",
                        ciudad = "Cajamarca",
                        barrio = "Centro"
                    )
                }
            }

            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrarUsuario(correo: String, contraseña: String): Result<String> {
        return try {
            val resultado = auth.createUserWithEmailAndPassword(correo, contraseña).await()
            val uid = resultado.user?.uid ?: ""
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarSesion(correo: String, contraseña: String): Result<String> {
        return try {
            val resultado = auth.signInWithEmailAndPassword(correo, contraseña).await()
            val uid = resultado.user?.uid ?: ""
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun guardarUsuario(
        uid: String,
        nombre: String,
        correo: String,
        ciudad: String,
        barrio: String
    ): Result<Boolean> {
        return try {
            val datosUsuario = hashMapOf(
                "uid" to uid,
                "nombre" to nombre,
                "correo" to correo,
                "ciudad" to ciudad,
                "barrio" to barrio
            )

            db.collection("usuarios")
                .document(uid)
                .set(datosUsuario)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener datos de cualquier usuario (para saludo en Home y datos del dueño en Detalle RF-10/RF-11)
    suspend fun obtenerUsuarioPorId(uid: String): Result<Usuario> {
        return try {
            if (uid.isBlank()) {
                return Result.failure(Exception("ID de usuario vacío"))
            }
            val doc = db.collection("usuarios").document(uid).get().await()
            if (doc.exists()) {
                Result.success(
                    Usuario(
                        uid = doc.getString("uid") ?: uid,
                        nombre = doc.getString("nombre")?.takeIf { it.isNotBlank() } ?: "Usuario Cambiazo",
                        correo = doc.getString("correo") ?: "",
                        ciudad = doc.getString("ciudad")?.takeIf { it.isNotBlank() } ?: "Cajamarca",
                        barrio = doc.getString("barrio") ?: ""
                    )
                )
            } else {
                Result.success(
                    Usuario(
                        uid = uid,
                        nombre = "Usuario Cambiazo",
                        ciudad = "Cajamarca"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recuperarContraseña(correo: String): Result<Boolean> {
        return try {
            auth.sendPasswordResetEmail(correo).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    fun usuarioActual(): String? {
        return auth.currentUser?.uid
    }
}