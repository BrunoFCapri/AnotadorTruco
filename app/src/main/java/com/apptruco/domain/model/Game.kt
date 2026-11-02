package com.apptruco.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Hand(
    val mano: Team,
    val picaPica: Boolean = false,
    val duel: Int = 1,
    val envido: Bet<EnvidoCall>? = null,
    val flor: Bet<FlorCall>? = null,
    val truco: Bet<TrucoCall>? = null,
    val tantoPlayed: Boolean = false,
) {
    val trucoPoints: Int get() = truco?.takeIf { it.accepted }?.last?.points ?: 1

    val isIdle: Boolean get() = envido == null && flor == null && truco?.isPending != true

    /** La flor tapa al envido que se estaba cantando; si se cancela la flor, el envido vuelve. */
    val activeEnvido: Bet<EnvidoCall>? get() = envido.takeIf { flor == null }
}

@Serializable
data class Game(
    val config: GameConfig,
    val hand: Hand,
    val score: Score = Score(),
    val timeline: List<ScoreEvent> = emptyList(),
) {
    val winner: Team? get() = Team.entries.firstOrNull { score[it] >= config.target }

    companion object {
        fun start(config: GameConfig, mano: Team) = Game(config = config, hand = Hand(mano = mano))
    }
}

/** Lo que la mesa tiene que resolver a continuación. */
sealed interface Prompt {
    data class HandWinner(val points: Int, val truco: TrucoCall?) : Prompt

    data class Answer(
        val calls: List<Canto>,
        val caller: Team,
        val rejectPoints: Int,
        val raises: List<Canto>,
    ) : Prompt {
        val responder: Team get() = caller.opponent
    }

    data class FlorSung(
        val caller: Team,
        val points: Int,
        val raises: List<Canto>,
    ) : Prompt {
        val responder: Team get() = caller.opponent
    }

    data class TantoWinner(val calls: List<Canto>, val prizes: Map<Team, Int>) : Prompt

    data class GameOver(val winner: Team, val score: Score) : Prompt
}
