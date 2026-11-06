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
import androidx.compose.ui.layout.ContentScale
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PuntoSeguroSugerido(
    val nombre: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double
)

/**
 * Pantalla 6: Chat de Negociacion en Tiempo Real (RF-16), Mapa de Puntos Seguros (RF-18)
 * y Pantalla 9: Modal de Calificacion Bilateral (RF-25, RF-26, RF-27).
 */
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

    // Inicializar configuracion de OpenStreetMap (osmdroid) para el mapa real (RF-18)
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Iniciar y detener la escucha en tiempo real del chat al entrar/salir de la pantalla (RF-16)
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

    // Desplazar automaticamente al ultimo mensaje cuando llega un mensaje nuevo (RF-16)
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

    val soySolicitante = usuarioActualId == inter.usuarioSolicitanteId
    val otroUsuarioId = if (soySolicitante) inter.usuarioPropietarioId else inter.usuarioSolicitanteId
    val otroUsuarioNombre = if (soySolicitante) {
        inter.usuarioPropietarioNombre
    } else {
        inter.usuarioSolicitanteNombre
    }

    val yaCalifiqueEsteTrueque = if (soySolicitante) {
        inter.calificadoPorSolicitante
    } else {
        inter.calificadoPorPropietario
    }

    // =========================================================================
    // MODAL RF-18: PROPONER / VISUALIZAR PUNTO DE ENCUENTRO SEGURO EN MAPA REAL
    // =========================================================================
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

    // =========================================================================
    // PANTALLA 9 (MODAL RF-25, RF-26, RF-27): EVALUACIÓN BILATERAL DE TRUEQUE
    // =========================================================================
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

    Scaffold(
        containerColor = OffWhiteBackground,
        bottomBar = {
            // Barra inferior de escritura de mensajes en tiempo real + Boton de Mapa (RF-16 y RF-18)
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
                    // Boton para abrir el Mapa de Puntos de Encuentro Seguros (RF-18)
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

                    // Boton de enviar mensaje en tiempo real (RF-16)
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
                .statusBarsPadding()
        ) {
            // 1. Cabecera del Chat con acceso al Perfil Público del otro usuario (RF-11)
            Surface(
                color = PureWhiteSurface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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

                        Column {
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
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Boton rapido en cabecera para ver/proponer Punto Seguro en Mapa (RF-18)
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

            // 2. Tarjeta Fija de Estado del Trueque y Punto de Encuentro Acordado (Mockup Pantalla 6)
            TarjetaResumenNegociacionFija(
                intercambio = inter,
                soySolicitante = soySolicitante,
                yaCalifique = yaCalifiqueEsteTrueque,
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
                },
                usuarioActualId = usuarioActualId
            )

            // 3. Historial de Mensajes en Tiempo Real (RF-16)
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Aviso de seguridad de Cambiazo (Mockup Pantalla 6)
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

/**
 * Tarjeta superior fija dentro del chat (Mockup Pantalla 6: Chat y Estado de Trueque).
 * Muestra los 2 articulos, el estado actual, el punto de encuentro acordado en el mapa
 * y los botones para Aceptar, Completar o Calificar el trueque.
 */
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
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
            }

            // RF-18: Mostrar el Punto de Encuentro Propuesto o Acordado visible para ambos usuarios
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

                        // Si el otro usuario propuso el punto y aun no esta confirmado, mostrar boton "Aceptar punto"
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

            // Botones de acción según el estado de la propuesta (RF-17 y RF-25)
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

/**
 * Burbuja individual de mensaje o tarjeta de Punto de Encuentro en el Chat (Mockup Pantalla 6).
 */
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

    // Si es un mensaje del sistema (confirmacion)
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
            // Tarjeta especial con vista de Punto de Encuentro Seguro en el chat (RF-18)
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
            // Burbuja de texto normal (RF-16)
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

/**
 * Modal con Mapa Real Interactivo (OpenStreetMap) para proponer y visualizar
 * Puntos de Encuentro Seguros dentro de la negociacion (Cumple con RF-18).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalPuntoEncuentroMapa(
    intercambio: Intercambio,
    onDismiss: () -> Unit,
    onEnviarPunto: (String, String, Double, Double) -> Unit
) {
    // Lista de Puntos Seguros concurridos en Cajamarca
    val puntosSeguros = remember {
        listOf(
            PuntoSeguroSugerido(
                "C.C. El Quinde Cajamarca",
                "Av. Hoyos Rubio - Zona comercial con vigilancia",
                -7.1542,
                -78.5058
            ),
            PuntoSeguroSugerido(
                "Plaza de Armas de Cajamarca",
                "Centro Histórico - Zona pública monitoreada",
                -7.1617,
                -78.5128
            ),
            PuntoSeguroSugerido(
                "Real Plaza Cajamarca",
                "Av. Vía de Evitamiento Norte - Patio principal",
                -7.1506,
                -78.5051
            ),
            PuntoSeguroSugerido(
                "Plazuela Belén",
                "Jr. Belén - Centro de Cajamarca",
                -7.1639,
                -78.5142
            ),
            PuntoSeguroSugerido(
                "Campus UPN Cajamarca",
                "Av. Vía de Evitamiento Norte - Entrada principal",
                -7.1489,
                -78.5065
            )
        )
    }

    val latInicial = if (intercambio.puntoEncuentroLat != 0.0) {
        intercambio.puntoEncuentroLat
    } else {
        puntosSeguros.first().latitud
    }
    val lngInicial = if (intercambio.puntoEncuentroLng != 0.0) {
        intercambio.puntoEncuentroLng
    } else {
        puntosSeguros.first().longitud
    }

    var nombreSeleccionado by remember {
        mutableStateOf(
            intercambio.puntoEncuentroNombre.ifBlank { puntosSeguros.first().nombre }
        )
    }
    var direccionSeleccionada by remember {
        mutableStateOf(
            intercambio.puntoEncuentroDireccion.ifBlank { puntosSeguros.first().direccion }
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
                Column {
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

            // Chips de Puntos Seguros recomendados
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

            // MAPA REAL INTERACTIVO (OpenStreetMap AndroidView - RF-18)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.5.dp, EmeraldPrimary, RoundedCornerShape(18.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(16.5)
                            controller.setCenter(GeoPoint(latSeleccionada, lngSeleccionada))
                        }
                    },
                    update = { mapView ->
                        val geoPoint = GeoPoint(latSeleccionada, lngSeleccionada)
                        mapView.controller.animateTo(geoPoint)
                        mapView.overlays.clear()

                        // Permitir que el usuario toque cualquier parte del mapa para mover el pin
                        val receptorToques = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                if (p != null) {
                                    latSeleccionada = p.latitude
                                    lngSeleccionada = p.longitude
                                    nombreSeleccionado = "Punto personalizado en el mapa"
                                    direccionSeleccionada = "Coord: %.4f, %.4f".format(p.latitude, p.longitude)
                                }
                                return true
                            }

                            override fun longPressHelper(p: GeoPoint?): Boolean = false
                        }
                        mapView.overlays.add(MapEventsOverlay(receptorToques))

                        // Pin marcador en la ubicacion elegida
                        val marker = Marker(mapView).apply {
                            position = geoPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = nombreSeleccionado
                            snippet = direccionSeleccionada
                        }
                        mapView.overlays.add(marker)
                        mapView.invalidate()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tarjeta con los datos del punto seleccionado
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = InputBackground,
                border = BorderStroke(1.dp, InputBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
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

/**
 * Pantalla 9: Modal de Calificación de Trueque (Cumple con RF-25, RF-26 y RF-27).
 * Permite calificar de 1 a 5 estrellas, elegir chips predefinidos y escribir una reseña.
 */
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

    // RF-27: Chips seleccionables con aspectos destacados del encuentro
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
            // Icono de exito superior (Mockup Pantalla 9)
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

            // Selector interactivo de 1 a 5 Estrellas (RF-26)
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

            // Chips de aspectos destacados (RF-27)
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

            // Comentario sobre el trueque (RF-26)
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