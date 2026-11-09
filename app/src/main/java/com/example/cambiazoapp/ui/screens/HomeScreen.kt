package com.example.cambiazoapp.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cambiazoapp.model.Producto
import com.example.cambiazoapp.model.Usuario
import com.example.cambiazoapp.ui.components.CambiazoImagen
import com.example.cambiazoapp.ui.theme.DarkSlateText
import com.example.cambiazoapp.ui.theme.DeepGreenBrand
import com.example.cambiazoapp.ui.theme.EmeraldPrimary
import com.example.cambiazoapp.ui.theme.InputBackground
import com.example.cambiazoapp.ui.theme.InputBorder
import com.example.cambiazoapp.ui.theme.LightGreenTint
import com.example.cambiazoapp.ui.theme.MutedText
import com.example.cambiazoapp.ui.theme.OffWhiteBackground
import com.example.cambiazoapp.ui.theme.PureWhiteSurface
import com.example.cambiazoapp.utils.UbicacionUtils
import com.example.cambiazoapp.viewmodel.IntercambioViewModel
import com.example.cambiazoapp.viewmodel.ProductoViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth

// Pantalla 3: Catálogo principal interactivo con búsqueda, filtros y distancia real en km (RF-07, RF-08, RF-09)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onIrAPublicar: () -> Unit,
    onIrAInventario: () -> Unit,
    onIrAActividad: () -> Unit,
    onIrADetalleProducto: (String) -> Unit,
    viewModel: ProductoViewModel = viewModel(),
    intercambioViewModel: IntercambioViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val usuarioActualId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val perfilActual by viewModel.perfilUsuarioActual.collectAsState()
    val catalogo by viewModel.catalogo.collectAsState()
    val usuariosMapa by viewModel.usuariosMapa.collectAsState()
    val favoritosIds by viewModel.favoritosIds.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val totalNoLeidos by intercambioViewModel.totalNoLeidos.collectAsState()

    // Coordenadas actuales del usuario para calcular distancia en kilómetros (RF-09)
    var miLatitud by remember { mutableDoubleStateOf(-7.1617) }
    var miLongitud by remember { mutableDoubleStateOf(-78.5128) }

    var textoBusqueda by remember { mutableStateOf("") }
    var buscadorEnfocado by remember { mutableStateOf(false) }
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }

    // Estados del modal de filtros combinables
    var mostrarModalFiltros by remember { mutableStateOf(false) }
    var estadoFisicoFiltro by remember { mutableStateOf("Todos") }
    var ordenSeleccionado by remember { mutableStateOf("Más cercanos a ti") }
    var distanciaMaximaFiltro by remember { mutableStateOf("Cualquier distancia") }

    val focusRequesterBusqueda = remember { FocusRequester() }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Obtiene las coordenadas GPS reales del celular
    @SuppressLint("MissingPermission")
    fun actualizarGpsLocal() {
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                miLatitud = loc.latitude
                miLongitud = loc.longitude
                viewModel.actualizarUbicacionGpsUsuario(usuarioActualId, loc.latitude, loc.longitude)
            }
        }
    }

    val permisoGpsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val ok = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (ok) {
            actualizarGpsLocal()
        }
    }

    // Cargar perfil, catálogo y escuchar mensajes nuevos al entrar a Home
    LaunchedEffect(usuarioActualId) {
        if (usuarioActualId.isNotEmpty()) {
            viewModel.cargarPerfilUsuario(usuarioActualId)
            viewModel.cargarCatalogoDisponible(usuarioActualId)
            intercambioViewModel.cargarMisIntercambios(usuarioActualId)

            val tienePermiso = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (tienePermiso) {
                actualizarGpsLocal()
            }
        }
    }

    LaunchedEffect(perfilActual) {
        val u = perfilActual
        if (u != null && u.latitud != 0.0 && u.longitud != 0.0) {
            miLatitud = u.latitud
            miLongitud = u.longitud
        }
    }

    val categoriasFiltro = listOf(
        "Todos", "Electrónica", "Tecnología", "Libros", "Ropa y accesorios",
        "Hogar", "Deportes", "Herramientas", "Videojuegos", "Música", "Otros"
    )

    val opcionesEstadoFisico = listOf(
        "Todos", "Nuevo", "Como nuevo", "Usado (Buen estado)", "Para repuestos"
    )

    val opcionesOrden = listOf(
        "Más cercanos a ti", "Recientes", "Nombre (A - Z)"
    )

    val opcionesDistancia = listOf(
        "Cualquier distancia", "Menos de 1 km", "Menos de 3 km", "Menos de 5 km"
    )

    val hayFiltrosExtraActivos = estadoFisicoFiltro != "Todos" ||
            distanciaMaximaFiltro != "Cualquier distancia"

    // Filtrar productos por categoría (RF-07), palabras clave (RF-08), estado y distancia real en km (RF-09)
    val productosFiltrados = remember(
        catalogo,
        categoriaSeleccionada,
        textoBusqueda,
        estadoFisicoFiltro,
        ordenSeleccionado,
        distanciaMaximaFiltro,
        miLatitud,
        miLongitud
    ) {
        val filtrados = catalogo.filter { producto ->
            val coincideCategoria = categoriaSeleccionada == "Todos" ||
                    producto.categoria.equals(categoriaSeleccionada, ignoreCase = true)

            val coincideEstado = estadoFisicoFiltro == "Todos" ||
                    producto.estado.equals(estadoFisicoFiltro, ignoreCase = true) ||
                    producto.estado.contains(estadoFisicoFiltro.substringBefore(" "), ignoreCase = true)

            val query = textoBusqueda.trim()
            val coincideBusqueda = query.isEmpty() ||
                    producto.nombre.contains(query, ignoreCase = true) ||
                    producto.descripcion.contains(query, ignoreCase = true) ||
                    producto.categoria.contains(query, ignoreCase = true) ||
                    producto.interesesCambio.contains(query, ignoreCase = true) ||
                    producto.direccionReferencia.contains(query, ignoreCase = true)

            val distKm = UbicacionUtils.calcularDistanciaKm(
                miLatitud, miLongitud, producto.latitud, producto.longitud, producto.id
            )
            val coincideDistancia = when (distanciaMaximaFiltro) {
                "Menos de 1 km" -> distKm <= 1.0
                "Menos de 3 km" -> distKm <= 3.0
                "Menos de 5 km" -> distKm <= 5.0
                else -> true
            }

            coincideCategoria && coincideEstado && coincideBusqueda && coincideDistancia && producto.activo
        }

        when (ordenSeleccionado) {
            "Más cercanos a ti" -> filtrados.sortedBy {
                UbicacionUtils.calcularDistanciaKm(
                    miLatitud, miLongitud, it.latitud, it.longitud, it.id
                )
            }
            "Nombre (A - Z)" -> filtrados.sortedBy { it.nombre.lowercase() }
            else -> filtrados
        }
    }

    // Modal desplegable de filtros avanzados
    if (mostrarModalFiltros) {
        ModalBottomSheet(
            onDismissRequest = { mostrarModalFiltros = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = PureWhiteSurface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filtrar y Ordenar Catálogo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlateText
                    )
                    IconButton(onClick = { mostrarModalFiltros = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar filtros",
                            tint = DarkSlateText
                        )
                    }
                }

                HorizontalDivider(color = InputBorder)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Ordenar por",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    opcionesOrden.forEach { opcion ->
                        val activo = ordenSeleccionado == opcion
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (activo) EmeraldPrimary else LightGreenTint,
                            modifier = Modifier.clickable { ordenSeleccionado = opcion }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                if (activo) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = opcion,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (activo) Color.White else DeepGreenBrand
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Distancia máxima",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    opcionesDistancia.forEach { dist ->
                        val activo = distanciaMaximaFiltro == dist
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (activo) EmeraldPrimary else PureWhiteSurface,
                            border = BorderStroke(1.dp, if (activo) EmeraldPrimary else InputBorder),
                            modifier = Modifier.clickable { distanciaMaximaFiltro = dist }
                        ) {
                            Text(
                                text = dist,
                                fontSize = 12.sp,
                                fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
                                color = if (activo) Color.White else DarkSlateText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Estado del artículo",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    opcionesEstadoFisico.forEach { opcion ->
                        val activo = estadoFisicoFiltro == opcion
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (activo) EmeraldPrimary else PureWhiteSurface,
                            border = BorderStroke(1.dp, if (activo) EmeraldPrimary else InputBorder),
                            modifier = Modifier.clickable { estadoFisicoFiltro = opcion }
                        ) {
                            Text(
                                text = opcion,
                                fontSize = 12.sp,
                                fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
                                color = if (activo) Color.White else DarkSlateText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            categoriaSeleccionada = "Todos"
                            estadoFisicoFiltro = "Todos"
                            distanciaMaximaFiltro = "Cualquier distancia"
                            ordenSeleccionado = "Más cercanos a ti"
                            mostrarModalFiltros = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, InputBorder),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Restablecer", color = DarkSlateText, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { mostrarModalFiltros = false },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Ver (${productosFiltrados.size}) artículos",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }

    val nombreCortoSaludo = perfilActual?.nombre?.trim()?.split(" ")?.firstOrNull() ?: "Usuario"
    val ciudadSaludo = perfilActual?.ciudad?.takeIf { it.isNotBlank() } ?: "Cajamarca"

    // Determinar si en la barra inferior se ilumina "inicio" o "buscar"
    val seccionBarraActiva = if (buscadorEnfocado || textoBusqueda.isNotEmpty()) "buscar" else "inicio"

    Scaffold(
        containerColor = OffWhiteBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            CambiazoBottomNavigationBar(
                seccionActiva = seccionBarraActiva,
                totalNoLeidos = totalNoLeidos,
                onHomeClick = {
                    focusManager.clearFocus()
                    buscadorEnfocado = false
                    categoriaSeleccionada = "Todos"
                    estadoFisicoFiltro = "Todos"
                    distanciaMaximaFiltro = "Cualquier distancia"
                    textoBusqueda = ""
                },
                onSearchClick = {
                    try {
                        focusRequesterBusqueda.requestFocus()
                    } catch (_: Exception) {
                    }
                },
                onPostClick = onIrAPublicar,
                onActivityClick = onIrAActividad,
                onProfileClick = onIrAInventario
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Cabecera con saludo, ubicación GPS y campana con aviso de mensajes nuevos (RF-16)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hola, $nombreCortoSaludo 👋",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlateText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            permisoGpsLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Ubicación GPS",
                            tint = DeepGreenBrand,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$ciudadSaludo, Perú (GPS activo)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepGreenBrand
                        )
                    }
                }

                // Botón campana con contador en vivo de mensajes nuevos sin leer (RF-16)
                Box {
                    Surface(
                        shape = CircleShape,
                        color = PureWhiteSurface,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { onIrAActividad() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = "Notificaciones de trueque",
                                tint = DarkSlateText,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    if (totalNoLeidos > 0) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE74C3C),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                        ) {
                            Text(
                                text = "$totalNoLeidos",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Barra de búsqueda dedicada combinable con filtros (RF-08)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    placeholder = {
                        Text(
                            text = "Buscar artículos, intereses o zona...",
                            color = MutedText,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = DarkSlateText.copy(alpha = 0.7f)
                        )
                    },
                    trailingIcon = {
                        if (textoBusqueda.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    textoBusqueda = ""
                                    focusManager.clearFocus()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar búsqueda",
                                    tint = MutedText
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PureWhiteSurface,
                        unfocusedContainerColor = PureWhiteSurface,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = InputBorder,
                        focusedTextColor = DarkSlateText,
                        unfocusedTextColor = DarkSlateText
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequesterBusqueda)
                        .onFocusChanged { buscadorEnfocado = it.isFocused }
                )

                Box {
                    Surface(
                        shape = CircleShape,
                        color = if (hayFiltrosExtraActivos) EmeraldPrimary else PureWhiteSurface,
                        border = BorderStroke(
                            1.dp,
                            if (hayFiltrosExtraActivos) EmeraldPrimary else InputBorder
                        ),
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { mostrarModalFiltros = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Opciones de filtrado",
                                tint = if (hayFiltrosExtraActivos) Color.White else DarkSlateText
                            )
                        }
                    }

                    if (hayFiltrosExtraActivos) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(DeepGreenBrand)
                                .border(1.5.dp, PureWhiteSurface, CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Banner de aviso si el usuario tiene mensajes nuevos sin leer (RF-16)
            if (totalNoLeidos > 0) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = LightGreenTint,
                    border = BorderStroke(1.dp, EmeraldPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onIrAActividad() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = EmeraldPrimary,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$totalNoLeidos",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tienes mensajes o propuestas nuevas en Trueques",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepGreenBrand
                            )
                        }
                        Text(
                            text = "Ver →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DeepGreenBrand
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3. Chips horizontales de categorías (RF-07)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categoriasFiltro) { categoria ->
                    val seleccionado = categoriaSeleccionada == categoria
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = if (seleccionado) EmeraldPrimary else LightGreenTint,
                        modifier = Modifier.clickable { categoriaSeleccionada = categoria }
                    ) {
                        Text(
                            text = categoria,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (seleccionado) Color.White else DeepGreenBrand,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Encabezado "Cerca de ti" + botón Actualizar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cerca de ti (${productosFiltrados.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            viewModel.cargarCatalogoDisponible(usuarioActualId)
                            intercambioViewModel.cargarMisIntercambios(usuarioActualId)
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar catálogo",
                        tint = DeepGreenBrand,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Actualizar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepGreenBrand
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Grilla de artículos del catálogo con distancia real en km (RF-07, RF-08, RF-09)
            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EmeraldPrimary)
                }
            } else if (productosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (catalogo.isEmpty()) {
                                "Aún no hay artículos publicados por otros usuarios."
                            } else {
                                "No encontramos artículos con esa búsqueda o filtro."
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkSlateText,
                            textAlign = TextAlign.Center
                        )

                        if (categoriaSeleccionada != "Todos" || textoBusqueda.isNotEmpty() || hayFiltrosExtraActivos) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    categoriaSeleccionada = "Todos"
                                    estadoFisicoFiltro = "Todos"
                                    distanciaMaximaFiltro = "Cualquier distancia"
                                    textoBusqueda = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Limpiar todos los filtros", color = Color.White)
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(productosFiltrados, key = { it.id }) { producto ->
                        val dueno = usuariosMapa[producto.usuarioId]
                        val esFavorito = favoritosIds.contains(producto.id)
                        val distanciaKm = UbicacionUtils.calcularDistanciaKm(
                            miLatitud,
                            miLongitud,
                            producto.latitud,
                            producto.longitud,
                            producto.id
                        )

                        TarjetaProductoCatalogo(
                            producto = producto,
                            dueno = dueno,
                            distanciaFormateada = UbicacionUtils.formatearDistancia(distanciaKm),
                            esFavorito = esFavorito,
                            onFavoritoClick = { viewModel.alternarFavorito(producto.id) },
                            onClick = { onIrADetalleProducto(producto.id) }
                        )
                    }
                }
            }
        }
    }
}

// Tarjeta individual de artículo dentro del catálogo principal
@Composable
fun TarjetaProductoCatalogo(
    producto: Producto,
    dueno: Usuario?,
    distanciaFormateada: String,
    esFavorito: Boolean,
    onFavoritoClick: () -> Unit,
    onClick: () -> Unit
) {
    val imagenMostrar = producto.imagenUrl.ifBlank {
        producto.imagenes.firstOrNull() ?: ""
    }

    val nombreCortoDueno = remember(dueno) {
        val nom = dueno?.nombre?.trim() ?: "Usuario"
        val partes = nom.split(" ")
        if (partes.size > 1 && partes[1].isNotEmpty()) {
            "${partes[0]} ${partes[1].first().uppercaseChar()}."
        } else {
            nom
        }
    }

    val estrellasDueno = dueno?.promedioEstrellas ?: 5.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .background(InputBackground)
            ) {
                CambiazoImagen(
                    imagenData = imagenMostrar,
                    contentDescription = producto.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Etiqueta de distancia geolocalizada en km (RF-09)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PureWhiteSurface.copy(alpha = 0.92f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = DeepGreenBrand,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = distanciaFormateada,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlateText
                        )
                    }
                }

                // Botón de favorito (RF-12)
                Surface(
                    shape = CircleShape,
                    color = PureWhiteSurface.copy(alpha = 0.92f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(30.dp)
                        .clickable { onFavoritoClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (esFavorito) Color(0xFFE74C3C) else DarkSlateText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Etiqueta de estado físico (RF-20)
                if (producto.estado.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (producto.estado.contains("Nuevo", ignoreCase = true)) {
                            DeepGreenBrand
                        } else {
                            DarkSlateText.copy(alpha = 0.85f)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = producto.estado.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = producto.nombre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                val textoBusco = producto.interesesCambio.ifBlank { "Abierto a ofertas" }
                Text(
                    text = "⇄ Busco: $textoBusco",
                    fontSize = 12.sp,
                    color = MutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(LightGreenTint),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!dueno?.fotoUrl.isNullOrBlank()) {
                                CambiazoImagen(
                                    imagenData = dueno?.fotoUrl,
                                    contentDescription = nombreCortoDueno,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = nombreCortoDueno.take(1).uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepGreenBrand
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = nombreCortoDueno,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = DarkSlateText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "%.1f".format(estrellasDueno),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlateText
                        )
                    }
                }
            }
        }
    }
}

// Barra de navegación inferior global en español con resaltado verde según la sección activa
@Composable
fun CambiazoBottomNavigationBar(
    seccionActiva: String = "inicio", // "inicio", "buscar", "trueques", "perfil"
    totalNoLeidos: Int = 0,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onPostClick: () -> Unit,
    onActivityClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        color = PureWhiteSurface,
        shadowElevation = 14.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Inicio
            val esInicio = seccionActiva == "inicio"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (esInicio) EmeraldPrimary.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onHomeClick() }
                    .padding(vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Inicio",
                    tint = if (esInicio) DeepGreenBrand else DarkSlateText,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Inicio",
                    fontSize = 10.sp,
                    fontWeight = if (esInicio) FontWeight.Bold else FontWeight.Normal,
                    color = if (esInicio) DeepGreenBrand else DarkSlateText,
                    maxLines = 1
                )
            }

            // 2. Buscar
            val esBuscar = seccionActiva == "buscar"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (esBuscar) EmeraldPrimary.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onSearchClick() }
                    .padding(vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = if (esBuscar) DeepGreenBrand else DarkSlateText,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Buscar",
                    fontSize = 10.sp,
                    fontWeight = if (esBuscar) FontWeight.Bold else FontWeight.Normal,
                    color = if (esBuscar) DeepGreenBrand else DarkSlateText,
                    maxLines = 1
                )
            }

            // 3. Publicar (Botón central circular verde +)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .offset(y = (-4).dp)
                    .clickable { onPostClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary)
                        .border(2.dp, PureWhiteSurface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Publicar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Publicar",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkSlateText,
                    maxLines = 1
                )
            }

            // 4. Trueques (con globo de mensajes nuevos en tiempo real - RF-16)
            val esTrueques = seccionActiva == "trueques"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (esTrueques) EmeraldPrimary.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onActivityClick() }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Trueques",
                        tint = if (esTrueques) DeepGreenBrand else DarkSlateText,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Trueques",
                        fontSize = 10.sp,
                        fontWeight = if (esTrueques) FontWeight.Bold else FontWeight.Normal,
                        color = if (esTrueques) DeepGreenBrand else DarkSlateText,
                        maxLines = 1
                    )
                }

                if (totalNoLeidos > 0) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE74C3C),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = "$totalNoLeidos",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // 5. Perfil
            val esPerfil = seccionActiva == "perfil"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (esPerfil) EmeraldPrimary.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onProfileClick() }
                    .padding(vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = "Perfil",
                    tint = if (esPerfil) DeepGreenBrand else DarkSlateText,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Perfil",
                    fontSize = 10.sp,
                    fontWeight = if (esPerfil) FontWeight.Bold else FontWeight.Normal,
                    color = if (esPerfil) DeepGreenBrand else DarkSlateText,
                    maxLines = 1
                )
            }
        }
    }
}