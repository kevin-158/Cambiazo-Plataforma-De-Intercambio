package com.example.cambiazoapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cambiazoapp.ui.screens.DetalleProductoScreen
import com.example.cambiazoapp.ui.screens.HomeScreen
import com.example.cambiazoapp.ui.screens.IntercambiosScreen
import com.example.cambiazoapp.ui.screens.LoginScreen
import com.example.cambiazoapp.ui.screens.MiInventarioScreen
import com.example.cambiazoapp.ui.screens.PublicarProductoScreen
import com.example.cambiazoapp.ui.screens.RegistroScreen

/**
 * Controlador central de navegacion entre todas las pantallas de Cambiazo.
 */
@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // Pantalla 1: Inicio de Sesion (RF-01, RF-02, RF-03)
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

        // Pantalla 2: Registro de Usuario (RF-04, RF-05, RF-06)
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

        // Pantalla 3: Feed Principal / Catalogo (RF-07, RF-08, RF-09)
        composable("inicio") {
            HomeScreen(
                onIrAPublicar = {
                    navController.navigate("publicar")
                },
                onIrAInventario = {
                    navController.navigate("inventario")
                },
                onIrAActividad = {
                    navController.navigate("intercambios")
                },
                onIrADetalleProducto = { productoId ->
                    navController.navigate("detalle/$productoId")
                }
            )
        }

        // Pantalla 4 y 5: Detalle del Articulo (RF-10) + Modal de Propuesta de Trueque (RF-13)
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
                onIrAPublicarNuevo = {
                    navController.navigate("publicar")
                },
                onPropuestaEnviadaIrATrueques = {
                    navController.navigate("intercambios") {
                        popUpTo("inicio")
                    }
                }
            )
        }

        // Pantalla 6 (Estado de propuestas): Actividad de Trueques (RF-17)
        composable("intercambios") {
            IntercambiosScreen(
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla 7: Publicar Nuevo Articulo con multiples fotos y portada (RF-19, RF-20, RF-21)
        composable("publicar") {
            PublicarProductoScreen(
                onProductoPublicado = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla 8: Mi Inventario / Gestion de articulos propios (RF-22)
        composable("inventario") {
            MiInventarioScreen(
                onIrAHome = {
                    navController.navigate("inicio") {
                        popUpTo("inicio") { inclusive = true }
                    }
                },
                onIrAPublicar = {
                    navController.navigate("publicar")
                },
                onIrAActividad = {
                    navController.navigate("intercambios")
                }
            )
        }
    }
}