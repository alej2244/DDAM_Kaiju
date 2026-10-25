package com.example.kaijuinventory.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = KaijuVerde40,
    primaryContainer = KaijuVerdeContainer,
    onPrimaryContainer = KaijuVerdeOnContainer,
    secondary = KaijuGris40,
    secondaryContainer = KaijuGrisContainer,
    tertiary = KaijuNaranjo40,
    tertiaryContainer = KaijuNaranjoContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = KaijuVerde80,
    primaryContainer = KaijuVerdeContainerDark,
    onPrimaryContainer = KaijuVerdeContainer,
    secondary = KaijuGris80,
    secondaryContainer = KaijuGrisContainerDark,
    tertiary = KaijuNaranjo80,
    tertiaryContainer = KaijuNaranjoContainerDark
)

/**
 * Tema Material 3 de la app. Se usa la paleta de marca en vez del color dinámico
 * del sistema para que la identidad visual de Kaiju sea igual en todos los equipos.
 */
@Composable
fun KaijuInventoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
