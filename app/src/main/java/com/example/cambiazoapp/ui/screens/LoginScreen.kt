//package com.example.cambiazoapp.ui.screens
package com.example.cambiazoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cambiazoapp.viewmodel.UsuarioViewModel
import androidx.compose.material3.AlertDialog
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn

@Composable
fun LoginScreen(
    onIrARegistro: () -> Unit,
    onLoginCorrecto: () -> Unit,
    viewModel: UsuarioViewModel = viewModel()
) {

    var correo by remember {
        mutableStateOf("")
    }

    var contraseña by remember {
        mutableStateOf("")
    }

    var mostrarRecuperacion by remember {
        mutableStateOf(false)
    }

    val contexto = LocalContext.current

    val lanzadorGoogle =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { resultado ->

            if (resultado.resultCode == Activity.RESULT_OK) {

                val tarea =
                    GoogleSignIn.getSignedInAccountFromIntent(
                        resultado.data
                    )

                try {

                    val cuenta = tarea.getResult(
                        com.google.android.gms.common.api.ApiException::class.java
                    )

                    val idToken = cuenta.idToken

                    if (idToken != null) {
                        viewModel.iniciarSesionConGoogle(idToken)
                    }

                } catch (e: Exception) {

                    // Error al seleccionar la cuenta de Google
                }
            }
        }

    val cargando by viewModel.cargando.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val usuarioAutenticado by viewModel.usuarioAutenticado.collectAsState()

    if (usuarioAutenticado) {
        onLoginCorrecto()
    }

    if (mostrarRecuperacion) {

        AlertDialog(
            onDismissRequest = {
                mostrarRecuperacion = false
            },
            title = {
                Text("Recuperar contraseña")
            },
            text = {
                Column {

                    Text(
                        text = "Ingresa tu correo y te enviaremos un enlace para restablecer tu contraseña."
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    OutlinedTextField(
                        value = correo,
                        onValueChange = {
                            correo = it
                        },
                        label = {
                            Text("Correo electrónico")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (correo.isNotBlank()) {
                            viewModel.recuperarContraseña(correo)
                            mostrarRecuperacion = false
                        }
                    }
                ) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarRecuperacion = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Cambiazo",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Inicia sesión para continuar"
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
            },
            label = {
                Text("Correo electrónico")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contraseña,
            onValueChange = {
                contraseña = it
            },
            label = {
                Text("Contraseña")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.iniciarSesion(
                    correo,
                    contraseña
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            if (cargando) {
                CircularProgressIndicator()
            } else {
                Text("Iniciar sesión")
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(
            onClick = {

                val clienteGoogle =
                    viewModel.obtenerGoogleSignInClient()

                lanzadorGoogle.launch(
                    clienteGoogle.signInIntent
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar con Google")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = {
                mostrarRecuperacion = true
            }
        ) {
            Text("¿Olvidaste tu contraseña?")
        }

        TextButton(
            onClick = onIrARegistro
        ) {
            Text("¿No tienes una cuenta? Regístrate")
        }

        if (mensaje.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = mensaje
            )
        }
    }
}
