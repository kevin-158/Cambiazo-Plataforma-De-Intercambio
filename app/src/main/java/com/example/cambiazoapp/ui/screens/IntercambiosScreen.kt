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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Shield
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
import com.example.cambiazoapp.ui.theme.InputBorder
import com.example.cambiazoapp.ui.theme.LightGreenTint
import com.example.cambiazoapp.ui.theme.MutedText
import com.example.cambiazoapp.ui.theme.OffWhiteBackground
import com.example.cambiazoapp.ui.theme.PureWhiteSurface
import com.example.cambiazoapp.viewmodel.IntercambioViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * Pantalla de Gestion de Propuestas y Estados de Trueque (Cumple con RF-17).
 * Permite al dueño ver las propuestas recibidas, aceptarlas o rechazarlas,
 * y permite a ambos usuarios ver el estado actualizado (Pendiente, Aceptada, Rechazada, Completada).
 */
@Composable
fun IntercambiosScreen(
    onVolver: () -> Unit,
    viewModel: IntercambioViewModel = viewModel()
) {
    val usuarioActualId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Pestaña seleccionada: 0 = Propuestas Recibidas, 1 = Propuestas Enviadas
    var tabSeleccionado by remember { mutableIntStateOf(0) }

    val propuestasRecibidas by viewModel.propuestasRecibidas.collectAsState()
    val propuestasEnviadas by viewModel.propuestasEnviadas.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    // Cargar las propuestas al abrir la pantalla
    LaunchedEffect(usuarioActualId) {
        if (usuarioActualId.isNotEmpty()) {
            viewModel.cargarMisIntercambios(usuarioActualId)
        }
    }

    val listaMostrar = if (tabSeleccionado == 0) propuestasRecibidas else propuestasEnviadas

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Cabecera superior
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
                        text = "Actividad de Trueques",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlateText
                    )
                    Text(
                        text = "Estado de propuestas en tiempo real",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
            }

            // Boton para refrescar el estado de las propuestas
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

        // Pestañas: Recibidas vs Enviadas
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
                    Text(
                        text = "Recibidas (${propuestasRecibidas.size})",
                        fontWeight = if (tabSeleccionado == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (tabSeleccionado == 0) DeepGreenBrand else MutedText
                    )
                }
            )
            Tab(
                selected = tabSeleccionado == 1,
                onClick = { tabSeleccionado = 1 },
                text = {
                    Text(
                        text = "Enviadas (${propuestasEnviadas.size})",
                        fontWeight = if (tabSeleccionado == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (tabSeleccionado == 1) DeepGreenBrand else MutedText
                    )
                }
            )
        }

        // Listado de tarjetas de propuestas con su estado actualizado (RF-17)
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
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (tabSeleccionado == 0) {
                            "Cuando otro usuario se interese en uno de tus artículos publicados, podrás aceptar o rechazar su propuesta aquí."
                        } else {
                            "Explora el catálogo en Home, entra al detalle de un artículo que te guste y toca 'Proponer Trueque'."
                        },
                        fontSize = 13.sp,
                        color = MutedText,
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
                        },
                        onCompletar = {
                            viewModel.cambiarEstadoPropuesta(
                                intercambioId = intercambio.id,
                                nuevoEstado = "Completada",
                                usuarioActualId = usuarioActualId
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Componente que representa la tarjeta fija de estado de la propuesta (Mockup Pantalla 6 / RF-17).
 */
@Composable
fun TarjetaEstadoPropuesta(
    intercambio: Intercambio,
    esPropuestaRecibida: Boolean,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    onCompletar: () -> Unit
) {
    // Colores dinamicos segun el estado actual de la propuesta (RF-17)
    val (colorBadgeFondo, colorBadgeTexto, textoBadge) = when (intercambio.estado) {
        "Aceptada" -> Triple(EmeraldPrimary, Color.White, "Propuesta Aceptada")
        "Completada" -> Triple(DeepGreenBrand, Color.White, "Intercambio Completado")
        "Rechazada" -> Triple( Color(0xFFFFDAD6), ErrorRed, "Propuesta Rechazada")
        else -> Triple(Color(0xFFFFF3CD), Color(0xFF856404), "Propuesta Pendiente")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // 1. Cabecera: Badge de Estado (Pendiente, Aceptada, Rechazada, Completada) y Usuario
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

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Comparativa visual de los dos articulos involucrados (Mockup Pantalla 6)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LightGreenTint.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Articulo Ofrecido
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (esPropuestaRecibida) "Te ofrecen" else "Tú ofreces",
                            fontSize = 11.sp,
                            color = MutedText,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        CambiazoImagen(
                            imagenData = intercambio.productoOfrecidoImagen,
                            contentDescription = intercambio.productoOfrecidoNombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = intercambio.productoOfrecidoNombre,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlateText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Icono central de intercambio
                    Surface(
                        shape = CircleShape,
                        color = PureWhiteSurface,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Intercambio",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Articulo Solicitado
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (esPropuestaRecibida) "Por tu artículo" else "Solicitas",
                            fontSize = 11.sp,
                            color = MutedText,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        CambiazoImagen(
                            imagenData = intercambio.productoSolicitadoImagen,
                            contentDescription = intercambio.productoSolicitadoNombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = intercambio.productoSolicitadoNombre,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlateText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 3. Mensaje adjunto si existe (RF-14)
            if (intercambio.mensaje.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = InputBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "\"${intercambio.mensaje}\"",
                            fontSize = 13.sp,
                            color = DarkSlateText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Acciones segun el estado actual y el rol del usuario (RF-17)
            when (intercambio.estado) {
                "Pendiente" -> {
                    if (esPropuestaRecibida) {
                        // El dueño del articulo puede Aceptar o Rechazar la propuesta recibida
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
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rechazar", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onAceptar,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Aceptar Trueque", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // El solicitante ve que esta esperando respuesta del dueño
                        Text(
                            text = "Esperando a que el propietario acepte o rechace tu propuesta.",
                            fontSize = 12.sp,
                            color = MutedText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                "Aceptada" -> {
                    // Sugerencia de seguridad y boton para Confirmar Intercambio Completado (Mockup Pantalla 6)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEDF4FF),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = DeepGreenBrand,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sugerencia de seguridad: Realicen el intercambio en un lugar público con afluencia de personas.",
                                fontSize = 11.sp,
                                color = DarkSlateText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onCompletar,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PureWhiteSurface),
                        border = BorderStroke(1.5.dp, DarkSlateText),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Confirmar Intercambio Completado",
                            color = DarkSlateText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                "Completada" -> {
                    Text(
                        text = "✅ ¡Este trueque fue completado exitosamente por ambas partes!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepGreenBrand,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                "Rechazada" -> {
                    Text(
                        text = "Esta propuesta fue rechazada.",
                        fontSize = 12.sp,
                        color = ErrorRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}