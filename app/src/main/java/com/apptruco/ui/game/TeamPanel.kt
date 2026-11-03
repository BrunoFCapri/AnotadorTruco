package com.apptruco.ui.game

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apptruco.R
import com.apptruco.domain.model.GameConfig
import com.apptruco.domain.model.Standing
import com.apptruco.domain.model.Team
import com.apptruco.ui.theme.BotonContorno
import com.apptruco.ui.theme.BotonCrema
import com.apptruco.ui.theme.BotonPintado
import com.apptruco.ui.theme.CartaAbajo
import com.apptruco.ui.theme.CartaArriba
import com.apptruco.ui.theme.CintaEquipoShape
import com.apptruco.ui.theme.CremaPintada
import com.apptruco.ui.theme.Filete
import com.apptruco.ui.theme.LineaGrisClara
import com.apptruco.ui.theme.Oro
import com.apptruco.ui.theme.OroClaro
import com.apptruco.ui.theme.OroContorno
import com.apptruco.ui.theme.OroOscuro
import com.apptruco.ui.theme.OroTinta
import com.apptruco.ui.theme.Tinta
import com.apptruco.ui.theme.Tonos
import com.apptruco.ui.theme.VolutasEnEsquinas
import com.apptruco.ui.theme.conSombra
import com.apptruco.ui.theme.drawFiletes

private val QuickPoints = listOf(1, 2, 3)
private val CardRadius = 16.dp
private val CardShape = RoundedCornerShape(CardRadius)

@Composable
fun TeamPanel(
    state: TeamUiState,
    config: GameConfig,
    onPoints: (Int) -> Unit,
    onMakeMano: () -> Unit,
    onSing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tonos = state.team.tonos()
    val mano by animateFloatAsState(targetValue = if (state.isMano) 1f else 0f, label = "mano")
    Box(modifier = modifier.carta(team = state.team, marco = tonos.base, mano = mano)) {
        VolutasEnEsquinas(size = 40.dp, inset = 9.dp, abajo = false)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, top = 16.dp, end = 14.dp, bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TeamHeader(team = state.team, isMano = state.isMano, onMakeMano = onMakeMano)
            TallyMarks(
                points = state.points,
                config = config,
                accent = tonos.base,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
            StandingCaption(standing = state.standing, hasBuenas = config.hasBuenas)
            PointControls(
                tonos = tonos,
                canSing = state.cantos.isNotEmpty(),
                onPoints = onPoints,
                onSing = onSing,
            )
        }
    }
}

/**
 * El naipe: fondo crema y doble marco (color del equipo y oro) cortado por la pinta. Cuando el
 * equipo es mano, [mano] va a 1 y la carta toma un filo de oro con resplandor.
 */
private fun Modifier.carta(team: Team, marco: Color, mano: Float): Modifier = this
    .then(
        if (mano > 0f) {
            Modifier.dropShadow(CardShape, Shadow(radius = 26.dp, color = Oro, alpha = 0.55f * mano))
        } else {
            Modifier
        },
    )
    .drawBehind {
        val radius = CardRadius.toPx()
        if (mano > 0f) {
            val ring = 3.dp.toPx()
            drawRoundRect(
                color = Oro.copy(alpha = mano),
                topLeft = Offset(-ring / 2f, -ring / 2f),
                size = Size(size.width + ring, size.height + ring),
                cornerRadius = CornerRadius(radius + ring / 2f),
                style = Stroke(width = ring),
            )
        }
        val fondo = Brush.verticalGradient(listOf(CartaArriba, CartaAbajo))
        drawRoundRect(fondo, cornerRadius = CornerRadius(radius))
        // Radios del mockup: cada línea redondea con 11 y 8 dp en su borde de afuera.
        drawFiletes(radius = 17.dp, filetes = listOf(Filete(6.dp, 2.dp, marco)))
        drawFiletes(radius = 18.dp, filetes = listOf(Filete(10.dp, 1.dp, Oro)))
        val corte = Size(9.dp.toPx(), 7.dp.toPx())
        team.cortes().forEach { dx ->
            val left = size.width / 2f - corte.width / 2f + dx.toPx()
            drawRect(fondo, topLeft = Offset(left, 4.dp.toPx()), size = corte)
            drawRect(fondo, topLeft = Offset(left, size.height - 4.dp.toPx() - corte.height), size = corte)
        }
    }

/** La pinta: el marco del naipe se corta una vez en copas y dos en espadas, arriba y abajo. */
private fun Team.cortes(): List<Dp> = when (this) {
    Team.NOSOTROS -> listOf(0.dp)
    Team.ELLOS -> listOf((-9).dp, 9.dp)
}

/** Copas para Nosotros, espadas para Ellos. */
@DrawableRes
private fun Team.palo(): Int = when (this) {
    Team.NOSOTROS -> R.drawable.palo_copa
    Team.ELLOS -> R.drawable.palo_espada
}

@Composable
private fun TeamHeader(
    team: Team,
    isMano: Boolean,
    onMakeMano: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val name = team.label()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                enabled = !isMano,
                onClickLabel = stringResource(R.string.make_mano, name),
                onClick = onMakeMano,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TeamRibbon(name = name, tonos = team.tonos())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Palo(team = team, modifier = Modifier.padding(start = 2.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (isMano) ManoBadge()
            }
            Palo(team = team, modifier = Modifier.padding(end = 2.dp), espejo = true)
        }
    }
}

/** El nombre del equipo en una cinta con las puntas en cola de golondrina. */
@Composable
private fun TeamRibbon(
    name: String,
    tonos: Tonos,
    modifier: Modifier = Modifier,
) {
    Text(
        text = name,
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(0f to tonos.claro, 0.4f to tonos.base, 1f to tonos.oscuro),
                shape = CintaEquipoShape,
            )
            .padding(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 5.dp),
        style = MaterialTheme.typography.titleSmall.conSombra(tonos.oscuro),
        color = CremaPintada,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun Palo(
    team: Team,
    modifier: Modifier = Modifier,
    espejo: Boolean = false,
) {
    Image(
        painter = painterResource(team.palo()),
        contentDescription = null,
        modifier = modifier
            .size(22.dp)
            .graphicsLayer { scaleX = if (espejo) -1f else 1f },
    )
}

@Composable
private fun ManoBadge(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.mano),
        modifier = modifier
            .dropShadow(
                CircleShape,
                Shadow(radius = 3.dp, color = Color.Black, offset = DpOffset(0.dp, 2.dp), alpha = 0.3f),
            )
            .background(Brush.verticalGradient(0f to OroClaro, 0.6f to Oro, 1f to OroOscuro), CircleShape)
            .border(1.dp, OroContorno, CircleShape)
            .padding(start = 12.dp, top = 1.dp, end = 12.dp, bottom = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        color = OroTinta,
    )
}

@Composable
private fun StandingCaption(
    standing: Standing,
    hasBuenas: Boolean,
    modifier: Modifier = Modifier,
) {
    val text = when {
        !hasBuenas -> pluralStringResource(R.plurals.puntos, standing.malas, standing.malas)
        standing.inBuenas -> pluralStringResource(R.plurals.buenas, standing.buenas, standing.buenas)
        else -> pluralStringResource(R.plurals.malas, standing.malas, standing.malas)
    }
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = Tinta,
        maxLines = 1,
    )
}

@Composable
private fun PointControls(
    tonos: Tonos,
    canSing: Boolean,
    onPoints: (Int) -> Unit,
    onSing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickPoints.forEach { points ->
                BotonCrema(
                    onClick = { onPoints(points) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    conOro = true,
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(stringResource(R.string.plus_points, points), style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BotonContorno(
                onClick = { onPoints(-1) },
                color = LineaGrisClara,
                contentColor = Tinta,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(stringResource(R.string.minus_points, 1), style = MaterialTheme.typography.titleLarge)
            }
            BotonPintado(
                onClick = onSing,
                tonos = tonos,
                enabled = canSing,
                modifier = Modifier
                    .weight(2f)
                    .height(52.dp),
            ) {
                Text(
                    text = stringResource(R.string.sing),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp).conSombra(),
                    maxLines = 1,
                )
            }
        }
    }
}
