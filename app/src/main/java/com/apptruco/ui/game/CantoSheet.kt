package com.apptruco.ui.game

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.apptruco.R
import com.apptruco.domain.model.Canto
import com.apptruco.domain.model.Team
import com.apptruco.domain.model.TrucoCall
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        properties = ModalBottomSheetProperties(
            isAppearanceLightStatusBars = false,
            isAppearanceLightNavigationBars = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(R.string.sing_title, team.label().uppercase()),
                style = MaterialTheme.typography.headlineSmall,
                color = team.accent(),
            )
            if (tanto.isNotEmpty()) CantoGroup(title = R.string.section_tanto, cantos = tanto, onSing = choose)
            if (truco.isNotEmpty()) CantoGroup(title = R.string.section_truco, cantos = truco, onSing = choose)
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
            text = stringResource(title).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CantoButtons(cantos = cantos, onSing = onSing)
    }
}
