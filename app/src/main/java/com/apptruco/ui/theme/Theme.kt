package com.apptruco.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// primary es Nosotros y tertiary es Ellos: el tono claro, que es el que se lee sobre el negro.
// Los contenedores llevan el tono base, el de los degradés y las series del gráfico.
private val TrucoColorScheme = darkColorScheme(
    primary = RojoClaro,
    onPrimary = Tinta,
    primaryContainer = Rojo,
    onPrimaryContainer = CremaPintada,
    secondary = Celeste,
    onSecondary = CelesteTinta,
    secondaryContainer = NegroCartel,
    onSecondaryContainer = OroClaro,
    tertiary = AzulClaro,
    onTertiary = Tinta,
    tertiaryContainer = Azul,
    onTertiaryContainer = CremaPintada,
    background = Negro,
    onBackground = Crema,
    surface = Negro,
    onSurface = Crema,
    surfaceVariant = NegroCartel,
    onSurfaceVariant = CremaTenue,
    surfaceContainerLowest = Negro,
    surfaceContainerLow = Color(0xFF111111),
    surfaceContainer = NegroAlto,
    surfaceContainerHigh = NegroCartel,
    surfaceContainerHighest = Color(0xFF262626),
    outline = LineaGrisClara,
    outlineVariant = Color(0xFF3A3329),
    error = RojoClaro,
    onError = Tinta,
)

// Sobre la carta crema: tinta oscura, líneas de lápiz y el oro quemado para las referencias.
private val CartaColorScheme = TrucoColorScheme.copy(
    surface = Crema,
    onSurface = Tinta,
    onSurfaceVariant = LineaGris,
    surfaceContainer = Crema,
    outline = OroOscuro,
    outlineVariant = GuiaCasita,
)

/** Siempre oscuro: se usa sobre la mesa, muchas veces de noche, y tiene que leerse de lejos. */
@Composable
fun TrucoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TrucoColorScheme,
        typography = TrucoTypography,
        content = content,
    )
}

/** Colores para lo que se dibuja sobre una carta crema en lugar del fondo negro. */
@Composable
fun SobreLaCarta(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CartaColorScheme, typography = MaterialTheme.typography) {
        CompositionLocalProvider(LocalContentColor provides Tinta, content = content)
    }
}
