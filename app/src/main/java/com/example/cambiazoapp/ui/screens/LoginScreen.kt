package com.example.cambiazoapp.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.cambiazoapp.ui.theme.InputBackground
import com.example.cambiazoapp.ui.theme.InputBorder
import com.example.cambiazoapp.ui.theme.MutedText
import com.example.cambiazoapp.ui.theme.OffWhiteBackground
import com.example.cambiazoapp.ui.theme.PureWhiteSurface
import com.example.cambiazoapp.viewmodel.UsuarioViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.cambiazoapp.R

@Composable
fun LoginScreen(
    onIrARegistro: () -> Unit,
    onLoginCorrecto: () -> Unit,
    viewModel: UsuarioViewModel = viewModel()
) {
    var correo by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    var mostrarContraseña by remember { mutableStateOf(false) }
    var mostrarRecuperacion by remember { mutableStateOf(false) }
    var correoRecuperacion by remember { mutableStateOf("") }

    val lanzadorGoogle = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            val tarea = GoogleSignIn.getSignedInAccountFromIntent(resultado.data)
            try {
                val cuenta = tarea.getResult(ApiException::class.java)
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

    // Usamos LaunchedEffect para evitar múltiples navegaciones durante recomposiciones
    LaunchedEffect(usuarioAutenticado) {
        if (usuarioAutenticado) {
            onLoginCorrecto()
        }
    }

    // Modal de Recuperación de Contraseña (RF-03)
    if (mostrarRecuperacion) {
        AlertDialog(
            onDismissRequest = { mostrarRecuperacion = false },
            containerColor = PureWhiteSurface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Recuperar contraseña",
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )
            },
            text = {
                Column {
                    Text(
                        text = "Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
                        color = MutedText,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = correoRecuperacion,
                        onValueChange = { correoRecuperacion = it },
                        placeholder = { Text("Correo electrónico", color = MutedText) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = MutedText
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputBackground,
                            unfocusedContainerColor = InputBackground,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = InputBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (correoRecuperacion.isNotBlank()) {
                            viewModel.recuperarContraseña(correoRecuperacion)
                            mostrarRecuperacion = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Enviar enlace", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarRecuperacion = false }) {
                    Text("Cancelar", color = MutedText)
                }
            }
        )
    }

    // Contenedor Principal (Fondo Nivel 0: #F9F9F9)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBackground)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Card Principal (Superficie Nivel 1: #FFFFFF con esquinas redondeadas de 24.dp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo de Cambiazo desde res/drawable/logo_cambiazo.png
                    Image(
                        painter = painterResource(id = R.drawable.logo_cambiazo),
                        contentDescription = "Logo Cambiazo",
                        modifier = Modifier.size(72.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Título de la marca
                    Text(
                        text = "Cambiazo",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepGreenBrand,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Subtítulo del mockup
                    Text(
                        text = "Trueque local y sostenible",
                        fontSize = 15.sp,
                        color = DarkSlateText.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Input: Correo electrónico (RF-01)
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
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
                            unfocusedBorderColor = InputBorder,
                            focusedTextColor = DarkSlateText,
                            unfocusedTextColor = DarkSlateText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Input: Contraseña con ícono de visibilidad (RF-01)
                    OutlinedTextField(
                        value = contraseña,
                        onValueChange = { contraseña = it },
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
                            unfocusedBorderColor = InputBorder,
                            focusedTextColor = DarkSlateText,
                            unfocusedTextColor = DarkSlateText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Enlace: ¿Olvidaste tu contraseña? alineado a la derecha (RF-03)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            color = DeepGreenBrand,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .clickable {
                                    correoRecuperacion = correo
                                    mostrarRecuperacion = true
                                }
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Botón Primario: Iniciar Sesión (RF-01)
                    Button(
                        onClick = {
                            viewModel.iniciarSesion(correo.trim(), contraseña)
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
                                text = "Iniciar Sesión",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Mensaje de retroalimentación o error
                    if (mensaje.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = mensaje,
                            color = if (mensaje.contains("correcto", ignoreCase = true) ||
                                mensaje.contains("envió", ignoreCase = true)
                            ) DeepGreenBrand else MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Separador: O CONTINÚA CON
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = InputBorder
                        )
                        Text(
                            text = "O CONTINÚA CON",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MutedText,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = InputBorder
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botones OAuth: Google y Apple (RF-02)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botón Google
                        OutlinedButton(
                            onClick = {
                                val clienteGoogle = viewModel.obtenerGoogleSignInClient()
                                lanzadorGoogle.launch(clienteGoogle.signInIntent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, InputBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = PureWhiteSurface,
                                contentColor = DarkSlateText
                            ),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "G",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFF4285F4)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Google",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = DarkSlateText
                            )
                        }

                        // Botón Apple (Visual según mockup RF-02)
                        OutlinedButton(
                            onClick = {
                                // Acción informativa o futura integración OAuth Apple
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, InputBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = PureWhiteSurface,
                                contentColor = DarkSlateText
                            ),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DarkSlateText
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Apple",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = DarkSlateText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Enlace Inferior: ¿No tienes una cuenta? Regístrate aquí
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = DarkSlateText, fontSize = 14.sp)) {
                                append("¿No tienes una cuenta? ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            ) {
                                append("Regístrate aquí")
                            }
                        },
                        modifier = Modifier
                            .clickable { onIrARegistro() }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}