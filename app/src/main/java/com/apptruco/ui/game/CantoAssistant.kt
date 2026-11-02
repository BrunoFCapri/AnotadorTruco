package com.apptruco.ui.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apptruco.R
import com.apptruco.domain.model.Canto
import com.apptruco.domain.model.FlorCall
import com.apptruco.domain.model.GameAction
import com.apptruco.domain.model.Prompt
import com.apptruco.domain.model.Team

private val BigButtonHeight = 64.dp

@Composable
fun CantoAssistant(
    prompt: Prompt,
    mano: Team,
    onAction: (GameAction) -> Unit,
    onRematch: () -> Unit,
    onNewGame: () -> Unit,
    onShowPerformance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val onCancel = { onAction(GameAction.CancelCanto) }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 12.dp,
    ) {
        AnimatedContent(
            targetState = prompt,
            modifier = Modifier.padding(16.dp),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentKey = { it::class },
            label = "prompt",
        ) { current ->
            when (current) {
                is Prompt.HandWinner -> HandWinnerPrompt(
                    prompt = current,
                    onWinner = { onAction(GameAction.WinHand(it)) },
                )
                is Prompt.Answer -> AnswerPrompt(
                    prompt = current,
                    onAnswer = { onAction(GameAction.Answer(it)) },
                    onRaise = { onAction(GameAction.Sing(current.responder, it)) },
                    onCancel = onCancel,
                )
                is Prompt.FlorSung -> FlorPrompt(
                    prompt = current,
                    onAccept = { onAction(GameAction.Answer(accept = true)) },
                    onRaise = { onAction(GameAction.Sing(current.responder, it)) },
                    onCancel = onCancel,
                )
                is Prompt.TantoWinner -> TantoWinnerPrompt(
                    prompt = current,
                    mano = mano,
                    onWinner = { onAction(GameAction.WinTanto(it)) },
                )
                is Prompt.GameOver -> GameOverPrompt(
                    prompt = current,
                    onRematch = onRematch,
                    onNewGame = onNewGame,
                    onShowPerformance = onShowPerformance,
                )
            }
        }
    }
}

@Composable
private fun HandWinnerPrompt(
    prompt: Prompt.HandWinner,
    onWinner: (Team) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.prompt_hand_winner),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            prompt.truco?.let { truco ->
                Text(
                    text = stringResource(R.string.prompt_querido, stringResource(truco.labelRes())),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
        TeamChoice(points = { prompt.points }, onPick = onWinner)
    }
}

@Composable
private fun AnswerPrompt(
    prompt: Prompt.Answer,
    onAnswer: (Boolean) -> Unit,
    onRaise: (Canto) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rejectLabel = if (prompt.calls.last() is FlorCall) R.string.me_achico else R.string.no_quiero
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SungHeader(caller = prompt.caller, calls = prompt.calls, onCancel = onCancel)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { onAnswer(true) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = BigButtonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = prompt.responder.accent(),
                    contentColor = prompt.responder.onAccent(),
                ),
            ) {
                Text(stringResource(R.string.quiero), style = MaterialTheme.typography.titleMedium)
            }
            OutlinedButton(
                onClick = { onAnswer(false) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = BigButtonHeight),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(
                    text = stringResource(rejectLabel, prompt.rejectPoints),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (prompt.raises.isNotEmpty()) CantoButtons(cantos = prompt.raises, onSing = onRaise)
    }
}

@Composable
private fun FlorPrompt(
    prompt: Prompt.FlorSung,
    onAccept: () -> Unit,
    onRaise: (Canto) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SungHeader(caller = prompt.caller, calls = listOf(FlorCall.FLOR), onCancel = onCancel)
        Button(
            onClick = onAccept,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = BigButtonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = prompt.caller.accent(),
                contentColor = prompt.caller.onAccent(),
            ),
        ) {
            Text(stringResource(R.string.anotar_flor, prompt.points), style = MaterialTheme.typography.titleMedium)
        }
        if (prompt.raises.isNotEmpty()) CantoButtons(cantos = prompt.raises, onSing = onRaise)
    }
}

@Composable
private fun SungHeader(
    caller: Team,
    calls: List<Canto>,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.prompt_sung, caller.label()).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = caller.accent(),
            )
            Text(text = calls.chainLabel(), style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(R.string.prompt_answer, caller.opponent.label()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.heightIn(min = 48.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        ) {
            Icon(painterResource(R.drawable.ic_close), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.cancel_canto), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun TantoWinnerPrompt(
    prompt: Prompt.TantoWinner,
    mano: Team,
    onWinner: (Team) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text(text = prompt.calls.chainLabel(), style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(R.string.prompt_tanto_winner),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TeamChoice(points = prompt.prizes::getValue, onPick = onWinner, mano = mano)
    }
}

@Composable
private fun GameOverPrompt(
    prompt: Prompt.GameOver,
    onRematch: () -> Unit,
    onNewGame: () -> Unit,
    onShowPerformance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val winner = prompt.winner
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.game_over, winner.label().uppercase()),
            style = MaterialTheme.typography.headlineMedium,
            color = winner.accent(),
        )
        Text(
            text = stringResource(R.string.final_score, prompt.score[winner], prompt.score[winner.opponent]),
            style = MaterialTheme.typography.titleLarge,
        )
        FilledTonalButton(
            onClick = onShowPerformance,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = BigButtonHeight),
        ) {
            Icon(painterResource(R.drawable.ic_chart), contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.performance), style = MaterialTheme.typography.titleMedium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onRematch,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = BigButtonHeight),
            ) {
                Text(stringResource(R.string.rematch), style = MaterialTheme.typography.titleMedium)
            }
            OutlinedButton(
                onClick = onNewGame,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = BigButtonHeight),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            ) {
                Text(
                    text = stringResource(R.string.new_game),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun TeamChoice(
    points: (Team) -> Int,
    onPick: (Team) -> Unit,
    modifier: Modifier = Modifier,
    mano: Team? = null,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Team.entries.forEach { team ->
            Button(
                onClick = { onPick(team) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = BigButtonHeight),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = team.accent(),
                    contentColor = team.onAccent(),
                ),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = team.label().uppercase(), style = MaterialTheme.typography.labelLarge, maxLines = 1)
                    Text(
                        text = stringResource(
                            if (team == mano) R.string.plus_points_mano else R.string.plus_points,
                            points(team),
                        ),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CantoButtons(
    cantos: List<Canto>,
    onSing: (Canto) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 2,
    ) {
        cantos.forEach { canto ->
            FilledTonalButton(
                onClick = { onSing(canto) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp),
            ) {
                Text(
                    text = stringResource(canto.labelRes()),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
