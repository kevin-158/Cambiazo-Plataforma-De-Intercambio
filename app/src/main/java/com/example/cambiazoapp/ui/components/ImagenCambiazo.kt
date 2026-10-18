package com.example.cambiazoapp.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.cambiazoapp.ui.theme.InputBackground
import com.example.cambiazoapp.ui.theme.MutedText

@Composable
fun CambiazoImagen(
    imagenData: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val cadena = imagenData as? String

    // Si es una imagen guardada en Base64 (con o sin prefijo data:image), la decodificamos a ImageBitmap
    if (cadena != null && (cadena.startsWith("data:image") || (cadena.length > 250 && !cadena.startsWith("http")))) {
        val imageBitmap = remember(cadena) {
            try {
                val base64Puro = cadena.substringAfter("base64,", cadena)
                val bytes = Base64.decode(base64Puro, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }

        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        } else {
            PlaceholderSinImagen(modifier)
        }
    } else if (imagenData != null && cadena?.isBlank() != true) {
        // Si es una URI local de la galería o una URL http/https de Firebase Storage
        AsyncImage(
            model = imagenData,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        PlaceholderSinImagen(modifier)
    }
}

@Composable
fun ImagenCambiazo(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    CambiazoImagen(
        imagenData = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

@Composable
private fun PlaceholderSinImagen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(InputBackground),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ImageNotSupported,
            contentDescription = "Sin imagen",
            tint = MutedText,
            modifier = Modifier.size(36.dp)
        )
    }
}