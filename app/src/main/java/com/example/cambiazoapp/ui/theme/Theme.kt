package com.example.cambiazoapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = PureWhiteSurface,
    secondary = NephriteSecondary,
    onSecondary = PureWhiteSurface,
    tertiary = DeepGreenBrand,
    background = Color(0xFF091D2E),
    surface = Color(0xFF132738),
    onBackground = PureWhiteSurface,
    onSurface = PureWhiteSurface,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = PureWhiteSurface,
    primaryContainer = LightGreenTint,
    onPrimaryContainer = DeepGreenBrand,
    secondary = DeepGreenBrand,
    onSecondary = PureWhiteSurface,
    tertiary = MutedText,
    background = OffWhiteBackground,
    surface = PureWhiteSurface,
    onBackground = DarkSlateText,
    onSurface = DarkSlateText,
    onSurfaceVariant = MutedText,
    outline = InputBorder,
    error = ErrorRed
)

@Composable
fun CambiazoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desactivamos dynamicColor por defecto para respetar la identidad visual de Cambiazo
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}