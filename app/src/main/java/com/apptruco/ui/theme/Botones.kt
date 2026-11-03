package com.apptruco.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val BotonRadius = 12.dp
private val DefaultPadding = PaddingValues(horizontal = 8.dp)

/**
 * Cómo se pinta un botón de cartel: un fondo, las líneas de filete por dentro y, abajo, una
 * sombra corrida sin desenfoque, como si el botón estuviera pintado sobre la tabla.
 */
@Immutable
data class Pintura(
    val fondo: Brush? = null,
    val filetes: List<Filete> = emptyList(),
    val sombra: Color = Color.Transparent,
    val sombraAlto: Dp = 2.dp,
    val radius: Dp = BotonRadius,
)

/** Degradé del equipo, con contorno negro, una banda del color del equipo y un filete de oro. */
fun pinturaPintada(tonos: Tonos) = Pintura(
    fondo = Brush.verticalGradient(0f to tonos.claro, 0.38f to tonos.base, 1f to tonos.oscuro),
    filetes = listOf(
        Filete(0.dp, 1.5.dp, Contorno),
        Filete(1.5.dp, 2.dp, tonos.base),
        Filete(3.5.dp, 1.2.dp, Oro),
    ),
    sombra = Color.Black.copy(alpha = 0.4f),
    sombraAlto = 3.dp,
)

/** Crema con contorno de tinta; [conOro] le suma el filete de oro de los botones de puntos. */
fun pinturaCrema(conOro: Boolean = false) = Pintura(
    fondo = Brush.verticalGradient(listOf(BotonCremaArriba, BotonCremaAbajo)),
    filetes = if (conOro) {
        listOf(Filete(0.dp, 1.5.dp, Tinta), Filete(1.5.dp, 2.dp, BotonCremaArriba), Filete(3.5.dp, 1.dp, Oro))
    } else {
        listOf(Filete(0.dp, 1.5.dp, Tinta))
    },
    sombra = Color.Black.copy(alpha = if (conOro) 0.25f else 0.4f),
)

/** Negro con borde de oro, para los cantos que se pueden subir. */
val PinturaNegra = Pintura(
    fondo = SolidColor(NegroAlto),
    filetes = listOf(Filete(0.dp, 1.2.dp, Oro)),
    sombra = Color.Black,
)

/** Sin fondo, solo el contorno: para las acciones que no tienen que llamar la atención. */
fun pinturaContorno(color: Color, width: Dp = 1.2.dp, radius: Dp = BotonRadius) =
    Pintura(filetes = listOf(Filete(0.dp, width, color)), radius = radius)

fun Modifier.pintura(pintura: Pintura): Modifier = this
    .drawBehind {
        if (pintura.sombra.alpha > 0f) {
            drawRoundRect(
                color = pintura.sombra,
                topLeft = Offset(0f, pintura.sombraAlto.toPx()),
                size = size,
                cornerRadius = CornerRadius(minOf(pintura.radius.toPx(), size.minDimension / 2f)),
            )
        }
    }
    .then(pintura.fondo?.let { Modifier.background(it, RoundedCornerShape(pintura.radius)) } ?: Modifier)
    .filetes(pintura.radius, pintura.filetes)

@Composable
fun BotonFilete(
    onClick: () -> Unit,
    pintura: Pintura,
    contentColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = DefaultPadding,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .alpha(if (enabled) 1f else 0.4f)
            .pintura(pintura),
        enabled = enabled,
        shape = RoundedCornerShape(pintura.radius),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = contentColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = contentColor,
        ),
        elevation = null,
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
fun BotonPintado(
    onClick: () -> Unit,
    tonos: Tonos,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = DefaultPadding,
    content: @Composable RowScope.() -> Unit,
) {
    BotonFilete(onClick, pinturaPintada(tonos), CremaPintada, modifier, enabled, contentPadding, content)
}

@Composable
fun BotonCrema(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    conOro: Boolean = false,
    contentPadding: PaddingValues = DefaultPadding,
    content: @Composable RowScope.() -> Unit,
) {
    BotonFilete(onClick, pinturaCrema(conOro), Tinta, modifier, contentPadding = contentPadding, content = content)
}

@Composable
fun BotonNegro(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = DefaultPadding,
    content: @Composable RowScope.() -> Unit,
) {
    BotonFilete(onClick, PinturaNegra, OroClaro, modifier, contentPadding = contentPadding, content = content)
}

@Composable
fun BotonContorno(
    onClick: () -> Unit,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    width: Dp = 1.2.dp,
    enabled: Boolean = true,
    contentPadding: PaddingValues = DefaultPadding,
    content: @Composable RowScope.() -> Unit,
) {
    BotonFilete(onClick, pinturaContorno(color, width), contentColor, modifier, enabled, contentPadding, content)
}

/** Sombra corrida de la letra pintada: un dp abajo a la derecha y sin desenfoque. */
@Composable
@ReadOnlyComposable
fun TextStyle.conSombra(color: Color = Color.Black.copy(alpha = 0.35f)): TextStyle {
    val corrimiento = with(LocalDensity.current) { 1.dp.toPx() }
    return copy(shadow = Shadow(color = color, offset = Offset(corrimiento, corrimiento)))
}
