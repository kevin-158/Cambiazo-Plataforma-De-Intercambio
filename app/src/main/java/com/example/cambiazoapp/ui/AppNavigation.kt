package com.example.cambiazoapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cambiazoapp.ui.screens.DetalleProductoScreen
import com.example.cambiazoapp.ui.screens.HomeScreen
import com.example.cambiazoapp.ui.screens.LoginScreen
import com.example.cambiazoapp.ui.screens.MiInventarioScreen
import com.example.cambiazoapp.ui.screens.PublicarProductoScreen
import com.example.cambiazoapp.ui.screens.RegistroScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                onIrARegistro = {
                    navController.navigate("registro")
                },
                onLoginCorrecto = {
                    navController.navigate("inicio") {
                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("registro") {
            RegistroScreen(
                onIrALogin = {
                    navController.popBackStack()
                },
                onRegistroCorrecto = {
                    navController.navigate("inicio") {
                        popUpTo("registro") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Pantalla 3: Feed Principal / Catálogo (RF-07, RF-08, RF-09)
        composable("inicio") {
            HomeScreen(
                onIrAPublicar = {
                    navController.navigate("publicar")
                },
                onIrAInventario = {
                    navController.navigate("inventario")
                },
                onIrAActividad = {
                    // En la Fase 4 conectaremos aquí la pantalla de Propuestas/Trueques (RF-17)
                    navController.navigate("inventario")
                },
                onIrADetalleProducto = { productoId ->
                    navController.navigate("detalle/$productoId")
                }
            )
        }

        // Pantalla 4: Detalle del Artículo con Galería de Imágenes (RF-10, RF-11, RF-12)
        composable(
            route = "detalle/{productoId}",
            arguments = listOf(navArgument("productoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productoId = backStackEntry.arguments?.getString("productoId") ?: ""
            DetalleProductoScreen(
                productoId = productoId,
                onVolver = {
                    navController.popBackStack()
                },
                onProponerTrueque = { idSolicitado ->
                    // En la Fase 4 abriremos aquí el Modal de Propuesta de Trueque (RF-13)
                }
            )
        }

        // Pantalla 7: Publicar Nuevo Artículo (RF-19, RF-20, RF-21)
        composable("publicar") {
            PublicarProductoScreen(
                onProductoPublicado = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla 8: Mi Inventario / Perfil (RF-22)
        composable("inventario") {
            MiInventarioScreen()
        }
    }
}