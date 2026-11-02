package com.apptruco.domain

import com.apptruco.domain.model.Game
import com.apptruco.domain.model.GameAction
import com.apptruco.domain.model.GameConfig
import com.apptruco.domain.model.Team
import com.apptruco.domain.rules.reduce
import kotlinx.serialization.Serializable

/** Partida en curso más los estados anteriores para deshacer. Empezar otra también se deshace. */
@Serializable
data class Session(
    val game: Game,
    val history: List<Game> = emptyList(),
) {
    val canUndo: Boolean get() = history.isNotEmpty()

    fun apply(action: GameAction): Session = advanceTo(game.reduce(action))

    fun restart(config: GameConfig, mano: Team): Session = advanceTo(Game.start(config, mano))

    fun undo(): Session = history.lastOrNull()?.let { Session(it, history.dropLast(1)) } ?: this

    private fun advanceTo(next: Game): Session =
        if (next == game) this else Session(next, (history + game).takeLast(MAX_UNDO))

    companion object {
        const val MAX_UNDO = 100

        fun start(config: GameConfig, mano: Team) = Session(Game.start(config, mano))
    }
}
