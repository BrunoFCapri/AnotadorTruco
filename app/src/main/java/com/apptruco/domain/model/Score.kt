package com.apptruco.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Score(
    val nosotros: Int = 0,
    val ellos: Int = 0,
) {
    operator fun get(team: Team): Int = when (team) {
        Team.NOSOTROS -> nosotros
        Team.ELLOS -> ellos
    }

    val leader: Int get() = maxOf(nosotros, ellos)

    fun plus(team: Team, points: Int, cap: Int): Score {
        val updated = (this[team] + points).coerceIn(0, cap)
        return when (team) {
            Team.NOSOTROS -> copy(nosotros = updated)
            Team.ELLOS -> copy(ellos = updated)
        }
    }
}

@Serializable
enum class PointSource { HAND, ENVIDO, FLOR, MANUAL }

/** Puntos que efectivamente se anotaron (ya recortados al objetivo), en orden. */
@Serializable
data class ScoreEvent(
    val team: Team,
    val points: Int,
    val source: PointSource,
)

data class Standing(
    val malas: Int,
    val buenas: Int,
) {
    val inBuenas: Boolean get() = buenas > 0
}
