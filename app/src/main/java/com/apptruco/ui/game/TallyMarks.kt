package com.apptruco.ui.game

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.apptruco.R
import com.apptruco.domain.model.GameConfig

private const val POINTS_PER_BOX = 5
private val BoxGap = 10.dp
private val MaxBoxSide = 96.dp

/**
 * Anotación clásica con fósforos: cada casita son cuatro lados más la diagonal (cinco puntos).
 * A 30 se separan las malas de las buenas con una raya; a 15 es una sola columna.
 */
@Composable
fun TallyMarks(
    points: Int,
    config: GameConfig,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val drawn by animateFloatAsState(
        targetValue = points.toFloat(),
        animationSpec = tween(durationMillis = 450),
        label = "tally",
    )
    val guide = MaterialTheme.colorScheme.outlineVariant
    val description = pluralStringResource(R.plurals.puntos, points, points)

    Column(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (config.hasBuenas) {
            val inBuenas = points > config.malas
            SegmentLabel(R.string.malas, highlighted = !inBuenas)
            TallyColumn(
                sticks = drawn,
                boxes = config.malas / POINTS_PER_BOX,
                color = color,
                guide = guide,
                modifier = Modifier.weight(1f),
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outline,
            )
            SegmentLabel(R.string.buenas, highlighted = inBuenas)
            TallyColumn(
                sticks = drawn - config.malas,
                boxes = (config.target - config.malas) / POINTS_PER_BOX,
                color = color,
                guide = guide,
                modifier = Modifier.weight(1f),
            )
        } else {
            TallyColumn(
                sticks = drawn,
                boxes = config.target / POINTS_PER_BOX,
                color = color,
                guide = guide,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SegmentLabel(
    @StringRes text: Int,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(text),
        modifier = modifier.padding(bottom = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = if (highlighted) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
}

@Composable
private fun TallyColumn(
    sticks: Float,
    boxes: Int,
    color: Color,
    guide: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        val gap = BoxGap.toPx()
        val side = minOf(
            size.width * 0.7f,
            (size.height - gap * (boxes - 1)) / boxes,
            MaxBoxSide.toPx(),
        )
        if (side <= 0f) return@Canvas
        val left = (size.width - side) / 2f
        val top = (size.height - side * boxes - gap * (boxes - 1)) / 2f
        repeat(boxes) { index ->
            drawCasita(
                topLeft = Offset(left, top + index * (side + gap)),
                side = side,
                sticks = sticks - index * POINTS_PER_BOX,
                color = color,
                guide = guide,
            )
        }
    }
}

private fun DrawScope.drawCasita(
    topLeft: Offset,
    side: Float,
    sticks: Float,
    color: Color,
    guide: Color,
) {
    val stroke = side * 0.11f
    drawRoundRect(
        color = guide,
        topLeft = topLeft,
        size = Size(side, side),
        cornerRadius = CornerRadius(stroke),
        style = Stroke(width = stroke * 0.3f),
    )

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
    order.forEachIndexed { index, (from, to) ->
        val progress = (sticks - index).coerceIn(0f, 1f)
        if (progress > 0f) {
            val (start, end) = trimmed(from, to, by = stroke)
            drawLine(
                color = color,
                start = start,
                end = lerp(start, end, progress),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}

private fun trimmed(from: Offset, to: Offset, by: Float): Pair<Offset, Offset> {
    val direction = (to - from) / (to - from).getDistance()
    return (from + direction * by) to (to - direction * by)
}
