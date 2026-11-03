package com.apptruco.ui.game

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apptruco.R
import com.apptruco.domain.model.GameConfig
import com.apptruco.ui.theme.CartaAbajo
import com.apptruco.ui.theme.Contorno
import com.apptruco.ui.theme.FosforoCabeza
import com.apptruco.ui.theme.FosforoCabezaLuz
import com.apptruco.ui.theme.FosforoCabezaSombra
import com.apptruco.ui.theme.FosforoPalo
import com.apptruco.ui.theme.FosforoSombra
import com.apptruco.ui.theme.GuiaCasita
import com.apptruco.ui.theme.Oro
import com.apptruco.ui.theme.Rojo
import com.apptruco.ui.theme.TintaApagada
import kotlin.math.atan2
import kotlin.math.sqrt

private const val POINTS_PER_BOX = 5
private val MaxBoxSide = 96.dp

// Medidas de la casita en unidades: cada lado mide 100.
private const val SIDE = 100f
private const val GAP = 18f
private const val TRIM = 6f
private const val MARGIN = 12f

// La diagonal se acorta 9 en cada eje para que su cabeza no choque con la del lado derecho.
private const val DIAGONAL = 4
private val DiagonalTrim = 9f * sqrt(2f)

private const val STICK_OUTLINE = 10f
private const val STICK_WIDTH = 6.5f
private const val HEAD_RX = 9.5f
private const val HEAD_RY = 7.2f
private const val HEAD_OUTLINE = 1.4f
private const val GUIDE_WIDTH = 2.5f
private const val GUIDE_RADIUS = 6f
private val ShadowShift = Offset(1.8f, 2.6f)
private val ShadowColor = FosforoSombra.copy(alpha = 0.35f)

/**
 * Anotación clásica con fósforos: cada casita son cuatro lados más la diagonal (cinco puntos).
 * A 30 se separan las malas de las buenas con una raya; a 15 es una sola columna.
 */
@Composable
fun TallyMarks(
    points: Int,
    config: GameConfig,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    // Se lee al dibujar y no al componer: mientras anima solo se repintan los fósforos.
    val drawn = animateFloatAsState(
        targetValue = points.toFloat(),
        animationSpec = tween(durationMillis = 450),
        label = "tally",
    )
    val description = pluralStringResource(R.plurals.puntos, points, points)

    Column(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (config.hasBuenas) {
            val inBuenas = points > config.malas
            SegmentLabel(R.string.malas, color = if (inBuenas) TintaApagada else accent)
            TallyColumn(
                sticks = { drawn.value },
                boxes = config.malas / POINTS_PER_BOX,
                modifier = Modifier.weight(1f),
            )
            BuenasDivider(modifier = Modifier.padding(vertical = 4.dp))
            SegmentLabel(R.string.buenas, color = if (inBuenas) accent else TintaApagada)
            TallyColumn(
                sticks = { drawn.value - config.malas },
                boxes = (config.target - config.malas) / POINTS_PER_BOX,
                modifier = Modifier.weight(1f),
            )
        } else {
            TallyColumn(
                sticks = { drawn.value },
                boxes = config.target / POINTS_PER_BOX,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SegmentLabel(
    @StringRes text: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(text).lowercase(),
        modifier = modifier.padding(bottom = 4.dp),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.5.sp),
        color = color,
    )
}

/** Raya roja que separa las malas de las buenas, con un rombo de oro al medio. */
@Composable
private fun BuenasDivider(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp),
    ) {
        val inset = 6.dp.toPx()
        val y = 6.75.dp.toPx()
        drawLine(
            brush = Brush.horizontalGradient(
                0f to Color.Transparent,
                0.15f to Rojo,
                0.85f to Rojo,
                1f to Color.Transparent,
                startX = inset,
                endX = size.width - inset,
            ),
            start = Offset(inset, y),
            end = Offset(size.width - inset, y),
            strokeWidth = 1.5.dp.toPx(),
        )
        val center = Offset(size.width / 2f, 7.dp.toPx())
        rotate(45f, pivot = center) {
            val ring = 14.dp.toPx()
            val diamond = 10.dp.toPx()
            drawRect(CartaAbajo, topLeft = center - Offset(ring, ring) / 2f, size = Size(ring, ring))
            drawRect(Oro, topLeft = center - Offset(diamond, diamond) / 2f, size = Size(diamond, diamond))
        }
    }
}

/**
 * Columna de casitas en su propia capa fuera de pantalla: mientras no cambian los puntos se reusa
 * lo ya pintado en lugar de volver a dibujar cada fósforo en cada cuadro.
 */
@Composable
private fun TallyColumn(
    sticks: () -> Float,
    boxes: Int,
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithCache {
                val casitas = Casitas.medir(boxes, size, maxUnit = MaxBoxSide.toPx() / SIDE)
                onDrawBehind { casitas?.let { drawCasitas(it, sticks()) } }
            },
    )
}

/** Un lado de la casita: del arranque a la punta, ya recortado, y su ángulo para la cabeza. */
private class Fosforo(val start: Offset, val end: Offset, val degrees: Float)

/**
 * Lo que no cambia entre cuadros para un tamaño de columna: dónde va cada casita y cada fósforo,
 * los trazos y el degradé de las cabezas. Al dibujar solo falta contar cuántos van.
 */
private class Casitas private constructor(boxes: Int, size: Size, val unit: Float) {
    val side = SIDE * unit
    val guideRadius = CornerRadius(GUIDE_RADIUS * unit)
    val guide = Stroke(
        width = GUIDE_WIDTH * unit,
        cap = StrokeCap.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(1f * unit, 8f * unit)),
    )
    val shadowShift = ShadowShift * unit
    val head = Size(2 * HEAD_RX * unit, 2 * HEAD_RY * unit)

    // La cabeza se dibuja con el origen en su centro, así un mismo degradé sirve para todas.
    val headTopLeft = Offset(-head.width / 2f, -head.height / 2f)
    val headBrush = Brush.radialGradient(
        0f to FosforoCabezaLuz,
        0.55f to FosforoCabeza,
        1f to FosforoCabezaSombra,
        center = headTopLeft + Offset(head.width * 0.38f, head.height * 0.35f),
        radius = 0.8f * (head.width + head.height) / 2f,
    )
    val headOutline = Stroke(width = HEAD_OUTLINE * unit)

    val boxTopLefts: List<Offset>
    val fosforos: List<Fosforo>

    init {
        val height = boxes * SIDE + (boxes - 1) * GAP
        val left = (size.width - side) / 2f
        val top = (size.height - height * unit) / 2f
        boxTopLefts = List(boxes) { index -> Offset(left, top + index * (SIDE + GAP) * unit) }
        fosforos = boxTopLefts.flatMap { topLeft -> ladosDe(topLeft) }
    }

    private fun ladosDe(topLeft: Offset): List<Fosforo> {
        val topRight = topLeft + Offset(side, 0f)
        val bottomLeft = topLeft + Offset(0f, side)
        val bottomRight = topLeft + Offset(side, side)
        val order = listOf(
            bottomLeft to topLeft,
            topLeft to topRight,
            topRight to bottomRight,
            bottomRight to bottomLeft,
            topLeft to bottomRight,
        )
        return order.mapIndexed { index, (from, to) ->
            val trim = if (index == DIAGONAL) TRIM + DiagonalTrim else TRIM
            val (start, end) = trimmed(from, to, by = trim * unit)
            val direction = to - from
            Fosforo(start, end, Math.toDegrees(atan2(direction.y, direction.x).toDouble()).toFloat())
        }
    }

    companion object {
        fun medir(boxes: Int, size: Size, maxUnit: Float): Casitas? {
            val height = boxes * SIDE + (boxes - 1) * GAP
            val unit = minOf(size.width * 0.7f / SIDE, size.height / (height + 2 * MARGIN), maxUnit)
            return if (unit > 0f) Casitas(boxes, size, unit) else null
        }
    }
}

private fun DrawScope.drawCasitas(casitas: Casitas, sticks: Float) {
    casitas.boxTopLefts.forEachIndexed { box, topLeft ->
        drawRoundRect(
            color = GuiaCasita,
            topLeft = topLeft,
            size = Size(casitas.side, casitas.side),
            cornerRadius = casitas.guideRadius,
            style = casitas.guide,
        )
        repeat(POINTS_PER_BOX) { lado ->
            val index = box * POINTS_PER_BOX + lado
            val progress = (sticks - index).coerceIn(0f, 1f)
            if (progress > 0f) drawFosforo(casitas.fosforos[index], progress, casitas)
        }
    }
}

/** Fósforo pintado: palo dorado con contorno negro, cabeza roja en la punta y la sombra sobre la carta. */
private fun DrawScope.drawFosforo(
    fosforo: Fosforo,
    progress: Float,
    casitas: Casitas,
) {
    val start = fosforo.start
    val tip = lerp(start, fosforo.end, progress)
    val shift = casitas.shadowShift
    val unit = casitas.unit
    drawLine(ShadowColor, start + shift, tip + shift, strokeWidth = STICK_OUTLINE * unit, cap = StrokeCap.Round)
    drawHead(center = tip + shift, degrees = fosforo.degrees, casitas = casitas, shadow = true)
    drawLine(Contorno, start, tip, strokeWidth = STICK_OUTLINE * unit, cap = StrokeCap.Round)
    drawLine(FosforoPalo, start, tip, strokeWidth = STICK_WIDTH * unit, cap = StrokeCap.Round)
    drawHead(center = tip, degrees = fosforo.degrees, casitas = casitas)
}

private fun DrawScope.drawHead(
    center: Offset,
    degrees: Float,
    casitas: Casitas,
    shadow: Boolean = false,
) {
    translate(center.x, center.y) {
        rotate(degrees, pivot = Offset.Zero) {
            if (shadow) {
                drawOval(ShadowColor, topLeft = casitas.headTopLeft, size = casitas.head)
            } else {
                drawOval(brush = casitas.headBrush, topLeft = casitas.headTopLeft, size = casitas.head)
                drawOval(Contorno, topLeft = casitas.headTopLeft, size = casitas.head, style = casitas.headOutline)
            }
        }
    }
}

private fun trimmed(from: Offset, to: Offset, by: Float): Pair<Offset, Offset> {
    val direction = (to - from) / (to - from).getDistance()
    return (from + direction * by) to (to - direction * by)
}
