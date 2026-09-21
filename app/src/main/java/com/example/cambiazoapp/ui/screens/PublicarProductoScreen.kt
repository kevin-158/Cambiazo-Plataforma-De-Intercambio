package com.example.cambiazoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cambiazoapp.model.Producto
import com.example.cambiazoapp.viewmodel.ProductoViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.imePadding





@Composable
fun PublicarProductoScreen(
    onProductoPublicado: () -> Unit,
    viewModel: ProductoViewModel = viewModel()
) {

    var nombre by remember {
        mutableStateOf("")
    }

    var descripcion by remember {
        mutableStateOf("")
    }

    var categoria by remember {
        mutableStateOf("")
    }

    var categoriaExpandida by remember {
        mutableStateOf(false)
    }

    var estado by remember {
        mutableStateOf("")
    }

    var estadoExpandido by remember {
        mutableStateOf(false)
    }

    var imagenUrl by remember {
        mutableStateOf("")
    }

    val usuarioId =
        FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Publicar artículo"
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
            },
            label = {
                Text("Nombre del artículo")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = {
                descripcion = it
            },
            label = {
                Text("Descripción")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

//==============================CATEGORIA==============================

        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    categoriaExpandida = true
                }
        ) {

            OutlinedTextField(
                value = categoria,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = {
                    Text("Categoría")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    disabledTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.outline,
                    disabledContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                )
            )
            androidx.compose.material3.DropdownMenu(
                expanded = categoriaExpandida,
                onDismissRequest = {
                    categoriaExpandida = false
                }
            ) {

                DropdownMenuItem(
                    text = { Text("Tecnología") },
                    onClick = {
                        categoria = "Tecnología"
                        categoriaExpandida = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Electrónica") },
                    onClick = {
                        categoria = "Electrónica"
                        categoriaExpandida = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Ropa y accesorios") },
                    onClick = {
                        categoria = "Ropa y accesorios"
                        categoriaExpandida = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Hogar") },
                    onClick = {
                        categoria = "Hogar"
                        categoriaExpandida = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Deportes") },
                    onClick = {
                        categoria = "Deportes"
                        categoriaExpandida = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Libros") },
                    onClick = {
                        categoria = "Libros"
                        categoriaExpandida = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Videojuegos") },
                    onClick = {
                        categoria = "Videojuegos"
                        categoriaExpandida = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Música") },
                    onClick = {
                        categoria = "Música"
                        categoriaExpandida = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Coleccionables") },
                    onClick = {
                        categoria = "Coleccionables"
                        categoriaExpandida = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Herramientas") },
                    onClick = {
                        categoria = "Herramientas"
                        categoriaExpandida = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Otros") },
                    onClick = {
                        categoria = "Otros"
                        categoriaExpandida = false
                    }
                )
            }
        }
//=========================ESTADO====================0
        Spacer(
            modifier = Modifier.height(12.dp)
        )

        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    estadoExpandido = true
                }
        ) {

            OutlinedTextField(
                value = estado,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = {
                    Text("Estado del artículo")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    disabledTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.outline,
                    disabledContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                )
            )

            androidx.compose.material3.DropdownMenu(
                expanded = estadoExpandido,
                onDismissRequest = {
                    estadoExpandido = false
                }
            ) {

                DropdownMenuItem(
                    text = { Text("Nuevo") },
                    onClick = {
                        estado = "Nuevo"
                        estadoExpandido = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Como nuevo") },
                    onClick = {
                        estado = "Como nuevo"
                        estadoExpandido = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Excelente estado") },
                    onClick = {
                        estado = "Excelente estado"
                        estadoExpandido = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Buen estado") },
                    onClick = {
                        estado = "Buen estado"
                        estadoExpandido = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Usado") },
                    onClick = {
                        estado = "Usado"
                        estadoExpandido = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Con signos de uso") },
                    onClick = {
                        estado = "Con signos de uso"
                        estadoExpandido = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Para reparar") },
                    onClick = {
                        estado = "Para reparar"
                        estadoExpandido = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Para repuestos") },
                    onClick = {
                        estado = "Para repuestos"
                        estadoExpandido = false
                    }
                )
            }
        }
//==================IMAGEN=================000
        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = imagenUrl,
            onValueChange = {
                imagenUrl = it
            },
            label = {
                Text("URL de imagen")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = {

                if (
                    nombre.isBlank() ||
                    descripcion.isBlank() ||
                    categoria.isBlank() ||
                    estado.isBlank()
                ) {
                    return@Button
                }

                val producto = Producto(
                    usuarioId = usuarioId,
                    nombre = nombre,
                    descripcion = descripcion,
                    categoria = categoria,
                    estado = estado,
                    imagenUrl = imagenUrl,
                    activo = true
                )

                viewModel.crearProducto(producto)

                onProductoPublicado()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Publicar artículo")
        }
    }
}

