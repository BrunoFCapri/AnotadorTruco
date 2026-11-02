package com.apptruco.domain.rules

import com.apptruco.domain.model.EnvidoCall
import com.apptruco.domain.model.FlorCall
import com.apptruco.domain.model.GameConfig
import com.apptruco.domain.model.Score
import com.apptruco.domain.model.Standing
import com.apptruco.domain.model.Team
import com.apptruco.domain.model.TrucoCall

object Scoring {
    const val FLOR = 3
    private const val CONTRAFLOR = 6
    private const val CONTRAFLOR_ACHICADA = 4
    private const val PICA_PICA_CLOSING = 5

    fun envidoQuerido(calls: List<EnvidoCall>, falta: Int): Int =
        if (EnvidoCall.FALTA_ENVIDO in calls) falta else calls.sumOf { it.fixedPoints }

    fun envidoNoQuerido(calls: List<EnvidoCall>): Int =
        calls.dropLast(1).sumOf { it.fixedPoints }.coerceAtLeast(1)

    fun florQuerida(calls: List<FlorCall>, falta: Int): Int = when (calls.last()) {
        FlorCall.FLOR -> FLOR
        FlorCall.CONTRAFLOR -> CONTRAFLOR
        FlorCall.CONTRAFLOR_AL_RESTO -> falta
    }

    fun florNoQuerida(calls: List<FlorCall>): Int = when (calls.last()) {
        FlorCall.FLOR -> FLOR
        FlorCall.CONTRAFLOR -> CONTRAFLOR_ACHICADA
        FlorCall.CONTRAFLOR_AL_RESTO ->
            if (FlorCall.CONTRAFLOR in calls) CONTRAFLOR else CONTRAFLOR_ACHICADA
    }

    fun trucoNoQuerido(calls: List<TrucoCall>): Int = calls.dropLast(1).lastOrNull()?.points ?: 1

    /**
     * Valor de la falta envido y la contraflor al resto. Si los dos están en las malas se juega
     * el partido: el ganador se lleva lo que le falta para terminar. Si alguno ya entró en las
     * buenas, se juega lo que le falta al que va ganando.
     */
    fun falta(score: Score, winner: Team, config: GameConfig): Int =
        if (config.hasBuenas && score.leader <= config.malas) {
            config.target - score[winner]
        } else {
            config.target - score.leader
        }

    fun standing(points: Int, config: GameConfig): Standing = Standing(
        malas = points.coerceAtMost(config.malas),
        buenas = (points - config.malas).coerceAtLeast(0),
    )

    /**
     * En partidas por equipos se juega pica-pica desde que alguien entra en las buenas hasta
     * que alguien queda a cinco de terminar; ahí se vuelve a la redonda para cerrar.
     */
    fun isPicaPicaZone(score: Score, config: GameConfig): Boolean =
        config.playsPicaPica &&
            score.leader > config.malas &&
            score.leader < config.target - PICA_PICA_CLOSING

    private val EnvidoCall.fixedPoints: Int
        get() = when (this) {
            EnvidoCall.ENVIDO -> 2
            EnvidoCall.REAL_ENVIDO -> 3
            EnvidoCall.FALTA_ENVIDO -> 0
        }
}
