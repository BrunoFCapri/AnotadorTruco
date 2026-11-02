package com.apptruco.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apptruco.R
import com.apptruco.domain.Session
import com.apptruco.domain.model.EnvidoCall
import com.apptruco.domain.model.GameAction
import com.apptruco.domain.model.GameConfig
import com.apptruco.domain.model.Prompt
import com.apptruco.domain.model.Team
import com.apptruco.ui.performance.PerformanceScreen
import com.apptruco.ui.setup.SetupScreen
import com.apptruco.ui.theme.TrucoTheme

private val RestingAssistantEstimate = 140.dp

@Composable
fun GameRoute(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel(factory = GameViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when (val screen = state) {
        GameScreenState.Loading -> Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        )
        is GameScreenState.Setup -> {
            BackHandler(enabled = screen.canCancel, onBack = viewModel::onBackToGame)
            SetupScreen(
                initialConfig = screen.config,
                initialMano = screen.mano,
                canCancel = screen.canCancel,
                onStart = viewModel::onStart,
                onCancel = viewModel::onBackToGame,
                modifier = modifier,
            )
        }
        is GameScreenState.Playing -> GameScreen(
            state = screen.game,
            onAction = viewModel::onAction,
            onUndo = viewModel::onUndo,
            onNewGame = viewModel::onOpenSetup,
            onRematch = viewModel::onRematch,
            onShowPerformance = viewModel::onOpenPerformance,
            modifier = modifier,
        )
        is GameScreenState.Performance -> {
            BackHandler(onBack = viewModel::onBackToGame)
            PerformanceScreen(
                state = screen.performance,
                onBack = viewModel::onBackToGame,
                onRematch = viewModel::onRematch,
                modifier = modifier,
            )
        }
    }
}

@Composable
fun GameScreen(
    state: GameUiState,
    onAction: (GameAction) -> Unit,
    onUndo: () -> Unit,
    onNewGame: () -> Unit,
    onRematch: () -> Unit,
    onShowPerformance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var singer by rememberSaveable { mutableStateOf<Team?>(null) }
    var restingAssistantHeight by remember { mutableStateOf<Dp?>(null) }
    val density = LocalDensity.current

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GameHeader(config = state.config, canUndo = state.canUndo, onUndo = onUndo, onNewGame = onNewGame)
            state.picaPicaDuel?.let { duel -> PicaPicaBanner(duel = duel, duels = state.config.duels) }
            // El asistente crece tapando los botones de puntos: las casitas nunca cambian de tamaño.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = (restingAssistantHeight ?: RestingAssistantEstimate) + 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    state.teams.forEach { team ->
                        TeamPanel(
                            state = team,
                            config = state.config,
                            onPoints = { points -> onAction(GameAction.AddPoints(team.team, points)) },
                            onMakeMano = { onAction(GameAction.PassMano) },
                            onSing = { singer = team.team },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }
                CantoAssistant(
                    prompt = state.prompt,
                    mano = state.mano,
                    onAction = onAction,
                    onRematch = onRematch,
                    onNewGame = onNewGame,
                    onShowPerformance = onShowPerformance,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .onSizeChanged { size ->
                            if (state.prompt is Prompt.HandWinner) {
                                val height = with(density) { size.height.toDp() }
                                restingAssistantHeight = restingAssistantHeight?.let { minOf(it, height) } ?: height
                            }
                        },
                )
            }
        }
    }

    singer?.let { team ->
        CantoSheet(
            team = team,
            cantos = state.teams.first { it.team == team }.cantos,
            onSing = { canto -> onAction(GameAction.Sing(team, canto)) },
            onDismiss = { singer = null },
        )
    }
}

@Composable
private fun GameHeader(
    config: GameConfig,
    canUndo: Boolean,
    onUndo: () -> Unit,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val details = listOfNotNull(
        stringResource(if (config.withFlor) R.string.con_flor else R.string.sin_flor),
        pluralStringResource(R.plurals.jugadores, config.players, config.players),
        if (config.playsPicaPica) stringResource(R.string.pica_pica) else null,
    ).joinToString(" · ")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.header_target, config.target), style = MaterialTheme.typography.headlineMedium)
            Text(
                text = details,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        IconButton(onClick = onNewGame, modifier = Modifier.size(56.dp)) {
            Icon(painterResource(R.drawable.ic_new_game), contentDescription = stringResource(R.string.new_game))
        }
        FilledTonalButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.height(52.dp)) {
            Icon(painterResource(R.drawable.ic_undo), contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.undo), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun PicaPicaBanner(
    duel: Int,
    duels: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondary,
    ) {
        Text(
            text = stringResource(R.string.pica_pica_banner, duel, duels),
            modifier = Modifier.padding(vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(widthDp = 411, heightDp = 860)
@Composable
private fun GameScreenPreview() {
    val session = Session.start(GameConfig(withFlor = true, players = 4), Team.NOSOTROS)
        .apply(GameAction.AddPoints(Team.NOSOTROS, 18))
        .apply(GameAction.AddPoints(Team.ELLOS, 7))
        .apply(GameAction.Sing(Team.ELLOS, EnvidoCall.ENVIDO))
    TrucoTheme {
        GameScreen(
            state = session.toUiState(),
            onAction = {},
            onUndo = {},
            onNewGame = {},
            onRematch = {},
            onShowPerformance = {},
        )
    }
}
