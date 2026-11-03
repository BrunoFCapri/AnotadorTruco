package com.apptruco.ui.performance

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apptruco.R
import com.apptruco.domain.Session
import com.apptruco.domain.model.EnvidoCall
import com.apptruco.domain.model.GameAction
import com.apptruco.domain.model.GameConfig
import com.apptruco.domain.model.PointSource
import com.apptruco.domain.model.Score
import com.apptruco.domain.model.Team
import com.apptruco.domain.model.TrucoCall
import com.apptruco.ui.game.accent
import com.apptruco.ui.game.label
import com.apptruco.ui.game.seriesColor
import com.apptruco.ui.theme.BotonContorno
import com.apptruco.ui.theme.BotonFilete
import com.apptruco.ui.theme.BotonPintado
import com.apptruco.ui.theme.CartaAbajo
import com.apptruco.ui.theme.CartaArriba
import com.apptruco.ui.theme.Cartel
import com.apptruco.ui.theme.Crema
import com.apptruco.ui.theme.Filete
import com.apptruco.ui.theme.LetraFileteada
import com.apptruco.ui.theme.MarcoOroRojo
import com.apptruco.ui.theme.Oro
import com.apptruco.ui.theme.OroOscuro
import com.apptruco.ui.theme.Pintura
import com.apptruco.ui.theme.SobreLaCarta
import com.apptruco.ui.theme.TonosRojo
import com.apptruco.ui.theme.TrucoTheme
import com.apptruco.ui.theme.conSombra
import com.apptruco.ui.theme.filetes
import com.apptruco.ui.theme.pantallaFileteada

@Composable
fun PerformanceScreen(
    state: PerformanceUiState,
    onBack: () -> Unit,
    onRematch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .pantallaFileteada(),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BotonFilete(
                    onClick = onBack,
                    pintura = Pintura(filetes = MarcoOroRojo, radius = 28.dp),
                    contentColor = Oro,
                    modifier = Modifier.size(56.dp),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Icon(painterResource(R.drawable.ic_back), contentDescription = stringResource(R.string.back))
                }
                LetraFileteada(
                    text = stringResource(R.string.performance),
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                )
            }
            Summary(state)
            Naipe {
                Readout(progression = state.performance.progression, selected = selected)
                ScoreChart(
                    progression = state.performance.progression,
                    config = state.config,
                    selected = selected,
                    onSelect = { selected = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                )
                Text(
                    text = stringResource(R.string.performance_chart_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Cartel(modifier = Modifier.fillMaxWidth(), volutasAbajo = false) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LetraFileteada(
                        text = stringResource(R.string.performance_by_source),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp),
                        textAlign = TextAlign.Center,
                    )
                    SourcesTable(state)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonPintado(
                    onClick = onRematch,
                    tonos = TonosRojo,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp),
                ) {
                    Text(
                        text = stringResource(R.string.rematch),
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 21.sp).conSombra(),
                    )
                }
                BotonContorno(
                    onClick = onBack,
                    color = Crema,
                    contentColor = Crema,
                    width = 1.5.dp,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp),
                ) {
                    Text(stringResource(R.string.back), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun Summary(state: PerformanceUiState, modifier: Modifier = Modifier) {
    val first = state.winner ?: Team.NOSOTROS
    val plays = state.performance.progression.lastIndex
    Column(modifier = modifier.padding(horizontal = 8.dp)) {
        state.winner?.let { winner ->
            Text(
                text = stringResource(R.string.game_over, winner.label()),
                style = MaterialTheme.typography.headlineSmall,
                color = winner.accent(),
            )
        }
        Text(
            text = stringResource(
                R.string.performance_summary,
                state.score[first],
                state.score[first.opponent],
                pluralStringResource(R.plurals.jugadas, plays, plays),
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Carta crema con filete de oro: el gráfico se lee sobre el papel, como las casitas. */
@Composable
private fun Naipe(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(CartaArriba, CartaAbajo)), RoundedCornerShape(16.dp))
            .filetes(radius = 17.dp, filetes = listOf(Filete(6.dp, 2.dp, OroOscuro), Filete(10.dp, 1.dp, Oro)))
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SobreLaCarta(content)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Readout(
    progression: List<Score>,
    selected: Int?,
    modifier: Modifier = Modifier,
) {
    val shown = progression[selected ?: progression.lastIndex]
    val title = when (selected) {
        null -> stringResource(R.string.performance_readout_final)
        0 -> stringResource(R.string.performance_readout_start)
        else -> stringResource(R.string.performance_readout_event, selected)
    }
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Team.entries.forEach { team ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                LineKey(team)
                Text(
                    text = stringResource(R.string.performance_readout_team, team.label(), shown[team]),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyLarge.tabular(),
                )
            }
        }
    }
}

@Composable
private fun SourcesTable(state: PerformanceUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row {
            Box(Modifier.weight(1.4f))
            Team.entries.forEach { team ->
                Text(
                    text = team.label(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                )
            }
        }
        state.sources.forEach { source ->
            TableRow(label = source.labelRes()) { team ->
                state.performance.pointsBySource.getValue(team).getValue(source).toString()
            }
        }
        HorizontalDivider(color = OroOscuro)
        TableRow(label = R.string.performance_biggest_lead) { team ->
            val lead = state.performance.biggestLead.getValue(team)
            if (lead > 0) stringResource(R.string.plus_points, lead) else lead.toString()
        }
    }
}

@Composable
private fun TableRow(
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    value: @Composable (Team) -> String,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(label),
            modifier = Modifier.weight(1.4f),
            style = MaterialTheme.typography.bodyLarge,
        )
        Team.entries.forEach { team ->
            Text(
                text = value(team),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium.tabular(),
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun LineKey(team: Team, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 16.dp, height = 4.dp)
            .background(team.seriesColor(), RoundedCornerShape(2.dp)),
    )
}

@StringRes
private fun PointSource.labelRes(): Int = when (this) {
    PointSource.HAND -> R.string.source_hand
    PointSource.ENVIDO -> R.string.source_envido
    PointSource.FLOR -> R.string.source_flor
    PointSource.MANUAL -> R.string.source_manual
}

private fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = "tnum")

@Preview(widthDp = 411, heightDp = 1100)
@Composable
private fun PerformanceScreenPreview() {
    val actions = buildList {
        add(GameAction.Sing(Team.ELLOS, EnvidoCall.REAL_ENVIDO))
        add(GameAction.Answer(accept = true))
        add(GameAction.WinTanto(Team.ELLOS))
        add(GameAction.WinHand(Team.ELLOS))
        repeat(6) { add(GameAction.WinHand(Team.NOSOTROS)) }
        add(GameAction.Sing(Team.NOSOTROS, TrucoCall.TRUCO))
        add(GameAction.Answer(accept = true))
        add(GameAction.WinHand(Team.NOSOTROS))
        repeat(8) { add(GameAction.AddPoints(Team.NOSOTROS, 3)) }
    }
    val game = actions.fold(Session.start(GameConfig(withFlor = true), Team.NOSOTROS)) { session, action ->
        session.apply(action)
    }.game
    TrucoTheme {
        PerformanceScreen(state = game.toPerformanceUiState(), onBack = {}, onRematch = {})
    }
}
