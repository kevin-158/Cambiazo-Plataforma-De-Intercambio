package com.example.cambiazoapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cambiazoapp.ui.screens.LoginScreen
import com.example.cambiazoapp.ui.screens.RegistroScreen
import com.example.cambiazoapp.ui.screens.PublicarProductoScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.cambiazoapp.ui.screens.MiInventarioScreen

//import androidx.compose.material3.Button
//import androidx.compose.material3.Text

//import androidx.compose.foundation.layout.Spacer


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
        //agrega la ruta publicar
        composable("publicar") {
            PublicarProductoScreen(
                onProductoPublicado = {
                    navController.popBackStack()
                }
            )
        }
        composable("inventario") {
            MiInventarioScreen()
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

        composable("inicio") {
            TextoInicio(
                onIrAPublicar = {
                    navController.navigate("publicar")
                },
                onIrAInventario = {
                    navController.navigate("inventario")
                }
            )
        }
    }
}

@Composable
fun TextoInicio(
    onIrAPublicar: () -> Unit,
    onIrAInventario: () -> Unit
) {
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {

        androidx.compose.material3.Text(
            text = "Bienvenido a Cambiazo"
        )

        androidx.compose.foundation.layout.Spacer(
            modifier = androidx.compose.ui.Modifier.height(20.dp)
        )

        androidx.compose.material3.Button(
            onClick = onIrAPublicar
        ) {
            androidx.compose.material3.Text(
                text = "Publicar artículo"
            )
        }

        androidx.compose.foundation.layout.Spacer(
            modifier = androidx.compose.ui.Modifier.height(12.dp)
        )

        androidx.compose.material3.Button(
            onClick = onIrAInventario
        ) {
            androidx.compose.material3.Text(
                text = "Mi inventario"
            )
        }
    }
}
