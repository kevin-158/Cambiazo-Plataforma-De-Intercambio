package com.example.cambiazoapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cambiazoapp.ui.theme.DarkSlateText
import com.example.cambiazoapp.ui.theme.DeepGreenBrand
import com.example.cambiazoapp.ui.theme.EmeraldPrimary
import com.example.cambiazoapp.ui.theme.ErrorRed
import com.example.cambiazoapp.ui.theme.InputBackground
import com.example.cambiazoapp.ui.theme.InputBorder
import com.example.cambiazoapp.ui.theme.MutedText
import com.example.cambiazoapp.ui.theme.OffWhiteBackground
import com.example.cambiazoapp.ui.theme.PureWhiteSurface
import com.example.cambiazoapp.viewmodel.UsuarioViewModel

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
    var correo by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var barrio by remember { mutableStateOf("") }

    var errorCiudad by remember { mutableStateOf("") }
    var errorNombre by remember { mutableStateOf("") }
    var errorCorreo by remember { mutableStateOf("") }
    var errorContraseña by remember { mutableStateOf("") }

    var confirmarContraseña by remember { mutableStateOf("") }
    var mostrarContraseña by remember { mutableStateOf(false) }
    var mostrarConfirmarContraseña by remember { mutableStateOf(false) }
    var aceptarTerminos by remember { mutableStateOf(false) }

    var errorConfirmarContraseña by remember { mutableStateOf("") }
    var errorTerminos by remember { mutableStateOf("") }

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

    val colorFortaleza = when (fortalezaContraseña) {
        "Débil" -> ErrorRed
        "Media" -> Color(0xFFF39C12)
        "Fuerte" -> EmeraldPrimary
        else -> InputBorder
    }

    LaunchedEffect(usuarioAutenticado) {
        if (usuarioAutenticado) {
            onRegistroCorrecto()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBackground)
            .imePadding()
    ) {
        // Barra Superior (TopBar según Mockup Pantalla 2)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureWhiteSurface)
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            IconButton(
                onClick = onIrALogin,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = DarkSlateText
                )
            }

            Text(
                text = "Crear Cuenta",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DeepGreenBrand,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Contenido desplazable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Únete a la comunidad de trueque local",
                fontSize = 16.sp,
                color = DarkSlateText.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Card Blanca de Formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 28.dp)
                ) {
                    // 1. Nombre completo
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            errorNombre = ""
                        },
                        placeholder = {
                            Text("Nombre completo", color = MutedText, fontSize = 15.sp)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Nombre",
                                tint = DarkSlateText.copy(alpha = 0.7f)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputBackground,
                            unfocusedContainerColor = InputBackground,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = if (errorNombre.isNotEmpty()) ErrorRed else InputBorder,
                            focusedTextColor = DarkSlateText,
                            unfocusedTextColor = DarkSlateText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorNombre.isNotEmpty()) {
                        Text(
                            text = errorNombre,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Correo electrónico
                    OutlinedTextField(
                        value = correo,
                        onValueChange = {
                            correo = it
                            errorCorreo = ""
                        },
                        placeholder = {
                            Text("Correo electrónico", color = MutedText, fontSize = 15.sp)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = "Correo",
                                tint = DarkSlateText.copy(alpha = 0.7f)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputBackground,
                            unfocusedContainerColor = InputBackground,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = if (errorCorreo.isNotEmpty()) ErrorRed else InputBorder,
                            focusedTextColor = DarkSlateText,
                            unfocusedTextColor = DarkSlateText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorCorreo.isNotEmpty()) {
                        Text(
                            text = errorCorreo,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Ciudad y Barrio
                    OutlinedTextField(
                        value = ciudad,
                        onValueChange = {
                            ciudad = it
                            errorCiudad = ""
                        },
                        placeholder = {
                            Text("Ciudad *", color = MutedText, fontSize = 15.sp)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = "Ciudad",
                                tint = DarkSlateText.copy(alpha = 0.7f)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputBackground,
                            unfocusedContainerColor = InputBackground,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = if (errorCiudad.isNotEmpty()) ErrorRed else InputBorder,
                            focusedTextColor = DarkSlateText,
                            unfocusedTextColor = DarkSlateText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorCiudad.isNotEmpty()) {
                        Text(
                            text = errorCiudad,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = barrio,
                        onValueChange = { barrio = it },
                        placeholder = {
                            Text("Barrio (opcional)", color = MutedText, fontSize = 15.sp)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = "Barrio",
                                tint = DarkSlateText.copy(alpha = 0.7f)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputBackground,
                            unfocusedContainerColor = InputBackground,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = InputBorder,
                            focusedTextColor = DarkSlateText,
                            unfocusedTextColor = DarkSlateText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Contraseña
                    OutlinedTextField(
                        value = contraseña,
                        onValueChange = {
                            contraseña = it
                            errorContraseña = ""
                        },
                        placeholder = {
                            Text("Contraseña", color = MutedText, fontSize = 15.sp)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Contraseña",
                                tint = DarkSlateText.copy(alpha = 0.7f)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { mostrarContraseña = !mostrarContraseña }) {
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
                                    },
                                    tint = DarkSlateText.copy(alpha = 0.6f)
                                )
                            }
                        },
                        visualTransformation = if (mostrarContraseña) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputBackground,
                            unfocusedContainerColor = InputBackground,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = if (errorContraseña.isNotEmpty()) ErrorRed else InputBorder,
                            focusedTextColor = DarkSlateText,
                            unfocusedTextColor = DarkSlateText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Indicador de Fortaleza de Contraseña (RF-05)
                    if (contraseña.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(3) { indice ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(5.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (indice < cantidadBarras) {
                                                colorFortaleza
                                            } else {
                                                InputBorder
                                            }
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = fortalezaContraseña,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorFortaleza
                        )
                    }

                    if (errorContraseña.isNotEmpty()) {
                        Text(
                            text = errorContraseña,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. Confirmar contraseña
                    OutlinedTextField(
                        value = confirmarContraseña,
                        onValueChange = {
                            confirmarContraseña = it
                            errorConfirmarContraseña = ""
                        },
                        placeholder = {
                            Text("Confirmar contraseña", color = MutedText, fontSize = 15.sp)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LockReset,
                                contentDescription = "Confirmar contraseña",
                                tint = DarkSlateText.copy(alpha = 0.7f)
                            )
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
                                    },
                                    tint = DarkSlateText.copy(alpha = 0.6f)
                                )
                            }
                        },
                        visualTransformation = if (mostrarConfirmarContraseña) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputBackground,
                            unfocusedContainerColor = InputBackground,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = if (errorConfirmarContraseña.isNotEmpty()) ErrorRed else InputBorder,
                            focusedTextColor = DarkSlateText,
                            unfocusedTextColor = DarkSlateText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorConfirmarContraseña.isNotEmpty()) {
                        Text(
                            text = errorConfirmarContraseña,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 6. Checkbox Términos y Condiciones (RF-06)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = aceptarTerminos,
                            onCheckedChange = {
                                aceptarTerminos = it
                                errorTerminos = ""
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = EmeraldPrimary,
                                uncheckedColor = InputBorder,
                                checkmarkColor = Color.White
                            )
                        )

                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = DarkSlateText, fontSize = 13.sp)) {
                                    append("Acepto los ")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        color = DeepGreenBrand,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                ) {
                                    append("Términos y Condiciones")
                                }
                                withStyle(style = SpanStyle(color = DarkSlateText, fontSize = 13.sp)) {
                                    append(" y la ")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        color = DeepGreenBrand,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                ) {
                                    append("Política de Privacidad")
                                }
                            },
                            lineHeight = 18.sp,
                            modifier = Modifier.clickable {
                                aceptarTerminos = !aceptarTerminos
                                errorTerminos = ""
                            }
                        )
                    }

                    if (errorTerminos.isNotEmpty()) {
                        Text(
                            text = errorTerminos,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // 7. Botón Primario: Crear mi cuenta
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

                            if (fortalezaContraseña != "Fuerte") {
                                errorContraseña = "La contraseña debe tener una fortaleza Fuerte."
                                return@Button
                            }

                            if (confirmarContraseña.isBlank()) {
                                errorConfirmarContraseña = "Confirma tu contraseña para continuar."
                                return@Button
                            }

                            if (contraseña != confirmarContraseña) {
                                errorConfirmarContraseña = "Las contraseñas no coinciden."
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
                        enabled = !cargando,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = EmeraldPrimary.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = "Crear mi cuenta",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (mensaje.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = mensaje,
                            color = if (mensaje.contains("correctamente", ignoreCase = true)) {
                                DeepGreenBrand
                            } else {
                                ErrorRed
                            },
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Enlace inferior: ¿Ya tienes cuenta? Inicia sesión
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = DarkSlateText, fontSize = 14.sp)) {
                        append("¿Ya tienes cuenta? ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    ) {
                        append("Inicia sesión")
                    }
                },
                modifier = Modifier
                    .clickable { onIrALogin() }
                    .padding(vertical = 8.dp)
            )
        }
    }
}