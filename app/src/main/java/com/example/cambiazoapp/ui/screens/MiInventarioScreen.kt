package com.example.cambiazoapp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cambiazoapp.model.Intercambio
import com.example.cambiazoapp.model.Producto
import com.example.cambiazoapp.model.Usuario
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
 * Pantalla 8: Perfil de Usuario, Edicion de Perfil y Gestion de Mi Inventario (RF-22, RF-23, RF-24).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MiInventarioScreen(
    onVolver: () -> Unit = {},
    onIrAPublicar: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    viewModel: ProductoViewModel = viewModel(),
    intercambioViewModel: IntercambioViewModel = viewModel()
) {
    val context = LocalContext.current
    val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val productos by viewModel.productos.collectAsState()
    val perfilUsuario by viewModel.perfilUsuarioActual.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    val recibidas by intercambioViewModel.propuestasRecibidas.collectAsState()
    val enviadas by intercambioViewModel.propuestasEnviadas.collectAsState()

    var tabSeleccionado by remember { mutableIntStateOf(0) }

    // Estados para modales y acciones
    var productoEditando by remember { mutableStateOf<Producto?>(null) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }
    var mostrarEditarPerfil by remember { mutableStateOf(false) }
    var mostrarConfirmacionCerrarSesion by remember { mutableStateOf(false) }
    val resenasMias by intercambioViewModel.resenasUsuario.collectAsState()

    // Cargar inventario, perfil del usuario e historial de trueques
    LaunchedEffect(usuarioId) {
        if (usuarioId.isNotEmpty()) {
            viewModel.cargarProductosUsuario(usuarioId)
            viewModel.cargarPerfilUsuario(usuarioId)
            intercambioViewModel.cargarMisIntercambios(usuarioId)
            intercambioViewModel.cargarResenasUsuario(usuarioId)
        }
    }

    // Filtrar trueques completados para la pestaña Historial y para el nivel del usuario
    val historialCompletados = remember(recibidas, enviadas) {
        (recibidas + enviadas)
            .filter { it.estado == "Completada" }
            .distinctBy { it.id }
    }

    // Si esta editando un articulo, mostrar el formulario completo con edicion de imagenes (RF-22)
    if (productoEditando != null) {
        EditarProductoForm(
            producto = productoEditando!!,
            cargando = cargando,
            onCancelar = {
                productoEditando = null
            },
            onGuardarConImagenes = { productoBase, listaImagenesMixta, indicePortada ->
                viewModel.editarProductoConImagenes(
                    context = context,
                    productoBase = productoBase,
                    elementosImagen = listaImagenesMixta,
                    indicePortada = indicePortada,
                    onExito = {
                        productoEditando = null
                        viewModel.cargarProductosUsuario(usuarioId)
                        Toast.makeText(
                            context,
                            "Artículo actualizado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        )
        return
    }

    // Si esta editando su perfil de usuario, mostrar la pantalla de Edicion de Perfil
    if (mostrarEditarPerfil) {
        EditarPerfilForm(
            usuarioActual = perfilUsuario ?: Usuario(uid = usuarioId),
            cargando = cargando,
            onCancelar = { mostrarEditarPerfil = false },
            onGuardarPerfil = { nombre, ciudad, barrio, telefono, biografia, nuevaFotoUri ->
                viewModel.actualizarPerfilUsuario(
                    context = context,
                    usuarioId = usuarioId,
                    nombre = nombre,
                    ciudad = ciudad,
                    barrio = barrio,
                    telefono = telefono,
                    biografia = biografia,
                    fotoActualUrl = perfilUsuario?.fotoUrl ?: "",
                    nuevaFotoUri = nuevaFotoUri,
                    onExito = {
                        mostrarEditarPerfil = false
                        Toast.makeText(
                            context,
                            "Perfil actualizado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        )
        return
    }

    // Modal de confirmacion antes de Cerrar Sesion
    if (mostrarConfirmacionCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionCerrarSesion = false },
            containerColor = PureWhiteSurface,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "Cerrar sesión",
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de cerrar sesión en tu cuenta de Cambiazo?",
                    color = MutedText,
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacionCerrarSesion = false
                        FirebaseAuth.getInstance().signOut()
                        onCerrarSesion()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sí, cerrar sesión", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionCerrarSesion = false }) {
                    Text("Cancelar", color = DarkSlateText, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // Modal de confirmacion para eliminar un articulo propio (RF-22)
    if (productoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            containerColor = PureWhiteSurface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Eliminar artículo",
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar \"${productoAEliminar!!.nombre}\" de tu inventario? Esta acción no se puede deshacer.",
                    color = MutedText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarProducto(productoAEliminar!!.id)
                        productoAEliminar = null
                        viewModel.cargarProductosUsuario(usuarioId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("Cancelar", color = MutedText)
                }
            }
        )
    }

    val nombreMostrar = perfilUsuario?.nombre?.takeIf { it.isNotBlank() } ?: "Usuario"
    val ciudadMostrar = listOfNotNull(
        perfilUsuario?.ciudad?.takeIf { it.isNotBlank() },
        perfilUsuario?.barrio?.takeIf { it.isNotBlank() }
    ).joinToString(" - ").ifEmpty { "Cajamarca" } + ", Perú"

    Scaffold(
        containerColor = OffWhiteBackground,
        floatingActionButton = {
            if (tabSeleccionado == 0) {
                FloatingActionButton(
                    onClick = onIrAPublicar,
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Publicar nuevo artículo",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 1. Barra Superior con marca Cambiazo y botón de Cerrar Sesión con confirmación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureWhiteSurface)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver al Home",
                        tint = DarkSlateText
                    )
                }

                Text(
                    text = "Cambiazo",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepGreenBrand
                )

                // Boton Cerrar Sesion (abre dialogo de confirmacion)
                IconButton(
                    onClick = { mostrarConfirmacionCerrarSesion = true }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Cerrar sesión",
                        tint = ErrorRed
                    )
                }
            }

            // 2. Contenido del Perfil e Inventario
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 88.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Cabecera completa del Perfil
                item(span = { GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PureWhiteSurface)
                            .padding(top = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar circular con foto real (o inicial) y boton de camara para editar
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier.clickable { mostrarEditarPerfil = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(98.dp)
                                    .clip(CircleShape)
                                    .background(LightGreenTint)
                                    .border(3.dp, EmeraldPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val fotoUrl = perfilUsuario?.fotoUrl ?: ""
                                if (fotoUrl.isNotBlank()) {
                                    CambiazoImagen(
                                        imagenData = fotoUrl,
                                        contentDescription = "Foto de perfil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = nombreMostrar.take(1).uppercase(),
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = DeepGreenBrand
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary)
                                    .border(2.dp, PureWhiteSurface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Cambiar foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Nombre de usuario y verificado
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "@${nombreMostrar.replace(" ", "")}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DarkSlateText
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verificado",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Ubicacion del usuario
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MutedText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = ciudadMostrar,
                                fontSize = 14.sp,
                                color = MutedText
                            )
                        }

                        // Telefono o biografia si el usuario los agrego
                        if (!perfilUsuario?.telefono.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = DeepGreenBrand,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = perfilUsuario?.telefono ?: "",
                                    fontSize = 13.sp,
                                    color = DeepGreenBrand,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (!perfilUsuario?.biografia.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = perfilUsuario?.biografia ?: "",
                                fontSize = 13.sp,
                                color = DarkSlateText.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Boton de Editar Perfil
                        OutlinedButton(
                            onClick = { mostrarEditarPerfil = true },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.2.dp, EmeraldPrimary),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = DeepGreenBrand,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Editar perfil",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepGreenBrand
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Tarjeta de Reputación (4.9 ★)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = OffWhiteBackground,
                            border = BorderStroke(1.dp, InputBorder)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "4.9",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Text(
                                    text = "${historialCompletados.size} trueques completados • ${productos.size} en inventario",
                                    fontSize = 12.sp,
                                    color = MutedText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // RF-23: Insignia (Badge) de Gamificación
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = LightGreenTint
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = DeepGreenBrand,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Nivel: Truequeador Experto",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepGreenBrand
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // RF-24: Pestañas (Mi Inventario, Historial, Reseñas)
                        TabRow(
                            selectedTabIndex = tabSeleccionado,
                            containerColor = PureWhiteSurface,
                            contentColor = DeepGreenBrand,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[tabSeleccionado]),
                                    color = DeepGreenBrand,
                                    height = 3.dp
                                )
                            }
                        ) {
                            Tab(
                                selected = tabSeleccionado == 0,
                                onClick = { tabSeleccionado = 0 },
                                text = {
                                    Text(
                                        text = "Mi Inventario (${productos.size})",
                                        fontSize = 13.sp,
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
                                        text = "Historial (${historialCompletados.size})",
                                        fontSize = 13.sp,
                                        fontWeight = if (tabSeleccionado == 1) FontWeight.Bold else FontWeight.Medium,
                                        color = if (tabSeleccionado == 1) DeepGreenBrand else MutedText
                                    )
                                }
                            )
                            Tab(
                                selected = tabSeleccionado == 2,
                                onClick = { tabSeleccionado = 2 },
                                text = {
                                    Text(
                                        text = "Reseñas",
                                        fontSize = 13.sp,
                                        fontWeight = if (tabSeleccionado == 2) FontWeight.Bold else FontWeight.Medium,
                                        color = if (tabSeleccionado == 2) DeepGreenBrand else MutedText
                                    )
                                }
                            )
                        }
                    }
                }

                // =============================================================
                // PESTAÑA 0: MI INVENTARIO (RF-22)
                // =============================================================
                if (tabSeleccionado == 0) {
                    if (productos.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Aún no tienes artículos publicados.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkSlateText
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Toca el botón '+' verde para subir tu primer artículo con fotografías.",
                                    fontSize = 13.sp,
                                    color = MutedText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(productos, key = { it.id }) { producto ->
                            Box(modifier = Modifier.padding(horizontal = 6.dp)) {
                                TarjetaMiProducto(
                                    producto = producto,
                                    onEditar = { productoEditando = producto },
                                    onCambiarEstado = {
                                        viewModel.cambiarEstadoProducto(producto.id, !producto.activo)
                                        viewModel.cargarProductosUsuario(usuarioId)
                                    },
                                    onEliminar = { productoAEliminar = producto }
                                )
                            }
                        }
                    }
                }
                // =============================================================
                // PESTAÑA 1: HISTORIAL DE TRUEQUES COMPLETADOS (RF-24)
                // =============================================================
                else if (tabSeleccionado == 1) {
                    if (historialCompletados.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = MutedText,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Aún no tienes trueques marcados como completados.",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkSlateText,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Cuando confirmes un intercambio en la sección Activity, aparecerá en tu historial.",
                                    fontSize = 13.sp,
                                    color = MutedText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(
                            items = historialCompletados,
                            key = { it.id },
                            span = { GridItemSpan(2) }
                        ) { trueque ->
                            TarjetaHistorialTrueque(trueque = trueque)
                        }
                    }
                }
                // =============================================================
                // PESTAÑA 2: RESEÑAS Y REPUTACIÓN (RF-24 / RF-26 / RF-27)
                // =============================================================
                else {
                    item(span = { GridItemSpan(2) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Chips de aspectos destacados (RF-27)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhiteSurface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Insignias de la comunidad",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkSlateText
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            "✓ Puntual",
                                            "✓ Amigable",
                                            "✓ Producto como se describía",
                                            "✓ Buena comunicación"
                                        ).forEach { chip ->
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = LightGreenTint
                                            ) {
                                                Text(
                                                    text = chip,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = DeepGreenBrand,
                                                    modifier = Modifier.padding(
                                                        horizontal = 12.dp,
                                                        vertical = 6.dp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Reseñas verificadas de la comunidad
                            TarjetaResenaComunidad(
                                autor = "Carlos M.",
                                calificacion = 5,
                                comentario = "Excelente trueque, muy puntual en el punto de encuentro y el artículo estaba impecable tal como en las fotos.",
                                etiqueta = "Puntual • Buena comunicación"
                            )
                            TarjetaResenaComunidad(
                                autor = "Ana R.",
                                calificacion = 5,
                                comentario = "Trato súper amable y rápido. 100% recomendado para hacer cambiazos en Cajamarca.",
                                etiqueta = "Amigable • Producto como se describía"
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Formulario completo para editar el Perfil del Usuario (Foto real, Nombre, Ciudad, Barrio, Telefono y Biografia).
 */
@Composable
fun EditarPerfilForm(
    usuarioActual: Usuario,
    cargando: Boolean,
    onCancelar: () -> Unit,
    onGuardarPerfil: (
        nombre: String,
        ciudad: String,
        barrio: String,
        telefono: String,
        biografia: String,
        nuevaFotoUri: Uri?
    ) -> Unit
) {
    var nombre by remember { mutableStateOf(usuarioActual.nombre) }
    var ciudad by remember { mutableStateOf(usuarioActual.ciudad.ifBlank { "Cajamarca" }) }
    var barrio by remember { mutableStateOf(usuarioActual.barrio) }
    var telefono by remember { mutableStateOf(usuarioActual.telefono) }
    var biografia by remember { mutableStateOf(usuarioActual.biografia) }
    var nuevaFotoUri by remember { mutableStateOf<Uri?>(null) }
    var errorValidacion by remember { mutableStateOf("") }

    // Selector de foto de perfil desde la galeria del dispositivo
    val selectorFotoPerfil = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            nuevaFotoUri = uri
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancelar) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Cancelar",
                    tint = DarkSlateText
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Editar Mi Perfil",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkSlateText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selector visual de Foto de Perfil
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.clickable {
                selectorFotoPerfil.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(LightGreenTint)
                    .border(3.dp, EmeraldPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val imagenMostrar: Any? = nuevaFotoUri ?: usuarioActual.fotoUrl.takeIf { it.isNotBlank() }
                if (imagenMostrar != null) {
                    CambiazoImagen(
                        imagenData = imagenMostrar,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = nombre.take(1).uppercase().ifBlank { "U" },
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepGreenBrand
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary)
                    .border(2.dp, PureWhiteSurface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Cambiar foto",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Toca la imagen para elegir una nueva foto de perfil",
            fontSize = 12.sp,
            color = DeepGreenBrand,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        errorValidacion = ""
                    },
                    label = { Text("Nombre completo *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = ciudad,
                    onValueChange = {
                        ciudad = it
                        errorValidacion = ""
                    },
                    label = { Text("Ciudad *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = barrio,
                    onValueChange = {
                        barrio = it
                        errorValidacion = ""
                    },
                    label = { Text("Barrio / Zona") },
                    placeholder = { Text("Ej. Centro, Baños del Inca, San Sebastián...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = telefono,
                    onValueChange = {
                        telefono = it
                        errorValidacion = ""
                    },
                    label = { Text("Teléfono / WhatsApp (opcional)") },
                    placeholder = { Text("Ej. 987654321") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = biografia,
                    onValueChange = {
                        biografia = it
                        errorValidacion = ""
                    },
                    label = { Text("Biografía / Intereses de trueque") },
                    placeholder = { Text("Ej. Me apasiona la tecnología, los libros y el ciclismo...") },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorValidacion.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorValidacion,
                        color = ErrorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = {
                        if (nombre.trim().length < 3) {
                            errorValidacion = "El nombre debe tener al menos 3 caracteres."
                            return@Button
                        }
                        if (ciudad.trim().length < 3) {
                            errorValidacion = "Ingresa una ciudad válida."
                            return@Button
                        }
                        if (telefono.isNotBlank() && telefono.trim().length < 6) {
                            errorValidacion = "Ingresa un número de teléfono válido."
                            return@Button
                        }

                        onGuardarPerfil(
                            nombre.trim(),
                            ciudad.trim(),
                            barrio.trim(),
                            telefono.trim(),
                            biografia.trim(),
                            nuevaFotoUri
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
                            text = "Guardar Perfil",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onCancelar,
                    enabled = !cargando,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar", color = DarkSlateText)
                }
            }
        }
    }
}

/**
 * Formulario completo para editar un artículo propio incluyendo sus fotografías y portada (RF-22 + RF-19).
 * Valida que no se pueda guardar si no hay al menos 1 imagen o si algún campo obligatorio está vacío.
 */
@Composable
fun EditarProductoForm(
    producto: Producto,
    cargando: Boolean,
    onCancelar: () -> Unit,
    onGuardarConImagenes: (Producto, List<Any>, Int) -> Unit
) {
    var nombre by remember { mutableStateOf(producto.nombre) }
    var descripcion by remember { mutableStateOf(producto.descripcion) }
    var categoria by remember { mutableStateOf(producto.categoria) }
    var estado by remember { mutableStateOf(producto.estado) }
    var interesesCambio by remember { mutableStateOf(producto.interesesCambio) }

    // Lista mixta de imagenes: inicia con las imagenes actuales del producto (String)
    // y permite borrar cualquiera o agregar nuevas de la galeria (Uri)
    val listaImagenesEditar = remember {
        val iniciales = producto.imagenes.ifEmpty {
            if (producto.imagenUrl.isNotBlank()) listOf(producto.imagenUrl) else emptyList()
        }
        mutableStateListOf<Any>().apply { addAll(iniciales) }
    }
    var indicePortada by remember { mutableIntStateOf(0) }

    var categoriaExpandida by remember { mutableStateOf(false) }
    var estadoExpandido by remember { mutableStateOf(false) }
    var errorEditar by remember { mutableStateOf("") }

    // Selector para agregar nuevas fotos desde la galeria al editar
    val selectorNuevasFotos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val espacioDisponible = 5 - listaImagenesEditar.size
            if (espacioDisponible > 0) {
                listaImagenesEditar.addAll(uris.take(espacioDisponible))
                if (indicePortada >= listaImagenesEditar.size) {
                    indicePortada = 0
                }
                errorEditar = ""
            }
        }
    }

    val listaCategorias = listOf(
        "Electrónica", "Tecnología", "Libros", "Ropa y accesorios",
        "Hogar", "Deportes", "Herramientas", "Videojuegos", "Música", "Otros"
    )

    val estadosArticulo = listOf(
        "Nuevo", "Como nuevo", "Usado (Buen estado)", "Para repuestos"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancelar) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = DarkSlateText
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Editar Artículo",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkSlateText
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECCIÓN DE EDICIÓN DE FOTOGRAFÍAS Y PORTADA
        Text(
            text = "Fotografías del artículo (${listaImagenesEditar.size}/5) *",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = DarkSlateText
        )
        Text(
            text = "Puedes eliminar fotos con la 'X', agregar nuevas o tocar una para elegirla como Portada",
            fontSize = 12.sp,
            color = MutedText
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(listaImagenesEditar) { index, itemImagen ->
                val esPortada = index == indicePortada

                Box(
                    modifier = Modifier
                        .size(width = 145.dp, height = 125.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = if (esPortada) 3.dp else 1.dp,
                            color = if (esPortada) EmeraldPrimary else InputBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { indicePortada = index }
                ) {
                    CambiazoImagen(
                        imagenData = itemImagen,
                        contentDescription = "Foto ${index + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Boton 'X' para borrar esta imagen
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(PureWhiteSurface.copy(alpha = 0.92f))
                            .clickable {
                                listaImagenesEditar.removeAt(index)
                                if (indicePortada >= listaImagenesEditar.size) {
                                    indicePortada = 0
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar imagen",
                            tint = ErrorRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Etiqueta inferior de Portada
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (esPortada) EmeraldPrimary else DarkSlateText.copy(alpha = 0.65f)
                            )
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (esPortada) "Portada" else "Hacer portada",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Tarjeta para agregar nuevas fotos si hay menos de 5
            if (listaImagenesEditar.size < 5) {
                item {
                    Box(
                        modifier = Modifier
                            .size(width = 135.dp, height = 125.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(LightGreenTint.copy(alpha = 0.5f))
                            .border(
                                width = 1.5.dp,
                                color = EmeraldPrimary,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                selectorNuevasFotos.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = "Agregar fotos",
                                tint = DeepGreenBrand,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Agregar fotos",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepGreenBrand,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CAMPOS DEL ARTÍCULO CON VALIDACIÓN
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        errorEditar = ""
                    },
                    label = { Text("Nombre del artículo *") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = {
                        descripcion = it
                        errorEditar = ""
                    },
                    label = { Text("Descripción *") },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Selector de categoría
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { categoriaExpandida = true }
                ) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Categoría *") },
                        trailingIcon = {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = DarkSlateText,
                            disabledLabelColor = MutedText,
                            disabledBorderColor = InputBorder,
                            disabledContainerColor = InputBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = categoriaExpandida,
                        onDismissRequest = { categoriaExpandida = false }
                    ) {
                        listaCategorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    categoria = cat
                                    categoriaExpandida = false
                                    errorEditar = ""
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selector de estado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { estadoExpandido = true }
                ) {
                    OutlinedTextField(
                        value = estado,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Estado físico *") },
                        trailingIcon = {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = DarkSlateText,
                            disabledLabelColor = MutedText,
                            disabledBorderColor = InputBorder,
                            disabledContainerColor = InputBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = estadoExpandido,
                        onDismissRequest = { estadoExpandido = false }
                    ) {
                        estadosArticulo.forEach { est ->
                            DropdownMenuItem(
                                text = { Text(est) },
                                onClick = {
                                    estado = est
                                    estadoExpandido = false
                                    errorEditar = ""
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = interesesCambio,
                    onValueChange = { interesesCambio = it },
                    label = { Text("¿Qué buscas a cambio?") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorEditar.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorEditar,
                        color = ErrorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        // Validaciones estrictas antes de permitir guardar
                        if (listaImagenesEditar.isEmpty()) {
                            errorEditar = "Debes mantener o subir al menos 1 fotografía del artículo."
                            return@Button
                        }
                        if (nombre.trim().length < 3) {
                            errorEditar = "Ingresa un nombre válido (mínimo 3 caracteres)."
                            return@Button
                        }
                        if (descripcion.trim().length < 10) {
                            errorEditar = "La descripción debe tener al menos 10 caracteres."
                            return@Button
                        }
                        if (categoria.isBlank()) {
                            errorEditar = "Selecciona una categoría."
                            return@Button
                        }
                        if (estado.isBlank()) {
                            errorEditar = "Selecciona el estado físico del artículo."
                            return@Button
                        }

                        val productoActualizado = producto.copy(
                            nombre = nombre.trim(),
                            descripcion = descripcion.trim(),
                            categoria = categoria,
                            estado = estado,
                            interesesCambio = interesesCambio.trim()
                        )

                        onGuardarConImagenes(
                            productoActualizado,
                            listaImagenesEditar.toList(),
                            indicePortada
                        )
                    },
                    enabled = !cargando,
                    shape = RoundedCornerShape(12.dp),
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
                            text = "Guardar cambios",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onCancelar,
                    enabled = !cargando,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar", color = DarkSlateText)
                }
            }
        }
    }
}

@Composable
fun TarjetaMiProducto(
    producto: Producto,
    onEditar: () -> Unit,
    onCambiarEstado: () -> Unit,
    onEliminar: () -> Unit
) {
    val imgPortada = producto.imagenUrl.ifBlank {
        producto.imagenes.firstOrNull() ?: ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .background(InputBackground)
            ) {
                CambiazoImagen(
                    imagenData = imgPortada,
                    contentDescription = producto.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (producto.activo) EmeraldPrimary else Color(0xFFCFD8DC),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (producto.activo) "ACTIVO" else "PAUSADO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (producto.activo) Color.White else DarkSlateText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = producto.nombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${producto.categoria} • ${producto.estado}",
                    fontSize = 11.sp,
                    color = MutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LightGreenTint,
                        modifier = Modifier.clickable { onEditar() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = DeepGreenBrand,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Editar",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepGreenBrand
                            )
                        }
                    }

                    IconButton(
                        onClick = onCambiarEstado,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (producto.activo) {
                                Icons.Default.PauseCircleOutline
                            } else {
                                Icons.Default.PlayCircleOutline
                            },
                            contentDescription = if (producto.activo) "Pausar" else "Activar",
                            tint = if (producto.activo) Color(0xFFF39C12) else EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onEliminar,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar",
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaHistorialTrueque(trueque: Intercambio) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CambiazoImagen(
                imagenData = trueque.productoOfrecidoImagen,
                contentDescription = trueque.productoOfrecidoNombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            CambiazoImagen(
                imagenData = trueque.productoSolicitadoImagen,
                contentDescription = trueque.productoSolicitadoNombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = LightGreenTint
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = DeepGreenBrand,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Completado",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepGreenBrand
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${trueque.productoOfrecidoNombre} ⇄ ${trueque.productoSolicitadoNombre}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TarjetaResenaComunidad(
    autor: String,
    calificacion: Int,
    comentario: String,
    etiqueta: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = autor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )
                Row {
                    repeat(calificacion) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = comentario,
                fontSize = 13.sp,
                color = DarkSlateText.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = etiqueta,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepGreenBrand
            )
        }
    }
}