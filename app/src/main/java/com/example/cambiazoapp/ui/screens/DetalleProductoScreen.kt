package com.example.cambiazoapp.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cambiazoapp.model.Producto
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
import com.example.cambiazoapp.utils.UbicacionUtils
import com.example.cambiazoapp.viewmodel.IntercambioViewModel
import com.example.cambiazoapp.viewmodel.ProductoViewModel
import com.google.firebase.auth.FirebaseAuth

// Pantalla 4 y 5: Detalle del Artículo (RF-10, RF-11), Favoritos/Compartir (RF-12) y Modal de Propuesta (RF-13)
/**
 * Pantalla 4: Detalle del Articulo (RF-10, RF-11, RF-12)
 * Incluye ademas la Pantalla 5: Modal de Propuesta de Trueque (RF-13, RF-14, RF-15).
 */
//=========Detalle del Artículo (Al tocar cualquier producto del catálogo)=======

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetalleProductoScreen(
    productoId: String,
    onVolver: () -> Unit,
    onIrAPublicarNuevo: () -> Unit,
    onPropuestaEnviadaIrATrueques: () -> Unit,
    onVerPerfilPublico: (String) -> Unit = {},
    viewModel: ProductoViewModel = viewModel(),
    intercambioViewModel: IntercambioViewModel = viewModel()
) {
    val context = LocalContext.current
    val usuarioActualId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Estados del producto visitado y de su propietario (RF-10 y RF-11)
    val producto by viewModel.productoSeleccionado.collectAsState()
    val propietario by viewModel.propietarioProducto.collectAsState()
    val perfilActual by viewModel.perfilUsuarioActual.collectAsState()
    val misProductos by viewModel.productos.collectAsState()
    val favoritosIds by viewModel.favoritosIds.collectAsState()
    val cargandoProducto by viewModel.cargando.collectAsState()

    val cargandoPropuesta by intercambioViewModel.cargando.collectAsState()
    val mensajeIntercambio by intercambioViewModel.mensaje.collectAsState()

    // Estados para el Modal de Proponer Trueque (RF-13)
    var mostrarModalPropuesta by remember { mutableStateOf(false) }
    var miArticuloElegido by remember { mutableStateOf<Producto?>(null) }
    var mensajePropuesta by remember { mutableStateOf("") }
    var errorPropuesta by remember { mutableStateOf("") }

    // Cargar los datos del producto visitado y tambien mi propio inventario (para RF-13)
    LaunchedEffect(productoId, usuarioActualId) {
        viewModel.cargarProductoPorId(productoId)
        if (usuarioActualId.isNotEmpty()) {
            viewModel.cargarPerfilUsuario(usuarioActualId)
            viewModel.cargarProductosUsuario(usuarioActualId)
        }
    }
    // Filtrar unicamente mis articulos que esten ACTIVOS (no pausados) para ofrecer en trueque (RF-13)
    val misArticulosActivos = remember(misProductos) {
        misProductos.filter { it.activo }
    }

    // Seleccionar por defecto el primer articulo activo si aun no se ha elegido ninguno
    LaunchedEffect(misArticulosActivos) {
        if (miArticuloElegido == null && misArticulosActivos.isNotEmpty()) {
            miArticuloElegido = misArticulosActivos.first()
        }
    }

    val prod = producto
    if (cargandoProducto || prod == null) {
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

    // Verificar en tiempo real si este artículo está en la lista de favoritos del usuario (RF-12)
    val esFavorito = favoritosIds.contains(prod.id)

    val galeriaImagenes = remember(prod) {
        prod.imagenes.ifEmpty {
            if (prod.imagenUrl.isNotBlank()) listOf(prod.imagenUrl) else emptyList()
        }
    }

    val nombreDueno = propietario?.nombre?.takeIf { it.isNotBlank() } ?: "Usuario Cambiazo"
    val promedioEstrellasDueno = propietario?.promedioEstrellas ?: 5.0
    val totalResenasDueno = propietario?.totalResenas ?: 0

    // Calcular distancia real en km entre el usuario actual y el artículo (RF-09)
    val distanciaTexto = remember(perfilActual, prod) {
        val miLat = perfilActual?.latitud ?: -7.1569
        val miLng = perfilActual?.longitud ?: -78.5175
        val km = UbicacionUtils.calcularDistanciaKm(miLat, miLng, prod.latitud, prod.longitud, prod.id)
        UbicacionUtils.formatearDistancia(km)
    }

    // Función para compartir el artículo mediante el selector nativo de Android (WhatsApp, redes, etc.)
    fun compartirArticulo(articulo: Producto) {
        val intereses = articulo.interesesCambio.ifBlank { "Abierto a ofertas" }
        val textoCompartir = buildString {
            append("🔄 *¡Mira este artículo para trueque en Cambiazo!* \n\n")
            append("📦 *Artículo:* ${articulo.nombre}\n")
            append("🏷️ *Categoría:* ${articulo.categoria} (${articulo.estado})\n")
            append("📍 *Ubicación:* ${articulo.direccionReferencia} - Cajamarca\n")
            append("🤝 *Busca a cambio:* $intereses\n\n")
            append("📝 *Descripción:* ${articulo.descripcion}\n\n")
            append("¡Descarga *Cambiazo* y propón un intercambio directo!")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Trueque en Cambiazo: ${articulo.nombre}")
            putExtra(Intent.EXTRA_TEXT, textoCompartir)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartir artículo mediante...")
        context.startActivity(shareIntent)
    }

    // Modal de Propuesta de Trueque (Pantalla 5 / RF-13)
    // =========================================================================
    // PANTALLA 5: MODAL DE PROPUESTA DE TRUEQUE (RF-13, RF-14 y RF-15)
    // =========================================================================

    if (mostrarModalPropuesta) {
        ModalBottomSheet(
            onDismissRequest = { mostrarModalPropuesta = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = PureWhiteSurface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 22.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Proponer Trueque",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkSlateText
                        )
                        Text(
                            text = "Selecciona un artículo de tu inventario para ofrecer",
                            fontSize = 13.sp,
                            color = MutedText
                        )
                    }
                    IconButton(onClick = { mostrarModalPropuesta = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = DarkSlateText)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Comparativa visual en vivo entre Mi Oferta y el Artículo Solicitado (Mockup Pantalla 5)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = LightGreenTint.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tú ofreces", fontSize = 11.sp, color = MutedText)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(PureWhiteSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                if (miArticuloElegido != null) {
                                    CambiazoImagen(
                                        imagenData = miArticuloElegido!!.imagenUrl.ifBlank {
                                            miArticuloElegido!!.imagenes.firstOrNull() ?: ""
                                        },
                                        contentDescription = miArticuloElegido!!.nombre,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text("?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MutedText)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = miArticuloElegido?.nombre ?: "Elige abajo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlateText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = EmeraldPrimary,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Por el artículo", fontSize = 11.sp, color = MutedText)
                            Spacer(modifier = Modifier.height(6.dp))
                            CambiazoImagen(
                                imagenData = galeriaImagenes.firstOrNull() ?: "",
                                contentDescription = prod.nombre,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = prod.nombre,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlateText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tu inventario disponible (${misArticulosActivos.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (misArticulosActivos.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = InputBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Text(
                                text = "Necesitas tener al menos 1 artículo activo en tu inventario para proponer un trueque.",
                                fontSize = 13.sp,
                                color = MutedText,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    mostrarModalPropuesta = false
                                    onIrAPublicarNuevo()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Publicar un artículo ahora", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(misArticulosActivos, key = { it.id }) { itemPropio ->
                            val seleccionado = miArticuloElegido?.id == itemPropio.id
                            Card(
                                modifier = Modifier
                                    .width(135.dp)
                                    .clickable {
                                        miArticuloElegido = itemPropio
                                        errorPropuesta = ""
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (seleccionado) LightGreenTint else PureWhiteSurface
                                ),
                                border = BorderStroke(
                                    width = if (seleccionado) 2.dp else 1.dp,
                                    color = if (seleccionado) EmeraldPrimary else InputBorder
                                )
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(85.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        CambiazoImagen(
                                            imagenData = itemPropio.imagenUrl.ifBlank {
                                                itemPropio.imagenes.firstOrNull() ?: ""
                                            },
                                            contentDescription = itemPropio.nombre,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        if (seleccionado) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(EmeraldPrimary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = itemPropio.nombre,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkSlateText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = mensajePropuesta,
                    onValueChange = { mensajePropuesta = it },
                    label = { Text("Mensaje inicial para @$nombreDueno (opcional)") },
                    placeholder = { Text("¡Hola! Me interesa tu artículo, te ofrezco el mío...") },
                    minLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InputBackground,
                        unfocusedContainerColor = InputBackground,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = InputBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorPropuesta.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorPropuesta,
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (mensajeIntercambio.isNotEmpty() && !mensajeIntercambio.contains("Pendiente")) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = mensajeIntercambio,
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val ofrecido = miArticuloElegido
                        if (ofrecido == null) {
                            errorPropuesta = "Selecciona uno de tus artículos para ofrecer."
                            return@Button
                        }
                        intercambioViewModel.enviarPropuestaTrueque(
                            productoOfrecido = ofrecido,
                            productoSolicitado = prod,
                            usuarioSolicitanteId = usuarioActualId,
                            nombrePropietario = nombreDueno,
                            mensajeOpcional = mensajePropuesta,
                            onExito = {
                                mostrarModalPropuesta = false
                                Toast.makeText(
                                    context,
                                    "¡Propuesta enviada exitosamente!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onPropuestaEnviadaIrATrueques()
                            }
                        )
                    },
                    enabled = !cargandoPropuesta && misArticulosActivos.isNotEmpty(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    contentPadding = PaddingValues(vertical = 15.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (cargandoPropuesta) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enviar Propuesta de Trueque",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    Scaffold(
        containerColor = OffWhiteBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (prod.usuarioId != usuarioActualId) {
                Surface(
                    color = PureWhiteSurface,
                    shadowElevation = 14.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botón rápido inferior de Favorito (RF-12)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (esFavorito) Color(0xFFFFEBEE) else InputBackground,
                            border = BorderStroke(
                                1.dp,
                                if (esFavorito) Color(0xFFE74C3C) else InputBorder
                            ),
                            modifier = Modifier
                                .size(52.dp)
                                .clickable {
                                    viewModel.alternarFavorito(prod.id)
                                    val msg = if (!esFavorito) {
                                        "Agregado a tus favoritos ❤️"
                                    } else {
                                        "Eliminado de tus favoritos"
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Guardar en favoritos",
                                    tint = if (esFavorito) Color(0xFFE74C3C) else DarkSlateText,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Botón principal Proponer Trueque (RF-13)
                        Button(
                            onClick = {
                                intercambioViewModel.limpiarMensaje()
                                mostrarModalPropuesta = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            contentPadding = PaddingValues(vertical = 15.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Proponer Trueque",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
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
                .verticalScroll(rememberScrollState())
        ) {
            // 1. CARRUSEL DE IMÁGENES CON BOTONES DE VOLVER, COMPARTIR Y FAVORITOS (RF-10 y RF-12)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(330.dp)
                    .background(InputBackground)
            ) {
                val pagerState = rememberPagerState(pageCount = { galeriaImagenes.size.coerceAtLeast(1) })

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    CambiazoImagen(
                        imagenData = galeriaImagenes.getOrNull(page) ?: "",
                        contentDescription = "${prod.nombre} imagen ${page + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Barra flotante superior con Volver, Compartir y Corazón de Favoritos (RF-12)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PureWhiteSurface.copy(alpha = 0.92f),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(42.dp)
                            .clickable { onVolver() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = DarkSlateText
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Botón funcional para Compartir el artículo por WhatsApp o redes
                        Surface(
                            shape = CircleShape,
                            color = PureWhiteSurface.copy(alpha = 0.92f),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .size(42.dp)
                                .clickable { compartirArticulo(prod) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Compartir artículo",
                                    tint = DarkSlateText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Botón funcional de Corazón para Guardar/Desmarcar de Favoritos (RF-12)
                        Surface(
                            shape = CircleShape,
                            color = PureWhiteSurface.copy(alpha = 0.92f),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .size(42.dp)
                                .clickable {
                                    viewModel.alternarFavorito(prod.id)
                                    val msg = if (!esFavorito) {
                                        "Agregado a tus favoritos ❤️"
                                    } else {
                                        "Eliminado de tus favoritos"
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorito",
                                    tint = if (esFavorito) Color(0xFFE74C3C) else DarkSlateText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Indicador de páginas del carrusel si hay más de 1 foto
                if (galeriaImagenes.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 14.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSlateText.copy(alpha = 0.55f))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(galeriaImagenes.size) { idx ->
                            val activo = pagerState.currentPage == idx
                            Box(
                                modifier = Modifier
                                    .size(if (activo) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(if (activo) EmeraldPrimary else Color.White.copy(alpha = 0.6f))
                            )
                        }
                    }
                }
            }

            // Contenido detallado del artículo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LightGreenTint
                    ) {
                        Text(
                            text = "${prod.categoria} • ${prod.estado}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepGreenBrand,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = DeepGreenBrand,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = distanciaTexto,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepGreenBrand
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = prod.nombre,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkSlateText
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Ubicación: ${prod.direccionReferencia}, Cajamarca",
                    fontSize = 13.sp,
                    color = MutedText
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. TARJETA DE INFORMACIÓN DEL DUEÑO (RF-11: Toca para abrir su Perfil Público)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onVerPerfilPublico(prod.usuarioId) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(LightGreenTint)
                                    .border(2.dp, EmeraldPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!propietario?.fotoUrl.isNullOrBlank()) {
                                    CambiazoImagen(
                                        imagenData = propietario?.fotoUrl,
                                        contentDescription = nombreDueno,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = nombreDueno.take(1).uppercase(),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepGreenBrand
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = nombreDueno,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkSlateText
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verificado",
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "%.1f".format(promedioEstrellasDueno),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkSlateText
                                    )
                                    Text(
                                        text = " ($totalResenasDueno reseñas • Ver perfil)",
                                        fontSize = 12.sp,
                                        color = DeepGreenBrand,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3. DESCRIPCIÓN DEL ARTÍCULO (RF-10)
                Text(
                    text = "Descripción del artículo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = prod.descripcion,
                    fontSize = 14.sp,
                    color = DarkSlateText.copy(alpha = 0.85f),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = InputBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // 4. INTERESES DE CAMBIO DEL DUEÑO (RF-21)
                Text(
                    text = "¿Qué busca a cambio?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )
                Spacer(modifier = Modifier.height(10.dp))

                val listaIntereses = prod.interesesCambio
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .ifEmpty { listOf("Abierto a cualquier propuesta") }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listaIntereses.forEach { interes ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = LightGreenTint
                        ) {
                            Text(
                                text = "⇄ $interes",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepGreenBrand,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}