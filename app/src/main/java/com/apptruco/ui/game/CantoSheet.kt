package com.apptruco.ui.game

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.apptruco.R
import com.apptruco.domain.model.Canto
import com.apptruco.domain.model.Team
import com.apptruco.domain.model.TrucoCall
import com.apptruco.ui.theme.Cartel
import com.apptruco.ui.theme.Crema
import com.apptruco.ui.theme.LetraFileteada
import com.apptruco.ui.theme.Oro
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CantoSheet(
    team: Team,
    cantos: List<Canto>,
    onSing: (Canto) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val choose: (Canto) -> Unit = { canto ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onSing(canto)
            onDismiss()
        }
    }
    val (truco, tanto) = cantos.partition { it is TrucoCall }

    // La hoja es transparente: lo que se ve es el cartel, que flota como el asistente.
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        contentColor = Crema,
        tonalElevation = 0.dp,
        dragHandle = { BottomSheetDefaults.DragHandle(modifier = Modifier.offset(y = 14.dp), color = Oro) },
        contentWindowInsets = { WindowInsets(0) },
        properties = ModalBottomSheetProperties(
            isAppearanceLightStatusBars = false,
            isAppearanceLightNavigationBars = false,
        ),
    ) {
        Cartel(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
            contentPadding = PaddingValues(start = 18.dp, top = 22.dp, end = 18.dp, bottom = 20.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                LetraFileteada(
                    text = stringResource(R.string.sing_title, team.label()),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 22.dp),
                )
                if (tanto.isNotEmpty()) CantoGroup(title = R.string.section_tanto, cantos = tanto, onSing = choose)
                if (truco.isNotEmpty()) CantoGroup(title = R.string.section_truco, cantos = truco, onSing = choose)
            }
        }
    }
}

@Composable
private fun CantoGroup(
    @StringRes title: Int,
    cantos: List<Canto>,
    onSing: (Canto) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(title),
            modifier = Modifier.padding(horizontal = 14.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        CantoButtons(cantos = cantos, onSing = onSing)
    }
}
