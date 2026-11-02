package com.apptruco.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TrucoColorScheme = darkColorScheme(
    primary = Amber,
    onPrimary = AmberInk,
    primaryContainer = AmberSeries,
    onPrimaryContainer = AmberInk,
    secondary = Mint,
    onSecondary = MintInk,
    secondaryContainer = FeltButton,
    onSecondaryContainer = Chalk,
    tertiary = Sky,
    onTertiary = SkyInk,
    tertiaryContainer = SkySeries,
    onTertiaryContainer = SkyInk,
    background = Ink,
    onBackground = Chalk,
    surface = Ink,
    onSurface = Chalk,
    surfaceVariant = FeltVariant,
    onSurfaceVariant = ChalkMuted,
    surfaceContainerLowest = Ink,
    surfaceContainerLow = Felt,
    surfaceContainer = Felt,
    surfaceContainerHigh = FeltRaised,
    surfaceContainerHighest = FeltHighest,
    outline = Line,
    outlineVariant = LineSoft,
    error = Coral,
    onError = CoralInk,
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
