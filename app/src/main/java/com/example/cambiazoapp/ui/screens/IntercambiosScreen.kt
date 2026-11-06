package com.example.cambiazoapp.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cambiazoapp.model.Intercambio
import com.example.cambiazoapp.ui.components.CambiazoImagen
import com.example.cambiazoapp.ui.theme.DarkSlateText
import com.example.cambiazoapp.ui.theme.DeepGreenBrand
import com.example.cambiazoapp.ui.theme.EmeraldPrimary
import com.example.cambiazoapp.ui.theme.ErrorRed
import com.example.cambiazoapp.ui.theme.InputBackground
import com.example.cambiazoapp.ui.theme.LightGreenTint
import com.example.cambiazoapp.ui.theme.MutedText
import com.example.cambiazoapp.ui.theme.OffWhiteBackground
import com.example.cambiazoapp.ui.theme.PureWhiteSurface
import com.example.cambiazoapp.viewmodel.IntercambioViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun IntercambiosScreen(
    onVolver: () -> Unit,
    onAbrirChatNegociacion: (String) -> Unit = {},
    viewModel: IntercambioViewModel = viewModel()
) {
    val usuarioActualId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var tabSeleccionado by remember { mutableIntStateOf(0) }

    val propuestasRecibidas by viewModel.propuestasRecibidas.collectAsState()
    val propuestasEnviadas by viewModel.propuestasEnviadas.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    LaunchedEffect(usuarioActualId) {
        if (usuarioActualId.isNotEmpty()) {
            viewModel.cargarMisIntercambios(usuarioActualId)
        }
    }

    val listaMostrar = if (tabSeleccionado == 0) propuestasRecibidas else propuestasEnviadas
    val noLeidosRecibidas = propuestasRecibidas.sumOf { it.noLeidosPropietario }
    val noLeidosEnviadas = propuestasEnviadas.sumOf { it.noLeidosSolicitante }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureWhiteSurface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = DarkSlateText
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Actividad y Mensajes",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlateText
                    )
                    Text(
                        text = "Toca una propuesta para abrir el chat y mapa en vivo",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
            }

            IconButton(
                onClick = { viewModel.cargarMisIntercambios(usuarioActualId) }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualizar",
                    tint = DeepGreenBrand
                )
            }
        }

        TabRow(
            selectedTabIndex = tabSeleccionado,
            containerColor = PureWhiteSurface,
            contentColor = DeepGreenBrand,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[tabSeleccionado]),
                    color = EmeraldPrimary,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = tabSeleccionado == 0,
                onClick = { tabSeleccionado = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Recibidas (${propuestasRecibidas.size})",
                            fontWeight = if (tabSeleccionado == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (tabSeleccionado == 0) DeepGreenBrand else MutedText
                        )
                        if (noLeidosRecibidas > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = EmeraldPrimary
                            ) {
                                Text(
                                    text = "$noLeidosRecibidas",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            )
            Tab(
                selected = tabSeleccionado == 1,
                onClick = { tabSeleccionado = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Enviadas (${propuestasEnviadas.size})",
                            fontWeight = if (tabSeleccionado == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (tabSeleccionado == 1) DeepGreenBrand else MutedText
                        )
                        if (noLeidosEnviadas > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = EmeraldPrimary
                            ) {
                                Text(
                                    text = "$noLeidosEnviadas",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            )
        }

        if (cargando) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        } else if (listaMostrar.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (tabSeleccionado == 0) {
                            "Aún no has recibido propuestas de trueque."
                        } else {
                            "Aún no has enviado propuestas de trueque."
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlateText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(listaMostrar, key = { it.id }) { intercambio ->
                    TarjetaEstadoPropuesta(
                        intercambio = intercambio,
                        esPropuestaRecibida = tabSeleccionado == 0,
                        onAbrirChat = { onAbrirChatNegociacion(intercambio.id) },
                        onAceptar = {
                            viewModel.cambiarEstadoPropuesta(
                                intercambioId = intercambio.id,
                                nuevoEstado = "Aceptada",
                                usuarioActualId = usuarioActualId
                            )
                        },
                        onRechazar = {
                            viewModel.cambiarEstadoPropuesta(
                                intercambioId = intercambio.id,
                                nuevoEstado = "Rechazada",
                                usuarioActualId = usuarioActualId
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaEstadoPropuesta(
    intercambio: Intercambio,
    esPropuestaRecibida: Boolean,
    onAbrirChat: () -> Unit,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit
) {
    val (colorBadgeFondo, colorBadgeTexto, textoBadge) = when (intercambio.estado) {
        "Aceptada" -> Triple(EmeraldPrimary, Color.White, "Propuesta Aceptada")
        "Completada" -> Triple(DeepGreenBrand, Color.White, "Intercambio Completado")
        "Rechazada" -> Triple(Color(0xFFFFDAD6), ErrorRed, "Propuesta Rechazada")
        else -> Triple(Color(0xFFFFF3CD), Color(0xFF856404), "Propuesta Pendiente")
    }

    val cantidadNoLeidos = if (esPropuestaRecibida) {
        intercambio.noLeidosPropietario
    } else {
        intercambio.noLeidosSolicitante
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAbrirChat() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colorBadgeFondo
                ) {
                    Text(
                        text = textoBadge,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorBadgeTexto,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // RF-16: Aviso de mensajes nuevos sin leer
                    if (cantidadNoLeidos > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldPrimary
                        ) {
                            Text(
                                text = "$cantidadNoLeidos nuevo(s)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    val otroUsuario = if (esPropuestaRecibida) {
                        "De: @${intercambio.usuarioSolicitanteNombre.replace(" ", "")}"
                    } else {
                        "Para: @${intercambio.usuarioPropietarioNombre.replace(" ", "")}"
                    }

                    Text(
                        text = otroUsuario,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepGreenBrand
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Comparativa visual de los dos articulos
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LightGreenTint.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (esPropuestaRecibida) "Te ofrecen" else "Tú ofreces",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        CambiazoImagen(
                            imagenData = intercambio.productoOfrecidoImagen,
                            contentDescription = intercambio.productoOfrecidoNombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = intercambio.productoOfrecidoNombre,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlateText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = PureWhiteSurface,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (esPropuestaRecibida) "Por tu artículo" else "Solicitas",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        CambiazoImagen(
                            imagenData = intercambio.productoSolicitadoImagen,
                            contentDescription = intercambio.productoSolicitadoNombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = intercambio.productoSolicitadoNombre,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlateText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Ultimo mensaje del chat en tiempo real (RF-16)
            if (intercambio.ultimoMensaje.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = InputBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = DeepGreenBrand,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = intercambio.ultimoMensaje,
                            fontSize = 13.sp,
                            color = DarkSlateText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Punto de encuentro si existe (RF-18)
            if (intercambio.puntoEncuentroNombre.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = DeepGreenBrand,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (intercambio.puntoEncuentroConfirmado) {
                            "Punto acordado: ${intercambio.puntoEncuentroNombre}"
                        } else {
                            "Punto propuesto: ${intercambio.puntoEncuentroNombre}"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepGreenBrand
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones rapidos de accion + Boton de Abrir Chat y Mapa
            if (intercambio.estado == "Pendiente" && esPropuestaRecibida) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onRechazar,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ErrorRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onAceptar,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aceptar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = onAbrirChat,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepGreenBrand),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (intercambio.estado == "Completada") {
                        "Ver Chat / Calificar Trueque ★"
                    } else {
                        "Abrir Chat en Vivo y Mapa 📍"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}