package com.apptruco.ui.setup

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apptruco.R
import com.apptruco.domain.model.GameConfig
import com.apptruco.domain.model.Team
import com.apptruco.ui.game.label
import com.apptruco.ui.theme.TrucoTheme

@Composable
fun SetupScreen(
    initialConfig: GameConfig,
    initialMano: Team,
    canCancel: Boolean,
    onStart: (GameConfig, Team) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var target by rememberSaveable { mutableIntStateOf(initialConfig.target) }
    var players by rememberSaveable { mutableIntStateOf(initialConfig.players) }
    var withFlor by rememberSaveable { mutableStateOf(initialConfig.withFlor) }
    var picaPica by rememberSaveable { mutableStateOf(initialConfig.picaPica) }
    var mano by rememberSaveable { mutableStateOf(initialMano) }

    val config = GameConfig(target = target, withFlor = withFlor, players = players, picaPica = picaPica)
    val picaPicaAvailable = config.copy(picaPica = true).playsPicaPica

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Text(stringResource(R.string.setup_title), style = MaterialTheme.typography.displaySmall)
            SetupSection(title = R.string.setup_target) {
                ChoiceRow(options = GameConfig.TARGETS, selected = target, onSelect = { target = it }) {
                    stringResource(R.string.header_target, it)
                }
            }
            SetupSection(title = R.string.setup_players) {
                ChoiceRow(
                    options = GameConfig.PLAYERS,
                    selected = players,
                    onSelect = {
                        players = it
                        if (it == 6) picaPica = true
                    },
                ) { it.toString() }
            }
            SwitchRow(title = R.string.setup_flor, checked = withFlor, onCheckedChange = { withFlor = it })
            SwitchRow(
                title = R.string.setup_pica_pica,
                hint = R.string.setup_pica_pica_hint,
                checked = picaPica && picaPicaAvailable,
                enabled = picaPicaAvailable,
                onCheckedChange = { picaPica = it },
            )
            SetupSection(title = R.string.setup_mano) {
                ChoiceRow(options = Team.entries, selected = mano, onSelect = { mano = it }) { it.label() }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onStart(config, mano) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                ) {
                    Text(stringResource(R.string.setup_start), style = MaterialTheme.typography.titleLarge)
                }
                if (canCancel) {
                    TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.cancel), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupSection(
    @StringRes title: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ChoiceRow(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (T) -> String,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                modifier = Modifier.heightIn(min = 56.dp),
            ) {
                Text(label(option), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun SwitchRow(
    @StringRes title: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes hint: Int? = null,
    enabled: Boolean = true,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
            hint?.let {
                Text(
                    text = stringResource(it),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Preview(widthDp = 411, heightDp = 860)
@Composable
private fun SetupScreenPreview() {
    TrucoTheme {
        SetupScreen(
            initialConfig = GameConfig(players = 6, picaPica = true),
            initialMano = Team.NOSOTROS,
            canCancel = true,
            onStart = { _, _ -> },
            onCancel = {},
        )
    }
}
