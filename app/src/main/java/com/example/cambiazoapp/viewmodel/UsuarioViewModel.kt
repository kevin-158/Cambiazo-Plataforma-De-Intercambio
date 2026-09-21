/*package com.example.cambiazoapp.viewmodel

class UsuarioViewModel {
}
00100111010100110
 */
package com.example.cambiazoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiazoapp.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class UsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository =
        UsuarioRepository(application.applicationContext)

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _mensaje = MutableStateFlow("")
    val mensaje: StateFlow<String> = _mensaje

    private val _usuarioAutenticado = MutableStateFlow(false)
    val usuarioAutenticado: StateFlow<Boolean> = _usuarioAutenticado

    fun registrarUsuario(
        nombre: String,
        correo: String,
        contraseña: String,
        ciudad: String,
        barrio: String
    ) {
        viewModelScope.launch {

            _cargando.value = true
            _mensaje.value = ""

            val resultado = repository.registrarUsuario(
                correo,
                contraseña
            )

            if (resultado.isSuccess) {

                val uid = resultado.getOrNull() ?: ""

                val resultadoGuardar = repository.guardarUsuario(
                    uid,
                    nombre,
                    correo,
                    ciudad,
                    barrio
                )

                if (resultadoGuardar.isSuccess) {
                    _usuarioAutenticado.value = true
                    _mensaje.value = "Usuario registrado correctamente"
                } else {
                    _mensaje.value =
                        resultadoGuardar.exceptionOrNull()?.message
                            ?: "Error al guardar los datos del usuario"
                }

            } else {

                _mensaje.value =
                    resultado.exceptionOrNull()?.message
                        ?: "Error al registrar usuario"
            }

            _cargando.value = false
        }
    }
    fun iniciarSesion(
        correo: String,
        contraseña: String
    ) {
        viewModelScope.launch {

            _cargando.value = true
            _mensaje.value = ""

            val resultado = repository.iniciarSesion(
                correo,
                contraseña
            )

            if (resultado.isSuccess) {
                _usuarioAutenticado.value = true
                _mensaje.value = "Inicio de sesión correcto"
            } else {
                _mensaje.value =
                    resultado.exceptionOrNull()?.message
                        ?: "Error al iniciar sesión"
            }

            _cargando.value = false
        }
    }

    fun iniciarSesionConGoogle(
        idToken: String
    ) {
        viewModelScope.launch {

            _cargando.value = true
            _mensaje.value = ""

            val resultado = repository.iniciarSesionConGoogle(idToken)

            if (resultado.isSuccess) {

                _usuarioAutenticado.value = true
                _mensaje.value = "Inicio de sesión con Google correcto"

            } else {

                _mensaje.value =
                    resultado.exceptionOrNull()?.message
                        ?: "Error al iniciar sesión con Google"
            }

            _cargando.value = false
        }
    }

    fun obtenerGoogleSignInClient(): com.google.android.gms.auth.api.signin.GoogleSignInClient {
        return repository.obtenerGoogleSignInClient()
    }

    fun recuperarContraseña(correo: String) {
        viewModelScope.launch {

            _cargando.value = true
            _mensaje.value = ""

            val resultado = repository.recuperarContraseña(correo)

            if (resultado.isSuccess) {
                _mensaje.value =
                    "Se envió un enlace para restablecer tu contraseña a tu correo."
            } else {
                _mensaje.value =
                    resultado.exceptionOrNull()?.message
                        ?: "No se pudo enviar el correo de recuperación."
            }

            _cargando.value = false
        }
    }

    fun cerrarSesion() {
        repository.cerrarSesion()
        _usuarioAutenticado.value = false
        _mensaje.value = "Sesión cerrada"
    }
}