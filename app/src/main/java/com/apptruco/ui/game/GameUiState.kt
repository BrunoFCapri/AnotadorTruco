package com.apptruco.ui.game

import com.apptruco.domain.Session
import com.apptruco.domain.model.Canto
import com.apptruco.domain.model.GameConfig
import com.apptruco.domain.model.Prompt
import com.apptruco.domain.model.Standing
import com.apptruco.domain.model.Team
import com.apptruco.domain.rules.Scoring
import com.apptruco.domain.rules.cantosFor
import com.apptruco.domain.rules.prompt
import com.apptruco.ui.performance.PerformanceUiState

sealed interface GameScreenState {
    data object Loading : GameScreenState

    data class Setup(
        val config: GameConfig,
        val mano: Team,
        val canCancel: Boolean,
    ) : GameScreenState

    data class Playing(val game: GameUiState) : GameScreenState

    data class Performance(val performance: PerformanceUiState) : GameScreenState
}

data class GameUiState(
    val config: GameConfig,
    val teams: List<TeamUiState>,
    val mano: Team,
    val prompt: Prompt,
    val picaPicaDuel: Int?,
    val canUndo: Boolean,
)

data class TeamUiState(
    val team: Team,
    val points: Int,
    val standing: Standing,
    val isMano: Boolean,
    val cantos: List<Canto>,
)

fun Session.toUiState(): GameUiState {
    val prompt = game.prompt()
    return GameUiState(
        config = game.config,
        teams = Team.entries.map { team ->
            val points = game.score[team]
            TeamUiState(
                team = team,
                points = points,
                standing = Scoring.standing(points, game.config),
                isMano = team == game.hand.mano,
                cantos = if (prompt is Prompt.HandWinner) game.cantosFor(team) else emptyList(),
            )
        },
        mano = game.hand.mano,
        prompt = prompt,
        picaPicaDuel = game.hand.duel.takeIf { game.hand.picaPica },
        canUndo = canUndo,
    )
}
