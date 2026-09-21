package com.example.cambiazoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cambiazoapp.viewmodel.ProductoViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.height

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

@Composable
fun MiInventarioScreen(
    viewModel: ProductoViewModel = viewModel()
) {

    val productos by viewModel.productos.collectAsState()

    val usuarioId =
        FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var productoEditando by remember {
        mutableStateOf<com.example.cambiazoapp.model.Producto?>(null)
    }
    var productoAEliminar by remember {
        mutableStateOf<com.example.cambiazoapp.model.Producto?>(null)
    }

    LaunchedEffect(usuarioId) {

        if (usuarioId.isNotEmpty()) {
            viewModel.cargarProductosUsuario(usuarioId)
        }
    }

    if (productoEditando != null) {

        EditarProductoForm(
            producto = productoEditando!!,
            onCancelar = {
                productoEditando = null
            },
            onGuardar = { productoActualizado ->

                viewModel.editarProducto(productoActualizado)

                productoEditando = null

                viewModel.cargarProductosUsuario(usuarioId)
            }
        )

        return
    }

    if (productoAEliminar != null) {

        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                productoAEliminar = null
            },
            title = {
                androidx.compose.material3.Text(
                    text = "Eliminar producto"
                )
            },
            text = {
                androidx.compose.material3.Text(
                    text = "¿Estás seguro de que deseas eliminar este producto?"
                )
            },
            confirmButton = {

                androidx.compose.material3.TextButton(
                    onClick = {

                        viewModel.eliminarProducto(
                            productoAEliminar!!.id
                        )

                        productoAEliminar = null

                        viewModel.cargarProductosUsuario(usuarioId)
                    }
                ) {
                    androidx.compose.material3.Text(
                        text = "Eliminar"
                    )
                }
            },
            dismissButton = {

                androidx.compose.material3.TextButton(
                    onClick = {
                        productoAEliminar = null
                    }
                ) {
                    androidx.compose.material3.Text(
                        text = "Cancelar"
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Mi inventario"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(productos) { producto ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = producto.nombre
                        )

                        Text(
                            text = "Categoría: ${producto.categoria}"
                        )

                        Text(
                            text = "Estado: ${producto.estado}"
                        )

                        Text(
                            text =
                                if (producto.activo) {
                                    "Publicado"
                                } else {
                                    "Pausado"
                                }
                        )
                        androidx.compose.foundation.layout.Spacer(
                            modifier = androidx.compose.ui.Modifier.height(12.dp)
                        )

                        androidx.compose.material3.Button(
                            onClick = {
                                productoEditando = producto
                            }
                        ) {
                            androidx.compose.material3.Text(
                                text = "Editar"
                            )
                        }
                        androidx.compose.foundation.layout.Spacer(
                            modifier = androidx.compose.ui.Modifier.height(8.dp)
                        )

                        androidx.compose.material3.Button(
                            onClick = {

                                viewModel.cambiarEstadoProducto(
                                    producto.id,
                                    !producto.activo
                                )

                                viewModel.cargarProductosUsuario(usuarioId)
                            }
                        ) {
                            androidx.compose.material3.Text(
                                text = if (producto.activo) {
                                    "Pausar"
                                } else {
                                    "Activar"
                                }
                            )
                        }
                        androidx.compose.foundation.layout.Spacer(
                            modifier = androidx.compose.ui.Modifier.height(8.dp)
                        )

                        androidx.compose.material3.Button(
                            onClick = {
                                productoAEliminar = producto
                            }
                        ) {
                            androidx.compose.material3.Text(
                                text = "Eliminar"
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun EditarProductoForm(
    producto: com.example.cambiazoapp.model.Producto,
    onCancelar: () -> Unit,
    onGuardar: (com.example.cambiazoapp.model.Producto) -> Unit
) {

    var nombre by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(producto.nombre)
    }

    var descripcion by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(producto.descripcion)
    }

    var categoria by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(producto.categoria)
    }

    var estado by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(producto.estado)
    }

    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        androidx.compose.material3.Text(
            text = "Editar producto"
        )

        androidx.compose.foundation.layout.Spacer(
            modifier = androidx.compose.ui.Modifier.height(16.dp)
        )

        androidx.compose.material3.OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
            },
            label = {
                androidx.compose.material3.Text("Nombre")
            },
            modifier = androidx.compose.ui.Modifier.fillMaxWidth()
        )

        androidx.compose.foundation.layout.Spacer(
            modifier = androidx.compose.ui.Modifier.height(12.dp)
        )

        androidx.compose.material3.OutlinedTextField(
            value = descripcion,
            onValueChange = {
                descripcion = it
            },
            label = {
                androidx.compose.material3.Text("Descripción")
            },
            modifier = androidx.compose.ui.Modifier.fillMaxWidth()
        )

        androidx.compose.foundation.layout.Spacer(
            modifier = androidx.compose.ui.Modifier.height(12.dp)
        )

        androidx.compose.material3.OutlinedTextField(
            value = categoria,
            onValueChange = {
                categoria = it
            },
            label = {
                androidx.compose.material3.Text("Categoría")
            },
            modifier = androidx.compose.ui.Modifier.fillMaxWidth()
        )

        androidx.compose.foundation.layout.Spacer(
            modifier = androidx.compose.ui.Modifier.height(12.dp)
        )

        androidx.compose.material3.OutlinedTextField(
            value = estado,
            onValueChange = {
                estado = it
            },
            label = {
                androidx.compose.material3.Text("Estado")
            },
            modifier = androidx.compose.ui.Modifier.fillMaxWidth()
        )

        androidx.compose.foundation.layout.Spacer(
            modifier = androidx.compose.ui.Modifier.height(20.dp)
        )

        androidx.compose.material3.Button(
            onClick = {

                val productoActualizado =
                    producto.copy(
                        nombre = nombre,
                        descripcion = descripcion,
                        categoria = categoria,
                        estado = estado
                    )

                onGuardar(productoActualizado)
            },
            modifier = androidx.compose.ui.Modifier.fillMaxWidth()
        ) {
            androidx.compose.material3.Text(
                text = "Guardar cambios"
            )
        }

        androidx.compose.foundation.layout.Spacer(
            modifier = androidx.compose.ui.Modifier.height(10.dp)
        )

        androidx.compose.material3.OutlinedButton(
            onClick = onCancelar,
            modifier = androidx.compose.ui.Modifier.fillMaxWidth()
        ) {
            androidx.compose.material3.Text(
                text = "Cancelar"
            )
        }
    }
}