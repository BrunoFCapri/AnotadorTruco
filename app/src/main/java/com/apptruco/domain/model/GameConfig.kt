package com.apptruco.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameConfig(
    val target: Int = 30,
    val withFlor: Boolean = false,
    val players: Int = 2,
    val picaPica: Boolean = false,
) {
    init {
        require(target in TARGETS) { "Partida a $target no soportada" }
        require(players in PLAYERS) { "Cantidad de jugadores inválida: $players" }
    }

    val hasBuenas: Boolean get() = target == 30

    /** Puntos que forman las malas; en una partida a 15 no hay buenas y todo cuenta como malas. */
    val malas: Int get() = if (hasBuenas) target / 2 else target

    val playsPicaPica: Boolean get() = picaPica && hasBuenas && players > 2

    val duels: Int get() = players / 2

    companion object {
        val TARGETS = listOf(15, 30)
        val PLAYERS = listOf(2, 4, 6)
    }
}
