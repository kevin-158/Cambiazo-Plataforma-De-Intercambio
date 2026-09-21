/*package com.example.cambiazoapp.repository

class UsuarioRepository {
}
00
 */
package com.example.cambiazoapp.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

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

    suspend fun iniciarSesionConGoogle(
        idToken: String
    ): Result<String> {

        return try {

            val credential = com.google.firebase.auth.GoogleAuthProvider
                .getCredential(idToken, null)

            val resultado = auth
                .signInWithCredential(credential)
                .await()

            val uid = resultado.user?.uid ?: ""

            Result.success(uid)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun registrarUsuario(
        correo: String,
        contraseña: String
    ): Result<String> {
        return try {
            val resultado = auth
                .createUserWithEmailAndPassword(correo, contraseña)
                .await()

            val uid = resultado.user?.uid ?: ""

            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarSesion(
        correo: String,
        contraseña: String
    ): Result<String> {
        return try {
            val resultado = auth
                .signInWithEmailAndPassword(correo, contraseña)
                .await()

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
    suspend fun recuperarContraseña(
        correo: String
    ): Result<Boolean> {
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