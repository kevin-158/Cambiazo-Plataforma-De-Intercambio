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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.cambiazoapp.model.Resena
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

/**
 * Pantalla de Perfil Publico del Dueño (Cumple con RF-11 y RF-24).
 * Muestra la reputacion real (promedio de estrellas y reseñas recibidas), nivel en la plataforma
 * y organiza la actividad del dueño en 3 pestañas: Inventario, Historial y Reseñas.
 */

//=======Perfil del Truequeador (Al tocar al dueño de un producto o desde el chat)======

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PerfilPublicoScreen(
    usuarioId: String,
    onVolver: () -> Unit,
    onIrADetalleProducto: (String) -> Unit,
    productoViewModel: ProductoViewModel = viewModel(),
    intercambioViewModel: IntercambioViewModel = viewModel()
) {
    val usuario by productoViewModel.perfilUsuarioActual.collectAsState()
    val productosUsuario by productoViewModel.productos.collectAsState()
    val recibidas by intercambioViewModel.propuestasRecibidas.collectAsState()
    val enviadas by intercambioViewModel.propuestasEnviadas.collectAsState()
    val resenasRecibidas by intercambioViewModel.resenasUsuario.collectAsState()

    var tabSeleccionado by remember { mutableIntStateOf(0) }

    LaunchedEffect(usuarioId) {
        if (usuarioId.isNotEmpty()) {
            productoViewModel.cargarPerfilUsuario(usuarioId)
            productoViewModel.cargarProductosUsuario(usuarioId)
            intercambioViewModel.cargarMisIntercambios(usuarioId)
            intercambioViewModel.cargarResenasUsuario(usuarioId)
        }
    }

    val inventarioActivo = remember(productosUsuario) {
        productosUsuario.filter { it.activo }
    }

    val historialCompletados = remember(recibidas, enviadas) {
        (recibidas + enviadas)
            .filter { it.estado == "Completada" }
            .distinctBy { it.id }
    }

    val nombreMostrar = usuario?.nombre?.takeIf { it.isNotBlank() } ?: "Usuario Cambiazo"
    val ubicacionMostrar = listOfNotNull(
        usuario?.ciudad?.takeIf { it.isNotBlank() },
        usuario?.barrio?.takeIf { it.isNotBlank() }
    ).joinToString(" - ").ifEmpty { "Cajamarca" } + ", Perú"

    // Calcular promedio real de estrellas a partir de sus reseñas recibidas (RF-11)
    val promedioReal = remember(resenasRecibidas, usuario) {
        if (resenasRecibidas.isNotEmpty()) {
            resenasRecibidas.map { it.estrellas }.average()
        } else {
            usuario?.promedioEstrellas ?: 5.0
        }
    }

    val nivelGamificacion = UbicacionUtils.obtenerNivelUsuario(
        truequesCompletados = historialCompletados.size,
        totalArticulos = inventarioActivo.size
    )

    // Agrupar etiquetas destacadas recibidas en sus reseñas (RF-27)
    val etiquetasDestacadas = remember(resenasRecibidas) {
        val todas = resenasRecibidas.flatMap { it.etiquetas }
        if (todas.isNotEmpty()) {
            todas.groupingBy { it }.eachCount().entries
                .sortedByDescending { it.value }
                .map { "✓ ${it.key} (${it.value})" }
        } else {
            listOf("✓ Puntual", "✓ Producto como se describía", "✓ Amigable")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Barra superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureWhiteSurface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = DarkSlateText
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Perfil del Truequeador",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkSlateText
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Cabecera de Reputacion y Nivel del Dueño (RF-11)
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PureWhiteSurface)
                        .padding(top = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(94.dp)
                                .clip(CircleShape)
                                .background(LightGreenTint)
                                .border(3.dp, EmeraldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val foto = usuario?.fotoUrl ?: ""
                            if (foto.isNotBlank()) {
                                CambiazoImagen(
                                    imagenData = foto,
                                    contentDescription = nombreMostrar,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = nombreMostrar.take(1).uppercase(),
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DeepGreenBrand
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary)
                                .border(2.dp, PureWhiteSurface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verificado",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = nombreMostrar,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkSlateText
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = ubicacionMostrar,
                            fontSize = 13.sp,
                            color = MutedText
                        )
                    }

                    if (!usuario?.biografia.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = usuario?.biografia ?: "",
                            fontSize = 13.sp,
                            color = DarkSlateText.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tarjeta de Promedio de Estrellas y Reseñas (RF-11)
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
                                    text = "%.1f".format(promedioReal),
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
                                text = "${resenasRecibidas.size} reseña(s) • ${historialCompletados.size} trueques completados",
                                fontSize = 12.sp,
                                color = MutedText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Insignia de Nivel (RF-11 / RF-23)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = LightGreenTint
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = DeepGreenBrand,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Nivel: $nivelGamificacion",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepGreenBrand
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // RF-24: Pestañas (Inventario, Historial, Reseñas)
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
                                    text = "Inventario (${inventarioActivo.size})",
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
                                    text = "Reseñas (${resenasRecibidas.size})",
                                    fontSize = 13.sp,
                                    fontWeight = if (tabSeleccionado == 2) FontWeight.Bold else FontWeight.Medium,
                                    color = if (tabSeleccionado == 2) DeepGreenBrand else MutedText
                                )
                            }
                        )
                    }
                }
            }

            // PESTAÑA 0: INVENTARIO ACTIVO DEL DUEÑO
            if (tabSeleccionado == 0) {
                if (inventarioActivo.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = "Este usuario no tiene artículos activos en este momento.",
                            fontSize = 14.sp,
                            color = MutedText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        )
                    }
                } else {
                    items(inventarioActivo, key = { it.id }) { prod ->
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .fillMaxWidth()
                                .clickable { onIrADetalleProducto(prod.id) },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .background(InputBackground)
                                ) {
                                    CambiazoImagen(
                                        imagenData = prod.imagenUrl.ifBlank { prod.imagenes.firstOrNull() ?: "" },
                                        contentDescription = prod.nombre,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = prod.nombre,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkSlateText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${prod.categoria} • ${prod.estado}",
                                        fontSize = 11.sp,
                                        color = MutedText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // PESTAÑA 1: HISTORIAL DE TRUEQUES COMPLETADOS
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
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aún no registra intercambios completados.",
                                fontSize = 14.sp,
                                color = MutedText
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
            // PESTAÑA 2: RESEÑAS REALES RECIBIDAS (RF-25 / RF-26 / RF-27)
            else {
                item(span = { GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Resumen de etiquetas destacadas (RF-27)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhiteSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Aspectos destacados por otros usuarios",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkSlateText
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    etiquetasDestacadas.forEach { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = LightGreenTint
                                        ) {
                                            Text(
                                                text = tag,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = DeepGreenBrand,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (resenasRecibidas.isEmpty()) {
                            Text(
                                text = "Aún no tiene reseñas escritas. ¡Sé el primero en completar un trueque y calificarlo!",
                                fontSize = 13.sp,
                                color = MutedText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            )
                        } else {
                            resenasRecibidas.forEach { resena ->
                                TarjetaResenaReal(resena = resena)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaResenaReal(resena: Resena) {
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
                    text = resena.autorNombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )
                Row {
                    repeat(resena.estrellas.coerceIn(1, 5)) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (resena.articuloIntercambiado.isNotBlank()) {
                Text(
                    text = "Trueque: ${resena.articuloIntercambiado}",
                    fontSize = 11.sp,
                    color = MutedText
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = resena.comentario,
                fontSize = 13.sp,
                color = DarkSlateText.copy(alpha = 0.9f)
            )

            if (resena.etiquetas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = resena.etiquetas.joinToString(" • "),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepGreenBrand
                )
            }
        }
    }
}