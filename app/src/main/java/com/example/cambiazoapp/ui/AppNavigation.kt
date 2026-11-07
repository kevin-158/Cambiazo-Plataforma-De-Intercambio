package com.example.cambiazoapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cambiazoapp.ui.screens.ChatTradeScreen
import com.example.cambiazoapp.ui.screens.DetalleProductoScreen
import com.example.cambiazoapp.ui.screens.HomeScreen
import com.example.cambiazoapp.ui.screens.IntercambiosScreen
import com.example.cambiazoapp.ui.screens.LoginScreen
import com.example.cambiazoapp.ui.screens.MiInventarioScreen
import com.example.cambiazoapp.ui.screens.PerfilPublicoScreen
import com.example.cambiazoapp.ui.screens.PublicarProductoScreen
import com.example.cambiazoapp.ui.screens.RegistroScreen

// Controlador central de navegación entre todas las pantallas de Cambiazo
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
                    navController.navigate("inventario") {
                        launchSingleTop = true
                    }
                },
                onIrAActividad = {
                    navController.navigate("intercambios") {
                        launchSingleTop = true
                    }
                },
                onIrADetalleProducto = { productoId ->
                    navController.navigate("detalle/$productoId")
                }
            )
        }

        // Pantalla 4 y 5: Detalle del Artículo (RF-10, RF-11) + Modal de Propuesta (RF-13)
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
                },
                onVerPerfilPublico = { duenoId ->
                    if (duenoId.isNotBlank()) {
                        navController.navigate("perfil_publico/$duenoId")
                    }
                }
            )
        }

        // Listado de Propuestas y Mensajes Nuevos con barra inferior activa en "Trueques" (RF-16, RF-17)
        composable("intercambios") {
            IntercambiosScreen(
                onVolver = {
                    navController.popBackStack()
                },
                onIrAInicio = {
                    navController.navigate("inicio") {
                        popUpTo("inicio") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onIrAPublicar = {
                    navController.navigate("publicar")
                },
                onIrAPerfil = {
                    navController.navigate("inventario") {
                        launchSingleTop = true
                    }
                },
                onAbrirChatNegociacion = { intercambioId ->
                    navController.navigate("chat/$intercambioId")
                }
            )
        }

        // Pantalla 6 y 9: Chat en Tiempo Real (RF-16), Mapa de Puntos Seguros (RF-18) y Calificación (RF-25)
        composable(
            route = "chat/{intercambioId}",
            arguments = listOf(navArgument("intercambioId") { type = NavType.StringType })
        ) { backStackEntry ->
            val intercambioId = backStackEntry.arguments?.getString("intercambioId") ?: ""
            ChatTradeScreen(
                intercambioId = intercambioId,
                onVolver = {
                    navController.popBackStack()
                },
                onVerPerfilPublico = { otroUsuarioId ->
                    if (otroUsuarioId.isNotBlank()) {
                        navController.navigate("perfil_publico/$otroUsuarioId")
                    }
                }
            )
        }

        // Perfil Público del Dueño con Pestañas Inventario, Historial y Reseñas (RF-11, RF-24)
        composable(
            route = "perfil_publico/{usuarioId}",
            arguments = listOf(navArgument("usuarioId") { type = NavType.StringType })
        ) { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getString("usuarioId") ?: ""
            PerfilPublicoScreen(
                usuarioId = usuarioId,
                onVolver = {
                    navController.popBackStack()
                },
                onIrADetalleProducto = { productoId ->
                    navController.navigate("detalle/$productoId")
                }
            )
        }

        // Pantalla 7: Publicar Nuevo Artículo (RF-19, RF-20, RF-21, RF-09)
        composable("publicar") {
            PublicarProductoScreen(
                onProductoPublicado = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla 8: Mi Inventario y Mi Perfil con barra inferior activa en "Perfil" (RF-22, RF-23, RF-24)
        composable("inventario") {
            MiInventarioScreen(
                onVolver = {
                    navController.popBackStack()
                },
                onIrAInicio = {
                    navController.navigate("inicio") {
                        popUpTo("inicio") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onIrAPublicar = {
                    navController.navigate("publicar")
                },
                onIrAActividad = {
                    navController.navigate("intercambios") {
                        launchSingleTop = true
                    }
                },
                onCerrarSesion = {
                    navController.navigate("login") {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}