package com.apptruco.ui.game

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.apptruco.R
import com.apptruco.domain.model.Canto
import com.apptruco.domain.model.EnvidoCall
import com.apptruco.domain.model.FlorCall
import com.apptruco.domain.model.Team
import com.apptruco.domain.model.TrucoCall
import com.apptruco.ui.theme.Tonos
import com.apptruco.ui.theme.TonosAzul
import com.apptruco.ui.theme.TonosRojo

@StringRes
fun Team.labelRes(): Int = when (this) {
    Team.NOSOTROS -> R.string.team_nosotros
    Team.ELLOS -> R.string.team_ellos
}

@Composable
fun Team.label(): String = stringResource(labelRes())

@Composable
@ReadOnlyComposable
fun Team.accent(): Color = when (this) {
    Team.NOSOTROS -> MaterialTheme.colorScheme.primary
    Team.ELLOS -> MaterialTheme.colorScheme.tertiary
}

/** Tonos pintados del equipo, para cintas, botones y todo lo que va sobre la carta crema. */
fun Team.tonos(): Tonos = when (this) {
    Team.NOSOTROS -> TonosRojo
    Team.ELLOS -> TonosAzul
}

/** Tono base del equipo, el de las series del gráfico. */
@Composable
@ReadOnlyComposable
fun Team.seriesColor(): Color = when (this) {
    Team.NOSOTROS -> MaterialTheme.colorScheme.primaryContainer
    Team.ELLOS -> MaterialTheme.colorScheme.tertiaryContainer
}

@StringRes
fun Canto.labelRes(): Int = when (this) {
    is EnvidoCall -> when (this) {
        EnvidoCall.ENVIDO -> R.string.canto_envido
        EnvidoCall.REAL_ENVIDO -> R.string.canto_real_envido
        EnvidoCall.FALTA_ENVIDO -> R.string.canto_falta_envido
    }
    is FlorCall -> when (this) {
        FlorCall.FLOR -> R.string.canto_flor
        FlorCall.CONTRAFLOR -> R.string.canto_contraflor
        FlorCall.CONTRAFLOR_AL_RESTO -> R.string.canto_contraflor_al_resto
    }
    is TrucoCall -> when (this) {
        TrucoCall.TRUCO -> R.string.canto_truco
        TrucoCall.RETRUCO -> R.string.canto_retruco
        TrucoCall.VALE_CUATRO -> R.string.canto_vale_cuatro
    }
}

@Composable
fun List<Canto>.chainLabel(): String = map { stringResource(it.labelRes()) }.joinToString(" · ")
