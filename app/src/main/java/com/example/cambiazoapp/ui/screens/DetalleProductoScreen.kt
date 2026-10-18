package com.example.cambiazoapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontStyle
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
import com.example.cambiazoapp.viewmodel.IntercambioViewModel
import com.example.cambiazoapp.viewmodel.ProductoViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * Pantalla 4: Detalle del Articulo (RF-10, RF-11, RF-12)
 * Incluye ademas la Pantalla 5: Modal de Propuesta de Trueque (RF-13, RF-14, RF-15).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProductoScreen(
    productoId: String,
    onVolver: () -> Unit,
    onIrAPublicarNuevo: () -> Unit,
    onPropuestaEnviadaIrATrueques: () -> Unit,
    viewModel: ProductoViewModel = viewModel(),
    intercambioViewModel: IntercambioViewModel = viewModel()
) {
    val context = LocalContext.current
    val usuarioActualId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Estados del producto visitado y de su propietario (RF-10 y RF-11)
    val producto by viewModel.productoSeleccionado.collectAsState()
    val propietario by viewModel.propietarioProducto.collectAsState()
    val misProductos by viewModel.productos.collectAsState()
    val favoritosIds by viewModel.favoritosIds.collectAsState()
    val cargandoProducto by viewModel.cargando.collectAsState()

    // Estados para el Modal de Propuesta de Trueque (Pantalla 5 - RF-13)
    var mostrarModalPropuesta by remember { mutableStateOf(false) }
    var miArticuloElegido by remember { mutableStateOf<Producto?>(null) }
    var mensajePropuesta by remember { mutableStateOf("") }
    var errorModal by remember { mutableStateOf("") }

    val cargandoIntercambio by intercambioViewModel.cargando.collectAsState()
    val mensajeIntercambio by intercambioViewModel.mensaje.collectAsState()

    // Cargar los datos del producto visitado y tambien mi propio inventario (para RF-13)
    LaunchedEffect(productoId, usuarioActualId) {
        viewModel.cargarProductoPorId(productoId)
        if (usuarioActualId.isNotEmpty()) {
            viewModel.cargarProductosUsuario(usuarioActualId)
        }
    }

    // Filtrar unicamente mis articulos que esten ACTIVOS (no pausados) para ofrecer en trueque (RF-13)
    val misArticulosActivos = remember(misProductos) {
        misProductos.filter { it.activo }
    }

    // Seleccionar por defecto el primer articulo activo si aun no se ha elegido ninguno
    LaunchedEffect(misArticulosActivos, mostrarModalPropuesta) {
        if (mostrarModalPropuesta && miArticuloElegido == null && misArticulosActivos.isNotEmpty()) {
            miArticuloElegido = misArticulosActivos.first()
        }
    }

    if (cargandoProducto && producto == null) {
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

    val prod = producto
    if (prod == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhiteBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "El artículo ya no está disponible.",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onVolver,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Volver al catálogo", color = Color.White)
                }
            }
        }
        return
    }

    // Lista de imagenes para la galeria deslizable (RF-10)
    val listaFotos = prod.imagenes.ifEmpty {
        if (prod.imagenUrl.isNotBlank()) listOf(prod.imagenUrl) else emptyList()
    }
    val pagerState = rememberPagerState(pageCount = { maxOf(listaFotos.size, 1) })
    val esFavorito = favoritosIds.contains(prod.id)
    val esMiPropioProducto = prod.usuarioId == usuarioActualId
    val nombreDueno = propietario?.nombre ?: "Usuario Cambiazo"

    // =========================================================================
    // PANTALLA 5: MODAL DE PROPUESTA DE TRUEQUE (RF-13, RF-14 y RF-15)
    // =========================================================================
    if (mostrarModalPropuesta) {
        ModalBottomSheet(
            onDismissRequest = {
                mostrarModalPropuesta = false
                errorModal = ""
                intercambioViewModel.limpiarMensaje()
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = PureWhiteSurface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Titulo y subtitulo del modal (Mockup Pantalla 5)
                Text(
                    text = "Proponer un Cambiazo",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkSlateText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Selecciona qué artículo de tu inventario deseas ofrecer a cambio",
                    fontSize = 14.sp,
                    color = MutedText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 1. Seccion: ESTÁS SOLICITANDO
                Text(
                    text = "ESTÁS SOLICITANDO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFEDF4FF),
                    border = BorderStroke(1.dp, Color(0xFFD1E4FB))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CambiazoImagen(
                            imagenData = listaFotos.firstOrNull() ?: "",
                            contentDescription = prod.nombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = prod.nombre,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlateText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = MutedText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Propiedad de ",
                                    fontSize = 13.sp,
                                    color = MutedText
                                )
                                Text(
                                    text = "@${nombreDueno.replace(" ", "")}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepGreenBrand
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // 2. Seccion RF-13: Selecciona tu articulo para el trueque
                Text(
                    text = "Selecciona tu artículo para el trueque:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Carrusel horizontal con mis articulos activos + opcion de publicar nuevo (RF-13 y RF-15)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(misArticulosActivos, key = { it.id }) { miProd ->
                        val seleccionado = miArticuloElegido?.id == miProd.id
                        val imgMiProd = miProd.imagenUrl.ifBlank {
                            miProd.imagenes.firstOrNull() ?: ""
                        }

                        Card(
                            modifier = Modifier
                                .width(155.dp)
                                .clickable {
                                    miArticuloElegido = miProd
                                    errorModal = ""
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
                            border = BorderStroke(
                                width = if (seleccionado) 2.5.dp else 1.dp,
                                color = if (seleccionado) EmeraldPrimary else InputBorder
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                ) {
                                    CambiazoImagen(
                                        imagenData = imgMiProd,
                                        contentDescription = miProd.nombre,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Icono de check verde cuando el articulo esta seleccionado (Mockup Pantalla 5)
                                    if (seleccionado) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(EmeraldPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Seleccionado",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = miProd.nombre,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkSlateText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (seleccionado) LightGreenTint else Color(0xFFEDF4FF)
                                    ) {
                                        Text(
                                            text = "Estado: ${miProd.estado.ifBlank { "Bueno" }}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (seleccionado) DeepGreenBrand else DarkSlateText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // RF-15: Tarjeta al final para publicar un nuevo articulo desde la misma ventana
                    item {
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(175.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(LightGreenTint.copy(alpha = 0.45f))
                                .border(
                                    width = 1.5.dp,
                                    color = EmeraldPrimary,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    mostrarModalPropuesta = false
                                    onIrAPublicarNuevo()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Publicar nuevo",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Publicar nuevo artículo",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepGreenBrand,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                if (misArticulosActivos.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No tienes artículos activos en tu inventario. Publica uno con el botón '+' para poder proponer un trueque.",
                        fontSize = 12.sp,
                        color = ErrorRed,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Seccion RF-14: Mensaje personalizado opcional
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        tint = DeepGreenBrand,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mensaje (Opcional)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlateText
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = mensajePropuesta,
                    onValueChange = { mensajePropuesta = it },
                    placeholder = {
                        Text(
                            text = "Añade un mensaje para el dueño (ej. \"¡Hola! Te ofrezco mi artículo en excelente estado si te interesa...\")",
                            color = MutedText,
                            fontSize = 13.sp
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

                // Mostrar mensajes de validacion o error
                if (errorModal.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorModal,
                        color = ErrorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                if (mensajeIntercambio.isNotEmpty() && !mensajeIntercambio.contains("enviada")) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = mensajeIntercambio,
                        color = ErrorRed,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // 4. Boton Principal: Enviar Propuesta de Trueque (RF-13)
                Button(
                    onClick = {
                        val seleccionado = miArticuloElegido
                        if (seleccionado == null) {
                            errorModal = "Selecciona un artículo de tu inventario para ofrecer a cambio."
                            return@Button
                        }

                        intercambioViewModel.enviarPropuestaTrueque(
                            productoOfrecido = seleccionado,
                            productoSolicitado = prod,
                            usuarioSolicitanteId = usuarioActualId,
                            nombrePropietario = nombreDueno,
                            mensajeOpcional = mensajePropuesta,
                            onExito = {
                                mostrarModalPropuesta = false
                                Toast.makeText(
                                    context,
                                    "Propuesta enviada como Pendiente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onPropuestaEnviadaIrATrueques()
                            }
                        )
                    },
                    enabled = !cargandoIntercambio && misArticulosActivos.isNotEmpty(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.White,
                        disabledContainerColor = EmeraldPrimary.copy(alpha = 0.45f)
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (cargandoIntercambio) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text(
                            text = "Enviar Propuesta de Trueque",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }

    // =========================================================================
    // CONTENIDO PRINCIPAL DE LA PANTALLA 4: DETALLE DEL ARTÍCULO
    // =========================================================================
    Scaffold(
        containerColor = OffWhiteBackground,
        bottomBar = {
            Surface(
                color = PureWhiteSurface,
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PureWhiteSurface,
                        border = BorderStroke(1.5.dp, DeepGreenBrand),
                        modifier = Modifier
                            .size(50.dp)
                            .clickable(enabled = !esMiPropioProducto && prod.activo) {
                                errorModal = ""
                                intercambioViewModel.limpiarMensaje()
                                mostrarModalPropuesta = true
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Mensaje",
                                tint = DeepGreenBrand
                            )
                        }
                    }

                    Button(
                        onClick = {
                            errorModal = ""
                            intercambioViewModel.limpiarMensaje()
                            mostrarModalPropuesta = true
                        },
                        enabled = !esMiPropioProducto && prod.activo,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = InputBorder
                        ),
                        contentPadding = PaddingValues(vertical = 15.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                esMiPropioProducto -> "Es tu propio artículo"
                                !prod.activo -> "Artículo pausado"
                                else -> "Proponer Trueque"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
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
            // 1. GALERÍA DE IMÁGENES INTERACTIVA (RF-10)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(InputBackground)
            ) {
                if (listaFotos.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        CambiazoImagen(
                            imagenData = listaFotos[page],
                            contentDescription = "${prod.nombre} - Foto ${page + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    CambiazoImagen(
                        imagenData = "",
                        contentDescription = "Sin fotos",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Boton superior izquierdo: Volver
                Surface(
                    shape = CircleShape,
                    color = PureWhiteSurface.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
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

                // Botones superiores derechos: Compartir y Favorito (RF-12)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PureWhiteSurface.copy(alpha = 0.9f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir",
                                tint = DarkSlateText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = PureWhiteSurface.copy(alpha = 0.9f),
                        modifier = Modifier
                            .size(42.dp)
                            .clickable { viewModel.alternarFavorito(prod.id) }
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

                // Contador inferior izquierdo: "1/4" (RF-10)
                if (listaFotos.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PureWhiteSurface.copy(alpha = 0.92f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = DeepGreenBrand,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${pagerState.currentPage + 1}/${listaFotos.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlateText
                            )
                        }
                    }

                    // Indicadores de puntos (dots) centrados abajo
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(listaFotos.size) { index ->
                            val activo = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .size(if (activo) 9.dp else 7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (activo) EmeraldPrimary else PureWhiteSurface.copy(alpha = 0.7f)
                                    )
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                // 2. TARJETA DE INFORMACIÓN DEL DUEÑO (RF-10 y RF-11)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val ubicacionDueno = listOfNotNull(
                            propietario?.ciudad?.takeIf { it.isNotBlank() },
                            propietario?.barrio?.takeIf { it.isNotBlank() }
                        ).joinToString(" - ").ifEmpty { "Cajamarca Centro" }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(LightGreenTint)
                                .border(2.dp, EmeraldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = nombreDueno.take(1).uppercase(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepGreenBrand
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = nombreDueno,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlateText
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = DeepGreenBrand,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Swapper Experto",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepGreenBrand
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "4.9",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkSlateText
                                )
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = DeepGreenBrand,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = " (Trueques verificados)",
                                    fontSize = 12.sp,
                                    color = MutedText
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MutedText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$ubicacionDueno (a 800m)",
                                    fontSize = 12.sp,
                                    color = MutedText
                                )
                            }
                        }

                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Más opciones",
                                tint = MutedText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3. CHIPS DE CATEGORÍA Y ESTADO FÍSICO (RF-10 / RF-20)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE3EFFF)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = DarkSlateText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = prod.categoria.ifBlank { "General" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkSlateText
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE3EFFF)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = DeepGreenBrand,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = prod.estado.ifBlank { "Buen estado" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkSlateText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. TÍTULO DEL ARTÍCULO
                Text(
                    text = prod.nombre,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkSlateText,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Publicado recientemente en tu zona",
                    fontSize = 12.sp,
                    color = MutedText
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 5. CAJA VERDE: "Lo que busco a cambio" (RF-21)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = LightGreenTint,
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFF39C12),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Lo que busco a cambio:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepGreenBrand
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = prod.interesesCambio.ifBlank { "Abierto a escuchar ofertas de trueque" },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = DarkSlateText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "* Abierto a escuchar otras ofertas locales.",
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = DeepGreenBrand
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 6. CARD DE DESCRIPCIÓN COMPLETA (RF-10)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Descripción",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlateText
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = prod.descripcion.ifBlank { "Sin descripción adicional." },
                            fontSize = 15.sp,
                            color = DarkSlateText.copy(alpha = 0.85f),
                            lineHeight = 23.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}