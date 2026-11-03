package com.apptruco.ui.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.apptruco.R
import com.apptruco.domain.model.Canto
import com.apptruco.domain.model.FlorCall
import com.apptruco.domain.model.GameAction
import com.apptruco.domain.model.Prompt
import com.apptruco.domain.model.Team
import com.apptruco.ui.theme.BotonContorno
import com.apptruco.ui.theme.BotonNegro
import com.apptruco.ui.theme.BotonPintado
import com.apptruco.ui.theme.Cartel
import com.apptruco.ui.theme.Crema
import com.apptruco.ui.theme.LetraFileteada
import com.apptruco.ui.theme.LineaGris
import com.apptruco.ui.theme.TonosRojo
import com.apptruco.ui.theme.conSombra

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
    // La sombra hacia arriba apaga las cartas cuando el cartel crece y las tapa.
    Cartel(
        modifier = modifier.dropShadow(
            RoundedCornerShape(20.dp),
            Shadow(radius = 30.dp, color = Color.Black, offset = DpOffset(0.dp, (-12).dp), alpha = 0.8f),
        ),
    ) {
        AnimatedContent(
            targetState = prompt,
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
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LetraFileteada(
                text = stringResource(R.string.prompt_hand_winner),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                modifier = Modifier.weight(1f),
            )
            prompt.truco?.let { truco ->
                Text(
                    text = stringResource(R.string.prompt_querido, stringResource(truco.labelRes())),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
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
            BotonPintado(
                onClick = { onAnswer(true) },
                tonos = prompt.responder.tonos(),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = BigButtonHeight),
            ) {
                Text(
                    text = stringResource(R.string.quiero),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 21.sp).conSombra(),
                )
            }
            BotonContorno(
                onClick = { onAnswer(false) },
                color = Crema,
                contentColor = Crema,
                width = 1.5.dp,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = BigButtonHeight),
            ) {
                Text(
                    text = stringResource(rejectLabel, prompt.rejectPoints),
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.sp),
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
        BotonPintado(
            onClick = onAccept,
            tonos = prompt.caller.tonos(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = BigButtonHeight),
        ) {
            Text(
                text = stringResource(R.string.anotar_flor, prompt.points),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 21.sp).conSombra(),
            )
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
    Row(modifier = modifier.padding(start = 14.dp, end = 6.dp), verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.prompt_sung, caller.label()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            // Un canto solo entra grande; la cadena entera se achica hasta caber en dos renglones.
            LetraFileteada(
                text = stringResource(R.string.prompt_call, calls.chainLabel()),
                style = MaterialTheme.typography.headlineLarge.copy(lineHeight = 1.15.em),
                maxLines = 2,
                autoSize = TextAutoSize.StepBased(minFontSize = 24.sp, maxFontSize = 42.sp, stepSize = 2.sp),
            )
            Text(
                text = stringResource(R.string.prompt_answer, caller.opponent.label()),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        BotonContorno(
            onClick = onCancel,
            color = LineaGris,
            contentColor = Crema,
            modifier = Modifier.heightIn(min = 48.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
        ) {
            Icon(painterResource(R.drawable.ic_close), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.cancel_canto),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp, letterSpacing = 0.sp),
            )
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
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            LetraFileteada(
                text = prompt.calls.chainLabel(),
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
            )
            Text(
                text = stringResource(R.string.prompt_tanto_winner),
                style = MaterialTheme.typography.bodyLarge,
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
        LetraFileteada(
            text = stringResource(R.string.game_over, winner.label()),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        Text(
            text = stringResource(R.string.final_score, prompt.score[winner], prompt.score[winner.opponent]),
            style = MaterialTheme.typography.titleLarge,
            color = Crema,
        )
        BotonNegro(
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
            BotonPintado(
                onClick = onRematch,
                tonos = TonosRojo,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = BigButtonHeight),
            ) {
                Text(
                    text = stringResource(R.string.rematch),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 21.sp).conSombra(),
                )
            }
            BotonContorno(
                onClick = onNewGame,
                color = Crema,
                contentColor = Crema,
                width = 1.5.dp,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = BigButtonHeight),
            ) {
                Text(
                    text = stringResource(R.string.new_game),
                    style = MaterialTheme.typography.labelLarge,
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
            BotonPintado(
                onClick = { onPick(team) },
                tonos = team.tonos(),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = BigButtonHeight),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = team.label(),
                        style = MaterialTheme.typography.labelMedium.conSombra(),
                        maxLines = 1,
                    )
                    Text(
                        text = stringResource(
                            if (team == mano) R.string.plus_points_mano else R.string.plus_points,
                            points(team),
                        ),
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp, lineHeight = 28.sp)
                            .conSombra(),
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
            BotonNegro(
                onClick = { onSing(canto) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp),
            ) {
                Text(
                    text = stringResource(canto.labelRes()),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
