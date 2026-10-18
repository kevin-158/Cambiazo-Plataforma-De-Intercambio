package com.example.cambiazoapp.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.cambiazoapp.model.Producto
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
import com.example.cambiazoapp.viewmodel.ProductoViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PublicarProductoScreen(
    onProductoPublicado: () -> Unit,
    viewModel: ProductoViewModel = viewModel()
) {
    val context = LocalContext.current
    val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var categoriaExpandida by remember { mutableStateOf(false) }
    var estado by remember { mutableStateOf("Usado (Buen estado)") }

    // RF-19: Lista de fotos seleccionadas y cuál es el índice de la Portada
    val imagenesSeleccionadas = remember { mutableStateListOf<Uri>() }
    var indicePortada by remember { mutableIntStateOf(0) }

    // RF-21: Etiquetas de "¿Qué buscas a cambio?"
    val etiquetasInteres = remember {
        mutableStateListOf("Tecnología", "Abierto a ofertas")
    }
    var mostrarDialogoNuevaEtiqueta by remember { mutableStateOf(false) }
    var nuevaEtiquetaTexto by remember { mutableStateOf("") }

    var errorValidacion by remember { mutableStateOf("") }
    val cargando by viewModel.cargando.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    // Selector nativo de múltiples fotos reales (RF-19)
    val selectorMultipleFotos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val espacioRestante = 5 - imagenesSeleccionadas.size
            imagenesSeleccionadas.addAll(uris.take(espacioRestante))
            if (indicePortada >= imagenesSeleccionadas.size) {
                indicePortada = 0
            }
            errorValidacion = ""
        }
    }

    val listaCategorias = listOf(
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

    val estadosArticulo = listOf(
        "Nuevo",
        "Como nuevo",
        "Usado (Buen estado)",
        "Para repuestos"
    )

    if (mostrarDialogoNuevaEtiqueta) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoNuevaEtiqueta = false },
            containerColor = PureWhiteSurface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Agregar interés de cambio",
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText,
                    fontSize = 18.sp
                )
            },
            text = {
                OutlinedTextField(
                    value = nuevaEtiquetaTexto,
                    onValueChange = { nuevaEtiquetaTexto = it },
                    placeholder = { Text("Ej. Bicicleta, Audífonos, Monitor...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevaEtiquetaTexto.isNotBlank()) {
                            etiquetasInteres.add(nuevaEtiquetaTexto.trim())
                            nuevaEtiquetaTexto = ""
                            mostrarDialogoNuevaEtiqueta = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Agregar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoNuevaEtiqueta = false }) {
                    Text("Cancelar", color = MutedText)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBackground)
            .imePadding()
    ) {
        // Barra Superior (Mockup Pantalla 7)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureWhiteSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onProductoPublicado) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = DarkSlateText
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Publicar para Trueque",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Paso 1 de 2",
                    fontSize = 12.sp,
                    color = MutedText,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Detalles",
                    fontSize = 12.sp,
                    color = MutedText,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Barra de progreso verde
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(EmeraldPrimary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFD9EAFF))
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // SECCIÓN RF-19: Carga de múltiples fotos reales y selección de Portada
            Text(
                text = "Fotografías del artículo (${imagenesSeleccionadas.size}/5)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkSlateText
            )
            Text(
                text = "Toca una foto para elegirla como Portada principal",
                fontSize = 12.sp,
                color = MutedText
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(imagenesSeleccionadas) { index, uri ->
                    val esPortada = index == indicePortada

                    Box(
                        modifier = Modifier
                            .size(width = 150.dp, height = 125.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = if (esPortada) 3.dp else 1.dp,
                                color = if (esPortada) EmeraldPrimary else InputBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { indicePortada = index }
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Foto ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Botón para quitar foto
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(PureWhiteSurface.copy(alpha = 0.9f))
                                .clickable {
                                    imagenesSeleccionadas.removeAt(index)
                                    if (indicePortada >= imagenesSeleccionadas.size) {
                                        indicePortada = 0
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar foto",
                                tint = DarkSlateText,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Etiqueta de Portada (RF-19)
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
                                text = if (esPortada) "Portada" else "Elegir portada",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Botón para agregar hasta 5 fotos reales
                if (imagenesSeleccionadas.size < 5) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(width = 140.dp, height = 125.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(LightGreenTint.copy(alpha = 0.5f))
                                .border(
                                    width = 1.5.dp,
                                    color = EmeraldPrimary,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    selectorMultipleFotos.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoCamera,
                                    contentDescription = "Agregar fotos",
                                    tint = DeepGreenBrand,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Agrega hasta 5 fotos",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepGreenBrand,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CARD PRINCIPAL DE DETALLES DEL ARTÍCULO (RF-20)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // 1. Título del artículo
                    Text(
                        text = "Título del artículo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkSlateText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            errorValidacion = ""
                        },
                        placeholder = {
                            Text(
                                "Ej. Bicicleta de Montaña Trek 29''",
                                color = MutedText,
                                fontSize = 14.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputBackground,
                            unfocusedContainerColor = InputBackground,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = InputBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Categoría (RF-20)
                    Text(
                        text = "Categoría",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkSlateText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                            placeholder = {
                                Text("Selecciona categoría", color = MutedText, fontSize = 14.sp)
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = DarkSlateText
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = DarkSlateText,
                                disabledPlaceholderColor = MutedText,
                                disabledBorderColor = InputBorder,
                                disabledContainerColor = InputBackground
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = categoriaExpandida,
                            onDismissRequest = { categoriaExpandida = false },
                            modifier = Modifier.background(PureWhiteSurface)
                        ) {
                            listaCategorias.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = DarkSlateText) },
                                    onClick = {
                                        categoria = cat
                                        categoriaExpandida = false
                                        errorValidacion = ""
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Estado del artículo (RF-20: Botones en grilla 2x2 como en el Mockup Pantalla 7)
                    Text(
                        text = "Estado del artículo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkSlateText
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        estadosArticulo.chunked(2).forEach { filaEstados ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                filaEstados.forEach { opcionEstado ->
                                    val seleccionado = estado == opcionEstado
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { estado = opcionEstado },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (seleccionado) LightGreenTint else PureWhiteSurface,
                                        border = BorderStroke(
                                            width = if (seleccionado) 1.5.dp else 1.dp,
                                            color = if (seleccionado) EmeraldPrimary else InputBorder
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 12.dp
                                            ),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(
                                                text = opcionEstado,
                                                fontSize = 13.sp,
                                                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                                                color = if (seleccionado) DeepGreenBrand else DarkSlateText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Descripción
                    Text(
                        text = "Descripción",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkSlateText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = {
                            descripcion = it
                            errorValidacion = ""
                        },
                        placeholder = {
                            Text(
                                "Describe los detalles de tu artículo, tiempo de uso, si tiene algún detalle...",
                                color = MutedText,
                                fontSize = 14.sp
                            )
                        },
                        minLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputBackground,
                            unfocusedContainerColor = InputBackground,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = InputBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECCIÓN RF-21: ¿Qué buscas a cambio?
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "¿Qué buscas a cambio?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlateText
                    )
                    Text(
                        text = "(Opcional)",
                        fontSize = 13.sp,
                        color = MutedText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Agrega etiquetas para ayudar a otros usuarios a proponerte trueques.",
                        fontSize = 13.sp,
                        color = DarkSlateText.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        etiquetasInteres.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = LightGreenTint
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 13.sp,
                                        color = DeepGreenBrand,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Quitar etiqueta",
                                        tint = DeepGreenBrand,
                                        modifier = Modifier
                                            .size(15.dp)
                                            .clickable { etiquetasInteres.remove(tag) }
                                    )
                                }
                            }
                        }

                        // Botón "+ Agregar interés"
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = PureWhiteSurface,
                            border = BorderStroke(1.dp, InputBorder),
                            modifier = Modifier.clickable { mostrarDialogoNuevaEtiqueta = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Agregar interés",
                                    tint = DarkSlateText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Agregar interés",
                                    fontSize = 13.sp,
                                    color = DarkSlateText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            if (errorValidacion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorValidacion,
                    color = ErrorRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (mensaje.isNotEmpty() && !mensaje.contains("correctamente")) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mensaje,
                    color = ErrorRed,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Botón Principal: Publicar / Siguiente
            Button(
                onClick = {
                    if (imagenesSeleccionadas.isEmpty()) {
                        errorValidacion = "Debes seleccionar al menos 1 fotografía real del artículo."
                        return@Button
                    }
                    if (nombre.isBlank()) {
                        errorValidacion = "Ingresa el título del artículo."
                        return@Button
                    }
                    if (categoria.isBlank()) {
                        errorValidacion = "Selecciona una categoría."
                        return@Button
                    }
                    if (estado.isBlank()) {
                        errorValidacion = "Selecciona el estado físico del artículo."
                        return@Button
                    }
                    if (descripcion.isBlank()) {
                        errorValidacion = "Ingresa una descripción del artículo."
                        return@Button
                    }

                    val productoBase = Producto(
                        usuarioId = usuarioId,
                        nombre = nombre.trim(),
                        descripcion = descripcion.trim(),
                        categoria = categoria,
                        estado = estado,
                        interesesCambio = etiquetasInteres.joinToString(", "),
                        activo = true
                    )

                    viewModel.crearProductoConImagenes(
                        context = context,
                        productoBase = productoBase,
                        urislocales = imagenesSeleccionadas.toList(),
                        indicePortada = indicePortada,
                        onExito = onProductoPublicado
                    )
                },
                enabled = !cargando,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = EmeraldPrimary.copy(alpha = 0.5f)
                ),
                contentPadding = PaddingValues(vertical = 16.dp),
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
                        text = "Publicar Artículo",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}