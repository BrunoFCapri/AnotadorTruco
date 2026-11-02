package com.apptruco.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.apptruco.R
import com.apptruco.domain.model.GameConfig
import com.apptruco.domain.model.Standing
import com.apptruco.domain.model.Team

private val QuickPoints = listOf(1, 2, 3)

@Composable
fun TeamPanel(
    state: TeamUiState,
    config: GameConfig,
    onPoints: (Int) -> Unit,
    onMakeMano: () -> Unit,
    onSing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = state.team.accent()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = if (state.isMano) BorderStroke(2.dp, accent) else null,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TeamHeader(team = state.team, isMano = state.isMano, onMakeMano = onMakeMano)
            TallyMarks(
                points = state.points,
                config = config,
                color = accent,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
            StandingCaption(standing = state.standing, hasBuenas = config.hasBuenas)
            PointControls(
                team = state.team,
                canSing = state.cantos.isNotEmpty(),
                onPoints = onPoints,
                onSing = onSing,
            )
        }
    }
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
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            color = team.accent(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(modifier = Modifier.height(28.dp), contentAlignment = Alignment.Center) {
            if (isMano) ManoBadge(team)
        }
    }
}

@Composable
private fun ManoBadge(team: Team, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = CircleShape, color = team.accent()) {
        Text(
            text = stringResource(R.string.mano),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelMedium,
            color = team.onAccent(),
        )
    }
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
        maxLines = 1,
    )
}

@Composable
private fun PointControls(
    team: Team,
    canSing: Boolean,
    onPoints: (Int) -> Unit,
    onSing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickPoints.forEach { points ->
                FilledTonalButton(
                    onClick = { onPoints(points) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(stringResource(R.string.plus_points, points), style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { onPoints(-1) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            ) {
                Text(stringResource(R.string.minus_points, 1), style = MaterialTheme.typography.titleLarge)
            }
            Button(
                onClick = onSing,
                enabled = canSing,
                modifier = Modifier
                    .weight(2f)
                    .height(52.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = team.accent(),
                    contentColor = team.onAccent(),
                ),
            ) {
                Text(stringResource(R.string.sing), style = MaterialTheme.typography.labelLarge, maxLines = 1)
            }
        }
    }
}
