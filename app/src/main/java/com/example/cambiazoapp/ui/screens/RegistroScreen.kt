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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.background
//import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
//import androidx.compose.foundation.relocation.BringIntoViewRequester
//import androidx.compose.foundation.relocation.bringIntoViewRequester
//import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.Checkbox

import androidx.compose.foundation.layout.imePadding

fun obtenerFortalezaContraseña(contraseña: String): String {

    if (contraseña.isEmpty()) {
        return ""
    }

    var puntos = 0

    if (contraseña.length >= 8) {
        puntos++
    }

    if (contraseña.any { it.isUpperCase() }) {
        puntos++
    }

    if (contraseña.any { it.isDigit() }) {
        puntos++
    }

    return when (puntos) {
        0, 1 -> "Débil"
        2 -> "Media"
        3 -> "Fuerte"
        else -> ""
    }
}
@Composable
fun RegistroScreen(
    onIrALogin: () -> Unit,
    onRegistroCorrecto: () -> Unit,
    viewModel: UsuarioViewModel = viewModel()
) {

    var correo by remember {
        mutableStateOf("")
    }

    var contraseña by remember {
        mutableStateOf("")
    }
    var nombre by remember {
        mutableStateOf("")
    }
    var ciudad by remember {
        mutableStateOf("")
    }

    var barrio by remember {
        mutableStateOf("")
    }
    var errorCiudad by remember {
        mutableStateOf("")
    }
    var errorNombre by remember {
        mutableStateOf("")
    }

    var errorCorreo by remember {
        mutableStateOf("")
    }

    var errorContraseña by remember {
        mutableStateOf("")
    }
    var confirmarContraseña by remember {
        mutableStateOf("")
    }

    var mostrarContraseña by remember {
        mutableStateOf(false)
    }

    var mostrarConfirmarContraseña by remember {
        mutableStateOf(false)
    }

    var aceptarTerminos by remember {
        mutableStateOf(false)
    }

    var errorConfirmarContraseña by remember {
        mutableStateOf("")
    }

    var errorTerminos by remember {
        mutableStateOf("")
    }

    val cargando by viewModel.cargando.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val usuarioAutenticado by viewModel.usuarioAutenticado.collectAsState()

    val fortalezaContraseña = obtenerFortalezaContraseña(contraseña)

    val cantidadBarras = when (fortalezaContraseña) {
        "Débil" -> 1
        "Media" -> 2
        "Fuerte" -> 3
        else -> 0
    }

    if (usuarioAutenticado) {
        onRegistroCorrecto()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Regístrate en Cambiazo"
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
            },
            label = {
                Text("Nombre *")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (errorNombre.isNotEmpty()) {
            Text(
                text = errorNombre,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = ciudad,

            onValueChange = {
                ciudad = it
            },
            label = {
                Text("Ciudad *")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true

        )
        if (errorCiudad.isNotEmpty()) {
            Text(
                text = errorCiudad,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = barrio,
            onValueChange = {
                barrio = it
            },
            label = {
                Text("Barrio (opcional)")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
            },
            label = {
                Text("Correo electrónico *")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (errorCorreo.isNotEmpty()) {
            Text(
                text = errorCorreo,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contraseña,
            onValueChange = {
                contraseña = it
            },
            label = {
                Text("Contraseña *")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (mostrarContraseña) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        mostrarContraseña = !mostrarContraseña
                    }
                ) {
                    Icon(
                        imageVector = if (mostrarContraseña) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (mostrarContraseña) {
                            "Ocultar contraseña"
                        } else {
                            "Mostrar contraseña"
                        }
                    )
                }
            }
        )
        if (contraseña.isNotEmpty()) {

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                repeat(3) { indice ->

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .background(
                                if (indice < cantidadBarras) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Fortaleza: $fortalezaContraseña",
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (errorContraseña.isNotEmpty()) {
            Text(
                text = errorContraseña,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmarContraseña,
            onValueChange = {
                confirmarContraseña = it
                errorConfirmarContraseña = ""

            },
            label = {
                Text("Confirmar contraseña")
            },
            modifier = Modifier.fillMaxWidth(),

            singleLine = true,
            visualTransformation = if (mostrarConfirmarContraseña) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        mostrarConfirmarContraseña = !mostrarConfirmarContraseña
                    }
                ) {
                    Icon(
                        imageVector = if (mostrarConfirmarContraseña) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (mostrarConfirmarContraseña) {
                            "Ocultar contraseña"
                        } else {
                            "Mostrar contraseña"
                        }
                    )
                }
            }
        )

        if (errorConfirmarContraseña.isNotEmpty()) {
            Text(
                text = errorConfirmarContraseña,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = aceptarTerminos,
                onCheckedChange = {
                    aceptarTerminos = it
                    errorTerminos = ""
                }
            )

            Text(
                text = "Acepto los términos y condiciones y la política de privacidad."
            )
        }
        if (errorTerminos.isNotEmpty()) {
            Text(
                text = errorTerminos,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = {
                errorNombre = ""
                errorCorreo = ""
                errorContraseña = ""
                errorCiudad = ""
                errorTerminos = ""

                if (nombre.isBlank()) {
                    errorNombre = "Ingresa tu nombre para continuar."
                    return@Button
                }

                if (correo.isBlank()) {
                    errorCorreo = "Ingresa tu correo para continuar."
                    return@Button
                }

                if (contraseña.isBlank()) {
                    errorContraseña = "Ingresa tu contraseña para continuar."
                    return@Button
                }
                //bloquear el registro si la contraseña no es "Fuerte"
                if (fortalezaContraseña != "Fuerte") {
                    errorContraseña = "La contraseña debe tener una fortaleza Fuerte."
                    return@Button
                }
                //validación de confirmación de contraseña.

                if (confirmarContraseña.isBlank()) {
                    errorConfirmarContraseña =
                        "Confirma tu contraseña para continuar."
                    return@Button
                }

                if (contraseña != confirmarContraseña) {
                    errorConfirmarContraseña =
                        "Las contraseñas no coinciden."
                    return@Button
                }

                if (ciudad.isBlank()) {
                    errorCiudad = "Ingresa tu ciudad para continuar."
                    return@Button
                }
                if (!aceptarTerminos) {
                    errorTerminos = "Debes aceptar los términos y condiciones para continuar."
                    return@Button
                }

                viewModel.registrarUsuario(
                    nombre,
                    correo,
                    contraseña,
                    ciudad,
                    barrio
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            if (cargando) {
                CircularProgressIndicator()
            } else {
                Text("Registrarse")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onIrALogin
        ) {
            Text("¿Ya tienes una cuenta? Inicia sesión")
        }

        if (mensaje.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = mensaje
            )
        }
    }
}
