package com.apptruco.ui.performance

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apptruco.R
import com.apptruco.domain.model.GameConfig
import com.apptruco.domain.model.Score
import com.apptruco.domain.model.Team
import com.apptruco.ui.game.label
import com.apptruco.ui.game.seriesColor
import kotlin.math.abs
import kotlin.math.roundToInt

private val PlotInsetStart = 32.dp
private val PlotInsetEnd = 40.dp
private val PlotInsetTop = 12.dp
private val PlotInsetBottom = 28.dp
private val LineWidth = 2.dp
private val MarkerRadius = 5.dp
private val MarkerRing = 2.dp
private const val GRID_STEP = 5
private val X_TICK_STEPS = listOf(1, 2, 5, 10, 20, 50, 100)

/**
 * Evolución del tanteador en escalones: cada paso es una jugada que movió el puntaje.
 * La grilla va de a cinco, como las casitas, y la raya de las buenas queda marcada.
 */
@Composable
fun ScoreChart(
    progression: List<Score>,
    config: GameConfig,
    selected: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val reveal = remember(progression) { Animatable(0f) }
    LaunchedEffect(reveal) {
        reveal.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }
    val currentOnSelect by rememberUpdatedState(onSelect)

    val series = Team.entries.associateWith { it.seriesColor() }
    val surface = MaterialTheme.colorScheme.surfaceContainer
    val grid = MaterialTheme.colorScheme.outlineVariant
    val threshold = MaterialTheme.colorScheme.outline
    val crosshair = MaterialTheme.colorScheme.onSurfaceVariant
    val axisStyle = MaterialTheme.typography.labelMedium.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.sp,
        fontFeatureSettings = "tnum",
    )
    val valueStyle = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurface)
    val buenasLabel = stringResource(R.string.buenas)
    val final = progression.last()
    val description = stringResource(
        R.string.performance_chart_description,
        Team.NOSOTROS.label(),
        final.nosotros,
        Team.ELLOS.label(),
        final.ellos,
    )
    val lastIndex = progression.lastIndex

    Canvas(
        modifier = modifier
            .clearAndSetSemantics { contentDescription = description }
            .pointerInput(lastIndex) {
                detectTapGestures { currentOnSelect(indexAt(it.x, size.width, lastIndex)) }
            }
            .pointerInput(lastIndex) {
                detectHorizontalDragGestures { change, _ ->
                    currentOnSelect(indexAt(change.position.x, size.width, lastIndex))
                }
            },
    ) {
        val left = PlotInsetStart.toPx()
        val right = size.width - PlotInsetEnd.toPx()
        val top = PlotInsetTop.toPx()
        val bottom = size.height - PlotInsetBottom.toPx()
        fun x(index: Int) = if (lastIndex == 0) left else left + (right - left) * index / lastIndex
        fun y(points: Int) = bottom - (bottom - top) * points / config.target

        for (value in 0..config.target step GRID_STEP) {
            drawLine(grid, Offset(left, y(value)), Offset(right, y(value)), strokeWidth = 1.dp.toPx())
            val label = textMeasurer.measure(value.toString(), axisStyle)
            drawText(label, topLeft = Offset(left - 8.dp.toPx() - label.size.width, y(value) - label.size.height / 2f))
        }
        if (config.hasBuenas) {
            val line = y(config.malas)
            drawLine(threshold, Offset(left, line), Offset(right, line), strokeWidth = 1.dp.toPx())
            val label = textMeasurer.measure(buenasLabel, axisStyle)
            drawText(label, topLeft = Offset(left + 6.dp.toPx(), line - label.size.height - 2.dp.toPx()))
        }
        val tickStep = X_TICK_STEPS.firstOrNull { lastIndex / it <= 6 } ?: lastIndex
        for (index in 0..lastIndex step tickStep.coerceAtLeast(1)) {
            val label = textMeasurer.measure(index.toString(), axisStyle)
            drawText(label, topLeft = Offset(x(index) - label.size.width / 2f, bottom + 6.dp.toPx()))
        }

        val stroke = Stroke(width = LineWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        clipRect(right = left + (right - left) * reveal.value + MarkerRadius.toPx()) {
            Team.entries.forEach { team ->
                val path = Path().apply {
                    moveTo(x(0), y(progression[0][team]))
                    for (index in 1..lastIndex) {
                        lineTo(x(index), y(progression[index - 1][team]))
                        lineTo(x(index), y(progression[index][team]))
                    }
                }
                drawPath(path, series.getValue(team), style = stroke)
            }
        }

        if (reveal.value >= 1f) {
            val ends = Team.entries.associateWith { Offset(x(lastIndex), y(final[it])) }
            ends.forEach { (team, point) -> drawMarker(point, series.getValue(team), surface) }
            val labels = Team.entries.associateWith { textMeasurer.measure(final[it].toString(), valueStyle) }
            val separation = abs(ends.getValue(Team.NOSOTROS).y - ends.getValue(Team.ELLOS).y)
            if (separation >= labels.values.maxOf { it.size.height }) {
                labels.forEach { (team, label) ->
                    drawText(
                        label,
                        topLeft = Offset(right + 10.dp.toPx(), ends.getValue(team).y - label.size.height / 2f),
                    )
                }
            }
        }

        selected?.takeIf { it in progression.indices }?.let { index ->
            drawLine(crosshair, Offset(x(index), top), Offset(x(index), bottom), strokeWidth = 1.dp.toPx())
            Team.entries.forEach { team ->
                drawMarker(Offset(x(index), y(progression[index][team])), series.getValue(team), surface)
            }
        }
    }
}

private fun Density.indexAt(x: Float, width: Int, lastIndex: Int): Int {
    val left = PlotInsetStart.toPx()
    val plotWidth = width - left - PlotInsetEnd.toPx()
    return ((x - left) / plotWidth * lastIndex).roundToInt().coerceIn(0, lastIndex)
}

private fun DrawScope.drawMarker(center: Offset, color: Color, ring: Color) {
    drawCircle(ring, radius = (MarkerRadius + MarkerRing).toPx(), center = center)
    drawCircle(color, radius = MarkerRadius.toPx(), center = center)
}
