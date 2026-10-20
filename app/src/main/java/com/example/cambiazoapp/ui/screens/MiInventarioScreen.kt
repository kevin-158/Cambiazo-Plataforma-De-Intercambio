package com.example.cambiazoapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cambiazoapp.model.Producto
import com.example.cambiazoapp.ui.components.CambiazoImagen
import com.example.cambiazoapp.ui.theme.DarkSlateText
import com.example.cambiazoapp.ui.theme.EmeraldPrimary
import com.example.cambiazoapp.ui.theme.ErrorRed
import com.example.cambiazoapp.ui.theme.InputBackground
import com.example.cambiazoapp.ui.theme.InputBorder
import com.example.cambiazoapp.ui.theme.MutedText
import com.example.cambiazoapp.ui.theme.OffWhiteBackground
import com.example.cambiazoapp.ui.theme.PureWhiteSurface
import com.example.cambiazoapp.viewmodel.ProductoViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * PANTALLA DE MI INVENTARIO (RF-22)
 * -------------------------------------------------------------
 * ÍNDICE DE SECCIONES EN ESTA PANTALLA:
 * 1. ENCABEZADO SUPERIOR: Título de la pantalla.
 * 2. LISTA DE ARTÍCULOS PROPIOS: 'LazyColumn' mostrando las tarjetas del usuario.
 * 3. TARJETA DE MI ARTÍCULO: Muestra foto (CambiazoImagen), estado, título y botones de acción (Editar, Pausar, Eliminar).
 * 4. MODAL DE EDICIÓN: 'ModalBottomSheet' que se abre al tocar "Editar", reutilizando la estética de la app.
 * 5. NAVEGACIÓN INFERIOR: La misma barra (CambiazoBottomNavigationBar) usada en HomeScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiInventarioScreen(
    onIrAHome: () -> Unit,
    onIrAPublicar: () -> Unit,
    onIrAActividad: () -> Unit,
    viewModel: ProductoViewModel = viewModel()
) {
    val productos by viewModel.productos.collectAsState()
    val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var productoEditando by remember { mutableStateOf<Producto?>(null) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(usuarioId) {
        if (usuarioId.isNotEmpty()) {
            viewModel.cargarProductosUsuario(usuarioId)
        }
    }

    Scaffold(
        containerColor = OffWhiteBackground,
        bottomBar = {
            CambiazoBottomNavigationBar(
                onHomeClick = onIrAHome,
                onSearchClick = { /* No-op aquí */ },
                onPostClick = onIrAPublicar,
                onActivityClick = onIrAActividad,
                onProfileClick = { /* Ya estamos aquí */ }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // 1. Título
            Text(
                text = "Mi Inventario",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkSlateText,
                modifier = Modifier.statusBarsPadding()
            )
            Text(
                text = "Gestiona los artículos que has publicado",
                fontSize = 14.sp,
                color = MutedText
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            if (productos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No tienes artículos en tu inventario.", color = MutedText)
                }
            } else {
                // 2. Lista de mis artículos
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(productos) { producto ->
                        // 3. Tarjeta de mi artículo
                        TarjetaMiArticulo(
                            producto = producto,
                            onEditar = { productoEditando = producto },
                            onPausarActivar = {
                                viewModel.cambiarEstadoProducto(producto.id, !producto.activo)
                                viewModel.cargarProductosUsuario(usuarioId)
                            },
                            onEliminar = { productoAEliminar = producto }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }

        // 4. Modal de Edición (Bottom Sheet)
        if (productoEditando != null) {
            ModalBottomSheet(
                onDismissRequest = { productoEditando = null },
                sheetState = sheetState,
                containerColor = PureWhiteSurface
            ) {
                EditarProductoForm(
                    producto = productoEditando!!,
                    onCancelar = { productoEditando = null },
                    onGuardar = { productoActualizado ->
                        viewModel.editarProducto(productoActualizado)
                        productoEditando = null
                        viewModel.cargarProductosUsuario(usuarioId)
                    }
                )
            }
        }

        // Modal de Confirmación de Eliminación
        if (productoAEliminar != null) {
            AlertDialog(
                containerColor = PureWhiteSurface,
                titleContentColor = DarkSlateText,
                textContentColor = MutedText,
                onDismissRequest = { productoAEliminar = null },
                title = { Text(text = "Eliminar artículo", fontWeight = FontWeight.Bold) },
                text = { Text(text = "¿Estás seguro de que deseas eliminar permanentemente '${productoAEliminar!!.nombre}'? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.eliminarProducto(productoAEliminar!!.id)
                            productoAEliminar = null
                            viewModel.cargarProductosUsuario(usuarioId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("Eliminar", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productoAEliminar = null }) {
                        Text("Cancelar", color = DarkSlateText)
                    }
                }
            )
        }
    }
}

@Composable
fun TarjetaMiArticulo(
    producto: Producto,
    onEditar: () -> Unit,
    onPausarActivar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhiteSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto del artículo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OffWhiteBackground)
            ) {
                if (producto.imagenUrl.isNotEmpty()) {
                    CambiazoImagen(
                        imagenData = producto.imagenUrl,
                        contentDescription = "Foto de ${producto.nombre}",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Badge de Estado (Publicado/Pausado) sobre la imagen
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (producto.activo) EmeraldPrimary else Color.Gray)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (producto.activo) "Activo" else "Pausado",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = producto.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = producto.categoria,
                    fontSize = 13.sp,
                    color = MutedText
                )
                Text(
                    text = producto.estado,
                    fontSize = 12.sp,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Botones de Acción (Iconos)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(80.dp)
            ) {
                Row {
                    // Botón Editar
                    IconButton(
                        onClick = onEditar,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(InputBackground)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = DarkSlateText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Botón Pausar/Activar
                    IconButton(
                        onClick = onPausarActivar,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(InputBackground)
                    ) {
                        Icon(
                            imageVector = if (producto.activo) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Pausar",
                            tint = if (producto.activo) DarkSlateText else MutedText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Botón Eliminar
                IconButton(
                    onClick = onEliminar,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, ErrorRed.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar",
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditarProductoForm(
    producto: Producto,
    onCancelar: () -> Unit,
    onGuardar: (Producto) -> Unit
) {
    var nombre by remember { mutableStateOf(producto.nombre) }
    var descripcion by remember { mutableStateOf(producto.descripcion) }
    var categoria by remember { mutableStateOf(producto.categoria) }
    var estado by remember { mutableStateOf(producto.estado) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Editar artículo",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DarkSlateText
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del artículo") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = InputBackground,
                unfocusedContainerColor = InputBackground,
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = InputBorder
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = InputBackground,
                unfocusedContainerColor = InputBackground,
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = InputBorder
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = categoria,
            onValueChange = { categoria = it },
            label = { Text("Categoría") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = InputBackground,
                unfocusedContainerColor = InputBackground,
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = InputBorder
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = estado,
            onValueChange = { estado = it },
            label = { Text("Estado Físico") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = InputBackground,
                unfocusedContainerColor = InputBackground,
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = InputBorder
            )
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onGuardar(
                    producto.copy(
                        nombre = nombre,
                        descripcion = descripcion,
                        categoria = categoria,
                        estado = estado
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
            Text(text = "Guardar cambios", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onCancelar,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, InputBorder)
        ) {
            Text(text = "Cancelar", color = DarkSlateText)
        }
        
        Spacer(modifier = Modifier.height(24.dp)) // Espacio extra para el teclado/navegación
    }
}