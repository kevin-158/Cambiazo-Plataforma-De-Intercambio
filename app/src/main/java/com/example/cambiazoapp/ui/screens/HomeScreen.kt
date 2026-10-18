package com.example.cambiazoapp.ui.screens

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
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.cambiazoapp.viewmodel.ProductoViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onIrAPublicar: () -> Unit,
    onIrAInventario: () -> Unit,
    onIrAActividad: () -> Unit,
    onIrADetalleProducto: (String) -> Unit,
    viewModel: ProductoViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val usuarioActualId = auth.currentUser?.uid ?: ""

    var nombreUsuarioActual by remember { mutableStateOf("Usuario") }
    var ubicacionUsuarioActual by remember { mutableStateOf("Cajamarca, Perú") }

    var textoBusqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }

    // Estados del Modal de Filtros
    var mostrarModalFiltros by remember { mutableStateOf(false) }
    var estadoFisicoFiltro by remember { mutableStateOf("Todos") }
    var ordenSeleccionado by remember { mutableStateOf("Recientes") } // "Recientes", "Cercanos", "Nombre A-Z"

    val focusRequesterBusqueda = remember { FocusRequester() }

    val catalogo by viewModel.catalogo.collectAsState()
    val usuariosMapa by viewModel.usuariosMapa.collectAsState()
    val favoritosIds by viewModel.favoritosIds.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    val categoriasFiltro = listOf(
        "Todos",
        "Electrónica",
        "Tecnología",
        "Libros",
        "Ropa y accesorios",
        "Hogar",
        "Deportes",
        "Herramientas",
        "Videojuegos",
        "Música",
        "Otros"
    )

    val opcionesEstadoFisico = listOf(
        "Todos",
        "Nuevo",
        "Como nuevo",
        "Usado (Buen estado)",
        "Para repuestos"
    )

    val opcionesOrden = listOf(
        "Recientes",
        "Cercanos a ti",
        "Nombre (A - Z)"
    )

    val hayFiltrosExtraActivos = estadoFisicoFiltro != "Todos" || ordenSeleccionado != "Recientes"

    // Cargar datos del usuario actual y el catálogo de productos disponibles (RF-07)
    LaunchedEffect(usuarioActualId) {
        if (usuarioActualId.isNotEmpty()) {
            viewModel.cargarCatalogoDisponible(usuarioActualId)
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(usuarioActualId)
                    .get()
                    .await()
                if (doc.exists()) {
                    val nombreCompleto = doc.getString("nombre") ?: "Usuario"
                    nombreUsuarioActual = nombreCompleto.trim().split(" ").firstOrNull() ?: "Usuario"
                    val ciudad = doc.getString("ciudad")?.takeIf { it.isNotBlank() } ?: "Cajamarca"
                    ubicacionUsuarioActual = "$ciudad, Perú"
                }
            } catch (_: Exception) {
            }
        }
    }

    // Filtrado reactivo por categoría (RF-07), estado físico, palabra clave (RF-08) y orden
    val productosFiltrados = remember(
        catalogo,
        categoriaSeleccionada,
        textoBusqueda,
        estadoFisicoFiltro,
        ordenSeleccionado
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
                    producto.interesesCambio.contains(query, ignoreCase = true)

            coincideCategoria && coincideEstado && coincideBusqueda && producto.activo
        }

        when (ordenSeleccionado) {
            "Cercanos a ti" -> filtrados.sortedBy { abs(it.id.hashCode()) % 2900 }
            "Nombre (A - Z)" -> filtrados.sortedBy { it.nombre.lowercase() }
            else -> filtrados
        }
    }

    // Modal desplegable de Filtros al tocar el botón de filtrar
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
                // 1. Cabecera: Saludo, Ubicación y Notificaciones (Mockup Pantalla 3)

            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filtrar y Ordenar",
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
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Ordenar por
                Text(
                    text = "Ordenar por",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )
                Spacer(modifier = Modifier.height(10.dp))

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
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                if (activo) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = opcion,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (activo) Color.White else DeepGreenBrand
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Filtrar por Estado Físico del Artículo
                Text(
                    text = "Estado del artículo",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )
                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    opcionesEstadoFisico.forEach { opcion ->
                        val activo = estadoFisicoFiltro == opcion
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (activo) EmeraldPrimary else PureWhiteSurface,
                            border = BorderStroke(
                                1.dp,
                                if (activo) EmeraldPrimary else InputBorder
                            ),
                            modifier = Modifier.clickable { estadoFisicoFiltro = opcion }
                        ) {
                            Text(
                                text = opcion,
                                fontSize = 13.sp,
                                fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
                                color = if (activo) Color.White else DarkSlateText,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Filtrar por Categoría dentro del modal también
                Text(
                    text = "Categoría",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )
                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoriasFiltro.forEach { cat ->
                        val activo = categoriaSeleccionada == cat
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (activo) DeepGreenBrand else LightGreenTint,
                            modifier = Modifier.clickable { categoriaSeleccionada = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (activo) Color.White else DeepGreenBrand,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Botones de acción del Modal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            categoriaSeleccionada = "Todos"
                            estadoFisicoFiltro = "Todos"
                            ordenSeleccionado = "Recientes"
                            mostrarModalFiltros = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, InputBorder),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Restablecer",
                            color = DarkSlateText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { mostrarModalFiltros = false },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Ver resultados (${productosFiltrados.size})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Scaffold(
        containerColor = OffWhiteBackground,
        bottomBar = {
            CambiazoBottomNavigationBar(
                onHomeClick = {
                    categoriaSeleccionada = "Todos"
                    estadoFisicoFiltro = "Todos"
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
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Cabecera: Saludo, Ubicación y Notificaciones (Mockup Pantalla 3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hola, $nombreUsuarioActual 👋",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlateText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Ubicación",
                            tint = DeepGreenBrand,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = ubicacionUsuarioActual,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepGreenBrand
                        )
                    }
                }

                // Botón campana de notificaciones / actividad
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
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 10.dp, end = 10.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE74C3C))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Barra de Búsqueda + Botón de Filtro (RF-08)
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
                            text = "Buscar libros, ropa, herramientas...",
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
                            IconButton(onClick = { textoBusqueda = "" }) {
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
                )

                // Botón que despliega el Modal de Filtros
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

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Chips de Categorías (RF-07)
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

            // Si hay filtro de estado físico o de orden activo, mostrar chip informativo
            if (hayFiltrosExtraActivos) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Filtros activos:",
                        fontSize = 11.sp,
                        color = MutedText,
                        fontWeight = FontWeight.Medium
                    )
                    if (estadoFisicoFiltro != "Todos") {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = LightGreenTint,
                            modifier = Modifier.clickable { estadoFisicoFiltro = "Todos" }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = estadoFisicoFiltro,
                                    fontSize = 11.sp,
                                    color = DeepGreenBrand,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Quitar filtro",
                                    tint = DeepGreenBrand,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                    if (ordenSeleccionado != "Recientes") {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = LightGreenTint,
                            modifier = Modifier.clickable { ordenSeleccionado = "Recientes" }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = ordenSeleccionado,
                                    fontSize = 11.sp,
                                    color = DeepGreenBrand,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Quitar orden",
                                    tint = DeepGreenBrand,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Encabezado "Cerca de ti" + Acción recargar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cerca de ti",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.cargarCatalogoDisponible(usuarioActualId) }
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

            // 5. Grilla de Artículos del Catálogo (RF-07)
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
                                "No encontramos artículos con los filtros seleccionados."
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkSlateText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (catalogo.isEmpty()) {
                                "Recuerda que aquí aparecen los artículos activos de otros usuarios (los tuyos están en Profile)."
                            } else {
                                "Prueba seleccionando otra categoría o limpiando los filtros."
                            },
                            fontSize = 13.sp,
                            color = MutedText,
                            textAlign = TextAlign.Center
                        )

                        if (categoriaSeleccionada != "Todos" || textoBusqueda.isNotEmpty() || hayFiltrosExtraActivos) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    categoriaSeleccionada = "Todos"
                                    estadoFisicoFiltro = "Todos"
                                    ordenSeleccionado = "Recientes"
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

                        TarjetaProductoCatalogo(
                            producto = producto,
                            dueno = dueno,
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

@Composable
fun TarjetaProductoCatalogo(
    producto: Producto,
    dueno: Usuario?,
    esFavorito: Boolean,
    onFavoritoClick: () -> Unit,
    onClick: () -> Unit
) {
    val imagenMostrar = producto.imagenUrl.ifBlank {
        producto.imagenes.firstOrNull() ?: ""
    }

    val pasosDistancia = remember(producto.id) {
        val hash = abs(producto.id.hashCode())
        val metros = 300 + (hash % 2900)
        if (metros < 1000) "a ${metros}m" else "a %.1f km".format(metros / 1000f)
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
                // Ahora usa CambiazoImagen que decodifica Base64 y URLs sin fallar
                CambiazoImagen(
                    imagenData = imagenMostrar,
                    contentDescription = producto.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Badge superior izquierdo: Distancia (RF-09)
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
                            text = pasosDistancia,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlateText
                        )
                    }
                }

                // Botón superior derecho: Corazón Favorito (RF-12)
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

                // Badge inferior izquierdo: Estado físico del artículo (RF-20)
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(LightGreenTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nombreCortoDueno.take(1).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepGreenBrand
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = nombreCortoDueno,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkSlateText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun CambiazoBottomNavigationBar(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onPostClick: () -> Unit,
    onActivityClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    // navigationBarsPadding() evita que los botones del sistema de Android tapen la barra
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
            // 1. Home
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EmeraldPrimary.copy(alpha = 0.18f))
                    .clickable { onHomeClick() }
                    .padding(vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = DeepGreenBrand,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Home",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepGreenBrand,
                    maxLines = 1
                )
            }

            // 2. Search
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSearchClick() }
                    .padding(vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = DarkSlateText,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Search",
                    fontSize = 10.sp,
                    color = DarkSlateText,
                    maxLines = 1
                )
            }

            // 3. Post (Botón central verde +)
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
                        contentDescription = "Post",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Post",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkSlateText,
                    maxLines = 1
                )
            }

            // 4. Activity
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onActivityClick() }
                    .padding(vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Activity",
                    tint = DarkSlateText,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Activity",
                    fontSize = 10.sp,
                    color = DarkSlateText,
                    maxLines = 1
                )
            }

            // 5. Profile
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onProfileClick() }
                    .padding(vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = "Profile",
                    tint = DarkSlateText,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Profile",
                    fontSize = 10.sp,
                    color = DarkSlateText,
                    maxLines = 1
                )
            }
        }
    }
}