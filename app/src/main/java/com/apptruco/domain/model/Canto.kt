package com.apptruco.domain.model

import kotlinx.serialization.Serializable

sealed interface Canto

@Serializable
enum class EnvidoCall : Canto { ENVIDO, REAL_ENVIDO, FALTA_ENVIDO }

@Serializable
enum class FlorCall : Canto { FLOR, CONTRAFLOR, CONTRAFLOR_AL_RESTO }

@Serializable
enum class TrucoCall(val points: Int) : Canto {
    TRUCO(2),
    RETRUCO(3),
    VALE_CUATRO(4);

    val next: TrucoCall? get() = entries.getOrNull(ordinal + 1)
}

/**
 * Cadena de cantos de un mismo tipo. [caller] es quien cantó lo último; cada subida la hace
 * el rival, así que siempre responde (o sube) [responder]. [previous] es la apuesta tal como
 * estaba antes del último canto (null si no había), para poder cancelarlo.
 */
@Serializable
data class Bet<C : Canto>(
    val calls: List<C>,
    val caller: Team,
    val accepted: Boolean = false,
    val previous: Bet<C>? = null,
) {
    init {
        require(calls.isNotEmpty())
    }

    val last: C get() = calls.last()

    val responder: Team get() = caller.opponent

    val isPending: Boolean get() = !accepted

    fun raise(call: C): Bet<C> = Bet(calls + call, caller = responder, previous = this)

    fun accept(): Bet<C> = copy(accepted = true)
}
