package com.apptruco.ui.setup

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.apptruco.R
import com.apptruco.domain.model.GameConfig
import com.apptruco.domain.model.Team
import com.apptruco.ui.game.label
import com.apptruco.ui.theme.BotonPintado
import com.apptruco.ui.theme.Cartel
import com.apptruco.ui.theme.Cinta
import com.apptruco.ui.theme.Contorno
import com.apptruco.ui.theme.Crema
import com.apptruco.ui.theme.CremaGastada
import com.apptruco.ui.theme.CremaPintada
import com.apptruco.ui.theme.Filete
import com.apptruco.ui.theme.LetraFileteada
import com.apptruco.ui.theme.Luz
import com.apptruco.ui.theme.Negro
import com.apptruco.ui.theme.NegroCartel
import com.apptruco.ui.theme.NegroLuz
import com.apptruco.ui.theme.Oro
import com.apptruco.ui.theme.OroClaro
import com.apptruco.ui.theme.OroQuemado
import com.apptruco.ui.theme.PerillaApagada
import com.apptruco.ui.theme.Rojo
import com.apptruco.ui.theme.Tinta
import com.apptruco.ui.theme.TonosRojo
import com.apptruco.ui.theme.TrucoTheme
import com.apptruco.ui.theme.Verde
import com.apptruco.ui.theme.VerdeClaro
import com.apptruco.ui.theme.conSombra
import com.apptruco.ui.theme.drawFiletes
import com.apptruco.ui.theme.pantallaFileteada
import com.apptruco.ui.theme.pintura
import com.apptruco.ui.theme.pinturaCrema
import com.apptruco.ui.theme.pinturaPintada

private val LuzSetup = Luz(0.5f, 0f, 1.2f, 0.9f, 0f to NegroCartel, 0.7f to Negro)
private val MarcoSetup = listOf(Filete(0.dp, 2.dp, Oro), Filete(5.dp, 1.5.dp, Rojo))
private val SetupPadding = PaddingValues(start = 26.dp, top = 30.dp, end = 26.dp, bottom = 20.dp)

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

    Surface(
        modifier = modifier
            .fillMaxSize()
            .pantallaFileteada(),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        // El cartel ocupa toda la pantalla y, si no entra, se desplaza entero con sus volutas.
        BoxWithConstraints(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(start = 24.dp, top = 6.dp, end = 24.dp, bottom = 16.dp),
        ) {
            val minContentHeight = maxHeight - SetupPadding.calculateTopPadding() -
                SetupPadding.calculateBottomPadding()
            Cartel(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                radius = 24.dp,
                fondo = LuzSetup,
                filetes = MarcoSetup,
                voluta = 54.dp,
                volutaInset = 10.dp,
                contentPadding = SetupPadding,
            ) {
                Column(
                    modifier = Modifier.heightIn(min = minContentHeight),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        LetraFileteada(
                            text = stringResource(R.string.setup_title),
                            style = MaterialTheme.typography.displaySmall,
                            textAlign = TextAlign.Center,
                        )
                        Cinta(text = stringResource(R.string.setup_dicho))
                    }
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
                    Spacer(Modifier.weight(1f))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BotonPintado(
                            onClick = { onStart(config, mano) },
                            tonos = TonosRojo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.setup_start),
                                style = MaterialTheme.typography.headlineSmall.conSombra(),
                            )
                        }
                        if (canCancel) {
                            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(R.string.cancel),
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                                    color = CremaGastada,
                                )
                            }
                        }
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
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 16.sp),
            color = MaterialTheme.colorScheme.secondary,
        )
        content()
    }
}

/** Opciones en fila: la elegida va pintada de rojo con una tilde y las demás en crema. */
@Composable
private fun <T> ChoiceRow(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (T) -> String,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp)
                    .pintura(if (isSelected) pinturaPintada(TonosRojo) else pinturaCrema())
                    .clip(RoundedCornerShape(12.dp))
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(option) }),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isSelected) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = CremaPintada,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) CremaPintada else Tinta,
                    maxLines = 1,
                )
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(title), style = MaterialTheme.typography.labelLarge, color = Crema)
            hint?.let {
                Text(
                    text = stringResource(it),
                    modifier = Modifier.padding(top = 2.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CremaGastada,
                )
            }
        }
        Interruptor(checked = checked, modifier = Modifier.alpha(if (enabled) 1f else 0.4f))
    }
}

/** Interruptor de cartel: prendido es verde con la perilla de oro; apagado, negro con la perilla gris. */
@Composable
private fun Interruptor(checked: Boolean, modifier: Modifier = Modifier) {
    val on by animateFloatAsState(targetValue = if (checked) 1f else 0f, label = "interruptor")
    Canvas(modifier = modifier.size(width = 60.dp, height = 32.dp)) {
        val pill = CornerRadius(size.height / 2f)
        drawRoundRect(NegroLuz, cornerRadius = pill)
        drawRoundRect(Brush.verticalGradient(listOf(VerdeClaro, Verde)), cornerRadius = pill, alpha = on)
        drawFiletes(radius = 16.dp, filetes = listOf(Filete(0.dp, 1.5.dp, Oro)))

        val knob = 11.dp.toPx()
        val center = Offset(lerp(16.dp, 44.dp, on).toPx(), size.height / 2f)
        drawCircle(PerillaApagada, radius = knob, center = center, alpha = 1f - on)
        drawCircle(
            brush = Brush.radialGradient(
                0f to OroClaro,
                0.6f to Oro,
                1f to OroQuemado,
                center = center + Offset(-0.2f * knob, -0.3f * knob),
                radius = 1.75f * knob,
            ),
            radius = knob,
            center = center,
            alpha = on,
        )
        drawCircle(Contorno, radius = knob + 0.5.dp.toPx(), center = center, alpha = on, style = Stroke(1.dp.toPx()))
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
