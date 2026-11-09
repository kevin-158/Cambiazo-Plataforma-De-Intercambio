package com.example.cambiazoapp.ui.screens

import android.preference.PreferenceManager
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cambiazoapp.model.Intercambio
import com.example.cambiazoapp.model.MensajeChat
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
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.MapTileIndex

//======Chat en Vivo y Negociación (Al abrir una propuesta)============

// Modelo auxiliar para los puntos de encuentro sugeridos (RF-18)
data class PuntoSeguroSugerido(
    val nombre: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double
)

// Pantalla 6: Chat en Tiempo Real (RF-16), Mapa de Puntos Seguros (RF-18) y Calificación (RF-25)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatTradeScreen(
    intercambioId: String,
    onVolver: () -> Unit,
    onVerPerfilPublico: (String) -> Unit,
    viewModel: IntercambioViewModel = viewModel()
) {
    val context = LocalContext.current
    val usuarioActualId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Iniciar escucha en tiempo real del intercambio y sus mensajes al abrir la pantalla (RF-16)
    DisposableEffect(intercambioId, usuarioActualId) {
        viewModel.iniciarChatTiempoReal(intercambioId, usuarioActualId)
        onDispose {
            viewModel.detenerChatTiempoReal()
        }
    }

    val intercambio by viewModel.intercambioActual.collectAsState()
    val mensajes by viewModel.mensajesChat.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    var textoMensaje by remember { mutableStateOf("") }
    var mostrarModalMapa by remember { mutableStateOf(false) }
    var mostrarModalCalificar by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Hacer scroll automático hacia el último mensaje recibido o enviado (RF-16)
    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.size - 1)
        }
    }

    val inter = intercambio
    if (inter == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhiteBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = EmeraldPrimary)
        }
        return
    }

    // Identificar el rol del usuario actual dentro de la negociación
    val soySolicitante = usuarioActualId == inter.usuarioSolicitanteId
    val otroUsuarioId = if (soySolicitante) inter.usuarioPropietarioId else inter.usuarioSolicitanteId
    val otroUsuarioNombre = if (soySolicitante) {
        inter.usuarioPropietarioNombre
    } else {
        inter.usuarioSolicitanteNombre
    }

    // Verificar si el usuario actual ya calificó este trueque (RF-25: 1 sola vez)
    val yaCalifiqueEsteTrueque = if (soySolicitante) {
        inter.calificadoPorSolicitante
    } else {
        inter.calificadoPorPropietario
    }

    // Modal de Mapa Real para proponer o ver el Punto de Encuentro Seguro (RF-18)
    if (mostrarModalMapa) {
        ModalPuntoEncuentroMapa(
            intercambio = inter,
            onDismiss = { mostrarModalMapa = false },
            onEnviarPunto = { nombre, direccion, lat, lng ->
                viewModel.proponerPuntoEncuentro(
                    usuarioActualId = usuarioActualId,
                    nombreLugar = nombre,
                    direccionLugar = direccion,
                    latitud = lat,
                    longitud = lng
                )
                mostrarModalMapa = false
                Toast.makeText(
                    context,
                    "Punto de encuentro enviado al chat",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // Modal de Evaluación Bilateral de 1 a 5 estrellas (RF-25, RF-26, RF-27)
    if (mostrarModalCalificar) {
        ModalCalificarTrueque(
            otroUsuarioNombre = otroUsuarioNombre,
            articuloNombre = if (soySolicitante) inter.productoSolicitadoNombre else inter.productoOfrecidoNombre,
            cargando = cargando,
            onDismiss = { mostrarModalCalificar = false },
            onEnviarCalificacion = { estrellas, comentario, etiquetas ->
                viewModel.enviarCalificacion(
                    usuarioActualId = usuarioActualId,
                    estrellas = estrellas,
                    comentario = comentario,
                    etiquetas = etiquetas,
                    onExito = {
                        mostrarModalCalificar = false
                        Toast.makeText(
                            context,
                            "¡Calificación registrada exitosamente!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        )
    }

    // Usamos contentWindowInsets = WindowInsets(0, 0, 0, 0) para eliminar el espacio blanco superior
    Scaffold(
        containerColor = OffWhiteBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // Barra inferior para escribir mensajes y abrir el mapa (RF-16 y RF-18)
            Surface(
                color = PureWhiteSurface,
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón de ubicación para abrir el mapa real (RF-18)
                    Surface(
                        shape = CircleShape,
                        color = LightGreenTint,
                        border = BorderStroke(1.dp, EmeraldPrimary),
                        modifier = Modifier
                            .size(46.dp)
                            .clickable { mostrarModalMapa = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Proponer punto de encuentro en mapa",
                                tint = DeepGreenBrand,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Campo de texto del mensaje
                    OutlinedTextField(
                        value = textoMensaje,
                        onValueChange = { textoMensaje = it },
                        placeholder = {
                            Text(
                                text = "Escribe un mensaje...",
                                fontSize = 14.sp,
                                color = MutedText
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputBackground,
                            unfocusedContainerColor = InputBackground,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = InputBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Botón para enviar el mensaje en tiempo real (RF-16)
                    Surface(
                        shape = CircleShape,
                        color = if (textoMensaje.isNotBlank()) EmeraldPrimary else InputBorder,
                        modifier = Modifier
                            .size(46.dp)
                            .clickable(enabled = textoMensaje.isNotBlank()) {
                                viewModel.enviarMensaje(textoMensaje, usuarioActualId)
                                textoMensaje = ""
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Cabecera superior pegada al borde superior con color blanco uniforme
            Surface(
                color = PureWhiteSurface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onVerPerfilPublico(otroUsuarioId) }
                    ) {
                        IconButton(onClick = onVolver) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = DarkSlateText
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(LightGreenTint)
                                .border(1.5.dp, EmeraldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = otroUsuarioNombre.take(1).uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepGreenBrand
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = otroUsuarioNombre,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlateText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Toca para ver su perfil y reputación ★",
                                fontSize = 11.sp,
                                color = DeepGreenBrand,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Botón rápido para abrir el Mapa de Puntos Seguros (RF-18)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = LightGreenTint,
                        modifier = Modifier
                            .clickable { mostrarModalMapa = true }
                            .padding(end = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = DeepGreenBrand,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Mapa",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepGreenBrand
                            )
                        }
                    }
                }
            }

            // 2. Tarjeta fija de resumen de la propuesta y punto de encuentro acordado
            TarjetaResumenNegociacionFija(
                intercambio = inter,
                soySolicitante = soySolicitante,
                yaCalifique = yaCalifiqueEsteTrueque,
                usuarioActualId = usuarioActualId,
                onAceptarPropuesta = {
                    viewModel.cambiarEstadoPropuesta(inter.id, "Aceptada", usuarioActualId)
                },
                onRechazarPropuesta = {
                    viewModel.cambiarEstadoPropuesta(inter.id, "Rechazada", usuarioActualId)
                },
                onCompletarIntercambio = {
                    viewModel.cambiarEstadoPropuesta(inter.id, "Completada", usuarioActualId)
                    mostrarModalCalificar = true
                },
                onAbrirModalCalificar = {
                    mostrarModalCalificar = true
                },
                onAbrirMapa = {
                    mostrarModalMapa = true
                },
                onConfirmarPuntoEncuentro = {
                    viewModel.confirmarPuntoEncuentro(usuarioActualId)
                }
            )

            // 3. Listado de mensajes en tiempo real (RF-16)
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Aviso de seguridad de Cambiazo
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFEDF4FF),
                        border = BorderStroke(1.dp, Color(0xFFD1E4FB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = DeepGreenBrand,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sugerencia de seguridad: Acuerden un punto de encuentro público usando el botón de ubicación 📍 en este chat.",
                                fontSize = 11.sp,
                                color = DarkSlateText
                            )
                        }
                    }
                }

                items(mensajes, key = { it.id }) { msg ->
                    BurbujaMensajeChat(
                        mensaje = msg,
                        esMio = msg.remitenteId == usuarioActualId,
                        onVerEnMapaClick = { mostrarModalMapa = true }
                    )
                }
            }
        }
    }
}

// Tarjeta superior fija con el estado de la propuesta y el punto de encuentro (RF-17 y RF-18)
@Composable
fun TarjetaResumenNegociacionFija(
    intercambio: Intercambio,
    soySolicitante: Boolean,
    yaCalifique: Boolean,
    usuarioActualId: String,
    onAceptarPropuesta: () -> Unit,
    onRechazarPropuesta: () -> Unit,
    onCompletarIntercambio: () -> Unit,
    onAbrirModalCalificar: () -> Unit,
    onAbrirMapa: () -> Unit,
    onConfirmarPuntoEncuentro: () -> Unit
) {
    val (colorBadge, textoEstado) = when (intercambio.estado) {
        "Aceptada" -> Pair(EmeraldPrimary, "Propuesta Aceptada")
        "Completada" -> Pair(DeepGreenBrand, "Intercambio Completado")
        "Rechazada" -> Pair(ErrorRed, "Propuesta Rechazada")
        else -> Pair(Color(0xFFF39C12), "Propuesta Pendiente")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CambiazoImagen(
                    imagenData = intercambio.productoOfrecidoImagen,
                    contentDescription = intercambio.productoOfrecidoNombre,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                CambiazoImagen(
                    imagenData = intercambio.productoSolicitadoImagen,
                    contentDescription = intercambio.productoSolicitadoNombre,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colorBadge.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = textoEstado,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorBadge,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${intercambio.productoOfrecidoNombre} ⇄ ${intercambio.productoSolicitadoNombre}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlateText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Mostrar punto de encuentro propuesto o acordado visible para ambos (RF-18)
            if (intercambio.puntoEncuentroNombre.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LightGreenTint.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAbrirMapa() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = DeepGreenBrand,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (intercambio.puntoEncuentroConfirmado) {
                                        "✅ Punto Acordado: ${intercambio.puntoEncuentroNombre}"
                                    } else {
                                        "📍 Punto Propuesto: ${intercambio.puntoEncuentroNombre}"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepGreenBrand
                                )
                                Text(
                                    text = "${intercambio.puntoEncuentroDireccion} (Toca para ver en mapa)",
                                    fontSize = 11.sp,
                                    color = DarkSlateText
                                )
                            }
                        }

                        if (!intercambio.puntoEncuentroConfirmado &&
                            intercambio.puntoEncuentroPropuestoPor.isNotBlank() &&
                            intercambio.puntoEncuentroPropuestoPor != usuarioActualId
                        ) {
                            Button(
                                onClick = onConfirmarPuntoEncuentro,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Acordar", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Botones de acción según el estado actual de la propuesta
            when (intercambio.estado) {
                "Pendiente" -> {
                    if (!soySolicitante) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRechazarPropuesta,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, ErrorRed),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Rechazar", fontSize = 12.sp, color = ErrorRed, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onAceptarPropuesta,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Aceptar Trueque", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                "Aceptada" -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onCompletarIntercambio,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PureWhiteSurface),
                        border = BorderStroke(1.5.dp, DarkSlateText),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Confirmar Intercambio Completado",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlateText
                        )
                    }
                }

                "Completada" -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!yaCalifique) {
                        Button(
                            onClick = onAbrirModalCalificar,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Calificar experiencia del trueque (1 a 5 ★)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Text(
                            text = "✓ Ya calificaste este intercambio completado.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepGreenBrand,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

// Burbuja de mensaje del chat o tarjeta interactiva de Punto de Encuentro (RF-16 y RF-18)
@Composable
fun BurbujaMensajeChat(
    mensaje: MensajeChat,
    esMio: Boolean,
    onVerEnMapaClick: () -> Unit
) {
    val formatoHora = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val horaTexto = remember(mensaje.timestamp) {
        if (mensaje.timestamp > 0L) formatoHora.format(Date(mensaje.timestamp)) else ""
    }

    if (mensaje.remitenteId.isBlank()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = LightGreenTint
            ) {
                Text(
                    text = mensaje.texto,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DeepGreenBrand,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (esMio) Alignment.End else Alignment.Start
    ) {
        if (mensaje.esPuntoEncuentro) {
            Card(
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .clickable { onVerEnMapaClick() },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (esMio) EmeraldPrimary else PureWhiteSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (esMio) Color.White else DeepGreenBrand,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Punto de Encuentro Seguro",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (esMio) Color.White else DeepGreenBrand
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = mensaje.nombreLugar,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (esMio) Color.White else DarkSlateText
                    )
                    Text(
                        text = mensaje.direccionLugar,
                        fontSize = 12.sp,
                        color = if (esMio) Color.White.copy(alpha = 0.9f) else MutedText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (esMio) DeepGreenBrand else LightGreenTint,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🗺️ Toca para abrir en el mapa real",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (esMio) Color.White else DeepGreenBrand,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (esMio) 18.dp else 4.dp,
                    bottomEnd = if (esMio) 4.dp else 18.dp
                ),
                color = if (esMio) EmeraldPrimary else PureWhiteSurface,
                shadowElevation = 1.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = mensaje.texto,
                    fontSize = 14.sp,
                    color = if (esMio) Color.White else DarkSlateText,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = horaTexto,
            fontSize = 10.sp,
            color = MutedText,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

// Modal con Mapa Real corregido con servidor CDN y caché nueva para eliminar el error 403 (RF-18)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalPuntoEncuentroMapa(
    intercambio: Intercambio,
    onDismiss: () -> Unit,
    onEnviarPunto: (String, String, Double, Double) -> Unit
) {
    val context = LocalContext.current

    // Fuente de mapas OSM vía CDN Carto Voyager (alta velocidad, diseño limpio y sin bloqueo 403)
// Lista de Puntos Seguros con coordenadas GPS exactas sobre el mapa de Cajamarca (RF-18)
    val puntosSeguros = remember {
        listOf(
            PuntoSeguroSugerido(
                "MegaPlaza Cajamarca",
                "Jr. Sor Manuela Gil / Av. Hoyos Rubio - Zona comercial segura",
                -7.1520,
                -78.5057
            ),
            PuntoSeguroSugerido(
                "Plaza de Armas de Cajamarca",
                "Centro Histórico (Jr. Del Comercio y Jr. Dos de Mayo)",
                -7.1569,
                -78.5175
            ),
            PuntoSeguroSugerido(
                "Real Plaza Cajamarca",
                "Av. Vía de Evitamiento Norte - Patio principal",
                -7.1542,
                -78.5042
            ),
            PuntoSeguroSugerido(
                "Campus UPN Cajamarca",
                "Av. Vía de Evitamiento Norte Cuadra 15 - Puerta principal",
                -7.1503,
                -78.5060
            ),
            PuntoSeguroSugerido(
                "Complejo Qhapaq Ñan",
                "Av. Alameda de los Incas - Explanada principal",
                -7.1636,
                -78.4998
            ),
            PuntoSeguroSugerido(
                "Plazuela Belén",
                "Jr. Belén con Jr. Junín - Centro Histórico",
                -7.1588,
                -78.5186
            )
        )
    }

    // Si el punto guardado coincide con alguno de la lista, usar su coordenada exacta actualizada
    val puntoCoincidente = puntosSeguros.find {
        it.nombre.equals(intercambio.puntoEncuentroNombre, ignoreCase = true) ||
                (intercambio.puntoEncuentroNombre.contains("Quinde", ignoreCase = true) && it.nombre.contains("MegaPlaza"))
    }

    val latInicial = when {
        puntoCoincidente != null -> puntoCoincidente.latitud
        intercambio.puntoEncuentroLat != 0.0 -> intercambio.puntoEncuentroLat
        else -> puntosSeguros.first().latitud
    }
    val lngInicial = when {
        puntoCoincidente != null -> puntoCoincidente.longitud
        intercambio.puntoEncuentroLng != 0.0 -> intercambio.puntoEncuentroLng
        else -> puntosSeguros.first().longitud
    }

    var nombreSeleccionado by remember {
        mutableStateOf(
            puntoCoincidente?.nombre
                ?: intercambio.puntoEncuentroNombre.ifBlank { puntosSeguros.first().nombre }
        )
    }
    var direccionSeleccionada by remember {
        mutableStateOf(
            puntoCoincidente?.direccion
                ?: intercambio.puntoEncuentroDireccion.ifBlank { puntosSeguros.first().direccion }
        )
    }
    var latSeleccionada by remember { mutableDoubleStateOf(latInicial) }
    var lngSeleccionada by remember { mutableDoubleStateOf(lngInicial) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = PureWhiteSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Punto de Encuentro Seguro",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkSlateText
                    )
                    Text(
                        text = "Elige un punto seguro o toca directamente en el mapa real",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = DarkSlateText)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chips horizontales con los lugares seguros sugeridos
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(puntosSeguros) { punto ->
                    val activo = nombreSeleccionado == punto.nombre
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (activo) EmeraldPrimary else LightGreenTint,
                        modifier = Modifier.clickable {
                            nombreSeleccionado = punto.nombre
                            direccionSeleccionada = punto.direccion
                            latSeleccionada = punto.latitud
                            lngSeleccionada = punto.longitud
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (activo) Color.White else DeepGreenBrand,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = punto.nombre,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activo) Color.White else DeepGreenBrand
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mapa interactivo real con carpeta de caché limpia (osmdroid_v2) y cabeceras HTTP válidas

            // Mapa interactivo real de OpenStreetMap (RF-18)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(255.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.5.dp, EmeraldPrimary, RoundedCornerShape(18.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        val config = Configuration.getInstance()
                        config.load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

                        // 1. Identificación única, válida y sin "com.example" (Cumple política de OSM)
                        config.userAgentValue = "CambiazoPeruApp/1.0 (https://github.com/kevin-158/Cambiazo-Plataforma-De-Intercambio; contacto@upn.pe)"
                        config.additionalHttpRequestProperties.clear()

                        // 2. Limitar hilos de descarga para cumplir con el "Rate Limit" de OSM
                        config.tileDownloadThreads = 2
                        config.tileDownloadMaxQueueSize = 20

                        // 3. Fuente oficial OpenStreetMap ( Espejo OSM-FR + OSM.org sin bloqueo ni API Key )
                        val fuenteOsmReal = object : XYTileSource(
                            "OpenStreetMap_Cambiazo_V3",
                            1,
                            19,
                            256,
                            ".png",
                            arrayOf(
                                "https://a.tile.openstreetmap.fr/osmfr/",
                                "https://b.tile.openstreetmap.fr/osmfr/",
                                "https://c.tile.openstreetmap.fr/osmfr/"
                            )
                        ) {
                            override fun getTileURLString(pMapTileIndex: Long): String {
                                return baseUrl +
                                        MapTileIndex.getZoom(pMapTileIndex) + "/" +
                                        MapTileIndex.getX(pMapTileIndex) + "/" +
                                        MapTileIndex.getY(pMapTileIndex) + mImageFilenameEnding
                            }
                        }

                        MapView(ctx).apply {
                            setTileSource(fuenteOsmReal)
                            setMultiTouchControls(true)
                            setUseDataConnection(true)
                            controller.setZoom(16.5)
                            controller.setCenter(GeoPoint(latSeleccionada, lngSeleccionada))
                        }
                    },
                    update = { mapView ->
                        val geoPoint = GeoPoint(latSeleccionada, lngSeleccionada)
                        mapView.controller.setZoom(17.2)
                        mapView.controller.setCenter(geoPoint)
                        mapView.overlays.clear()

                        // Permitir tocar cualquier calle del mapa para ubicar el pin
                        val receptorToques = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                if (p != null) {
                                    latSeleccionada = p.latitude
                                    lngSeleccionada = p.longitude
                                    nombreSeleccionado = "Punto seleccionado en el mapa"
                                    direccionSeleccionada = "Cajamarca (%.4f, %.4f)".format(p.latitude, p.longitude)
                                }
                                return true
                            }

                            override fun longPressHelper(p: GeoPoint?): Boolean = false
                        }
                        mapView.overlays.add(MapEventsOverlay(receptorToques))

                        // Marcador visual en la coordenada seleccionada
                        val marker = Marker(mapView).apply {
                            position = geoPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = nombreSeleccionado
                            snippet = direccionSeleccionada
                        }
                        mapView.overlays.add(marker)
                        marker.showInfoWindow()
                        mapView.invalidate()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resumen de la dirección seleccionada debajo del mapa
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = InputBackground,
                border = BorderStroke(1.dp, InputBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = DeepGreenBrand,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = nombreSeleccionado,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlateText
                        )
                        Text(
                            text = direccionSeleccionada,
                            fontSize = 12.sp,
                            color = MutedText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    onEnviarPunto(
                        nombreSeleccionado,
                        direccionSeleccionada,
                        latSeleccionada,
                        lngSeleccionada
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                contentPadding = PaddingValues(vertical = 15.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Proponer este Punto de Encuentro",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}


// Pantalla 9: Modal de Calificación Bilateral de 1 a 5 estrellas (RF-25, RF-26 y RF-27)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ModalCalificarTrueque(
    otroUsuarioNombre: String,
    articuloNombre: String,
    cargando: Boolean,
    onDismiss: () -> Unit,
    onEnviarCalificacion: (Int, String, List<String>) -> Unit
) {
    var estrellasSeleccionadas by remember { mutableIntStateOf(5) }
    var comentario by remember { mutableStateOf("") }
    var errorValidacion by remember { mutableStateOf("") }

    val opcionesEtiquetas = listOf(
        "Puntual",
        "Amigable",
        "Producto como se describía",
        "Buena comunicación",
        "Lugar seguro"
    )
    val etiquetasElegidas = remember {
        mutableStateListOf("Puntual", "Producto como se describía")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = PureWhiteSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(LightGreenTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "¡Cambiazo Completado!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkSlateText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "¿Cómo fue tu experiencia intercambiando \"$articuloNombre\" con @$otroUsuarioNombre?",
                fontSize = 14.sp,
                color = MutedText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Estrellas seleccionables de 1 a 5 (RF-26)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    val activa = i <= estrellasSeleccionadas
                    Icon(
                        imageVector = if (activa) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "$i estrellas",
                        tint = if (activa) EmeraldPrimary else InputBorder,
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { estrellasSeleccionadas = i }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "¿Qué destacarías del encuentro?",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkSlateText
            )
            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                opcionesEtiquetas.forEach { tag ->
                    val seleccionado = etiquetasElegidas.contains(tag)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (seleccionado) EmeraldPrimary else LightGreenTint,
                        modifier = Modifier.clickable {
                            if (seleccionado) {
                                etiquetasElegidas.remove(tag)
                            } else {
                                etiquetasElegidas.add(tag)
                            }
                        }
                    ) {
                        Text(
                            text = tag,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (seleccionado) Color.White else DeepGreenBrand,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = comentario,
                onValueChange = {
                    comentario = it
                    errorValidacion = ""
                },
                placeholder = {
                    Text(
                        text = "Escribe una breve reseña sobre el usuario y el estado del artículo...",
                        fontSize = 13.sp,
                        color = MutedText
                    )
                },
                minLines = 3,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputBackground,
                    unfocusedContainerColor = InputBackground,
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = InputBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorValidacion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorValidacion,
                    color = ErrorRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (comentario.trim().length < 5) {
                        errorValidacion = "Escribe un comentario de al menos 5 caracteres."
                        return@Button
                    }
                    onEnviarCalificacion(
                        estrellasSeleccionadas,
                        comentario.trim(),
                        etiquetasElegidas.toList()
                    )
                },
                enabled = !cargando,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                contentPadding = PaddingValues(vertical = 15.dp),
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
                        text = "Enviar Calificación",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}