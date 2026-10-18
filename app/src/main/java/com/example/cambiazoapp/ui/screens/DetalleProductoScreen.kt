
package com.example.cambiazoapp.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
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
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
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
import androidx.compose.foundation.layout.navigationBarsPadding
import com.example.cambiazoapp.ui.components.CambiazoImagen

@Composable
fun DetalleProductoScreen(
    productoId: String,
    onVolver: () -> Unit,
    onProponerTrueque: (String) -> Unit,
    viewModel: ProductoViewModel = viewModel()
) {
    val usuarioActualId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val producto by viewModel.productoSeleccionado.collectAsState()
    val propietario by viewModel.propietarioProducto.collectAsState()
    val favoritosIds by viewModel.favoritosIds.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    LaunchedEffect(productoId) {
        viewModel.cargarProductoPorId(productoId)
    }

    if (cargando && producto == null) {
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

    // Lista de imágenes para el carrusel interactivo (RF-10)
    val listaFotos = prod.imagenes.ifEmpty {
        if (prod.imagenUrl.isNotBlank()) listOf(prod.imagenUrl) else emptyList()
    }
    val pagerState = rememberPagerState(pageCount = { maxOf(listaFotos.size, 1) })
    val esFavorito = favoritosIds.contains(prod.id)
    val esMiPropioProducto = prod.usuarioId == usuarioActualId

    Scaffold(
        containerColor = OffWhiteBackground,
        bottomBar = {
            // Barra fija inferior con botón "Proponer Trueque" (Mockup Pantalla 4)
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
                                onProponerTrueque(prod.id)
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
                        onClick = { onProponerTrueque(prod.id) },
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.ImageNotSupported,
                                contentDescription = "Sin fotos",
                                tint = MutedText,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Sin fotografías disponibles", color = MutedText, fontSize = 13.sp)
                        }
                    }
                }

                // Botón superior izquierdo: Volver
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

                // Contador inferior izquierdo: "1/4" (Mockup Pantalla 4 - RF-10)
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
                        val nombreDueno = propietario?.nombre ?: "Usuario Cambiazo"
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

                // 5. CAJA VERDE: "Lo que busco a cambio" (Mockup Pantalla 4)
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