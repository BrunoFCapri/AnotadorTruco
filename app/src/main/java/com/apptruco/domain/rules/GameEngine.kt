package com.apptruco.domain.rules

import com.apptruco.domain.model.Bet
import com.apptruco.domain.model.Canto
import com.apptruco.domain.model.EnvidoCall
import com.apptruco.domain.model.FlorCall
import com.apptruco.domain.model.Game
import com.apptruco.domain.model.GameAction
import com.apptruco.domain.model.Hand
import com.apptruco.domain.model.PointSource
import com.apptruco.domain.model.Prompt
import com.apptruco.domain.model.ScoreEvent
import com.apptruco.domain.model.Team
import com.apptruco.domain.model.TrucoCall

/** Aplica una acción. Las acciones que no corresponden al estado actual devuelven el mismo juego. */
fun Game.reduce(action: GameAction): Game = when (action) {
    is GameAction.AddPoints -> award(action.team, action.points, PointSource.MANUAL)
    is GameAction.Sing -> sing(action.team, action.canto)
    is GameAction.Answer -> answer(action.accept)
    GameAction.CancelCanto -> cancelCanto()
    is GameAction.WinTanto -> winTanto(action.team)
    is GameAction.WinHand ->
        if (winner == null && hand.isIdle) endHand(action.team, hand.trucoPoints) else this
    GameAction.PassMano -> withHand { copy(mano = mano.opponent) }
}

fun Game.cantosFor(team: Team): List<Canto> {
    val flor = hand.flor
    val envido = hand.activeEnvido
    return when {
        winner != null -> emptyList()
        flor != null ->
            if (flor.isPending && team == flor.responder) florRaises(flor.last) else emptyList()
        envido != null ->
            if (envido.isPending && team == envido.responder) {
                envidoRaises(envido.calls) + florOpening()
            } else {
                emptyList()
            }
        else -> tantoOpenings(team) + trucoCalls(team)
    }
}

fun Game.prompt(): Prompt {
    val gameWinner = winner
    val flor = hand.flor
    val envido = hand.activeEnvido
    val truco = hand.truco
    return when {
        gameWinner != null -> Prompt.GameOver(gameWinner, score)
        flor != null && flor.accepted -> Prompt.TantoWinner(
            calls = flor.calls,
            prizes = Team.entries.associateWith { team ->
                Scoring.florQuerida(flor.calls, Scoring.falta(score, team, config))
            },
        )
        flor != null && flor.last == FlorCall.FLOR ->
            Prompt.FlorSung(flor.caller, Scoring.FLOR, cantosFor(flor.responder))
        flor != null -> answerPrompt(flor, Scoring.florNoQuerida(flor.calls))
        envido != null && envido.accepted -> Prompt.TantoWinner(
            calls = envido.calls,
            prizes = Team.entries.associateWith { team ->
                Scoring.envidoQuerido(envido.calls, Scoring.falta(score, team, config))
            },
        )
        envido != null -> answerPrompt(envido, Scoring.envidoNoQuerido(envido.calls))
        truco != null && truco.isPending -> answerPrompt(truco, Scoring.trucoNoQuerido(truco.calls))
        else -> Prompt.HandWinner(hand.trucoPoints, truco?.last)
    }
}

private fun Game.answerPrompt(bet: Bet<*>, rejectPoints: Int) = Prompt.Answer(
    calls = bet.calls,
    caller = bet.caller,
    rejectPoints = rejectPoints,
    raises = cantosFor(bet.responder),
)

private fun Game.sing(team: Team, canto: Canto): Game {
    if (canto !in cantosFor(team)) return this
    return withHand {
        when (canto) {
            is EnvidoCall -> copy(envido = envido?.raise(canto) ?: Bet(listOf(canto), team))
            is FlorCall -> copy(flor = flor?.raise(canto) ?: Bet(listOf(canto), team))
            is TrucoCall -> copy(truco = truco?.raise(canto) ?: Bet(listOf(canto), team))
        }
    }
}

private fun Game.answer(accept: Boolean): Game {
    val flor = hand.flor
    val envido = hand.activeEnvido
    val truco = hand.truco
    return when {
        winner != null -> this
        flor != null -> when {
            flor.accepted -> this
            flor.last == FlorCall.FLOR -> award(flor.caller, Scoring.FLOR, PointSource.FLOR).closeTanto()
            accept -> withHand { copy(flor = flor.accept()) }
            else -> award(flor.caller, Scoring.florNoQuerida(flor.calls), PointSource.FLOR).closeTanto()
        }
        envido != null -> when {
            envido.accepted -> this
            accept -> withHand { copy(envido = envido.accept()) }
            else -> award(envido.caller, Scoring.envidoNoQuerido(envido.calls), PointSource.ENVIDO).closeTanto()
        }
        truco != null && truco.isPending ->
            if (accept) {
                withHand { copy(truco = truco.accept()) }
            } else {
                endHand(truco.caller, Scoring.trucoNoQuerido(truco.calls))
            }
        else -> this
    }
}

private fun Game.cancelCanto(): Game {
    val flor = hand.flor
    val envido = hand.activeEnvido
    val truco = hand.truco
    return when {
        winner != null -> this
        flor != null -> if (flor.isPending) withHand { copy(flor = flor.previous) } else this
        envido != null -> if (envido.isPending) withHand { copy(envido = envido.previous) } else this
        truco != null && truco.isPending -> withHand { copy(truco = truco.previous) }
        else -> this
    }
}

private fun Game.winTanto(team: Team): Game {
    val pending = prompt() as? Prompt.TantoWinner ?: return this
    val source = if (pending.calls.first() is FlorCall) PointSource.FLOR else PointSource.ENVIDO
    return award(team, pending.prizes.getValue(team), source).closeTanto()
}

private fun Game.endHand(team: Team, points: Int): Game =
    award(team, points, PointSource.HAND).run { copy(hand = nextHand()) }

private fun Game.nextHand(): Hand =
    if (hand.picaPica && hand.duel < config.duels) {
        Hand(mano = hand.mano, picaPica = true, duel = hand.duel + 1)
    } else {
        Hand(
            mano = hand.mano.opponent,
            picaPica = !hand.picaPica && Scoring.isPicaPicaZone(score, config),
        )
    }

private fun Game.tantoOpenings(team: Team): List<Canto> {
    val truco = hand.truco
    val trucoAllowsTanto = truco == null ||
        (truco.isPending && truco.last == TrucoCall.TRUCO && team == truco.responder)
    return if (!hand.tantoPlayed && trucoAllowsTanto) EnvidoCall.entries + florOpening() else emptyList()
}

private fun Game.trucoCalls(team: Team): List<TrucoCall> {
    val truco = hand.truco ?: return listOf(TrucoCall.TRUCO)
    return if (team == truco.responder) listOfNotNull(truco.last.next) else emptyList()
}

private fun Game.florOpening(): List<FlorCall> =
    if (config.withFlor) listOf(FlorCall.FLOR) else emptyList()

private fun envidoRaises(calls: List<EnvidoCall>): List<EnvidoCall> = buildList {
    if (calls.size < 2 && calls.all { it == EnvidoCall.ENVIDO }) add(EnvidoCall.ENVIDO)
    if (EnvidoCall.REAL_ENVIDO !in calls && EnvidoCall.FALTA_ENVIDO !in calls) add(EnvidoCall.REAL_ENVIDO)
    if (EnvidoCall.FALTA_ENVIDO !in calls) add(EnvidoCall.FALTA_ENVIDO)
}

private fun florRaises(last: FlorCall): List<FlorCall> = when (last) {
    FlorCall.FLOR -> listOf(FlorCall.CONTRAFLOR, FlorCall.CONTRAFLOR_AL_RESTO)
    FlorCall.CONTRAFLOR -> listOf(FlorCall.CONTRAFLOR_AL_RESTO)
    FlorCall.CONTRAFLOR_AL_RESTO -> emptyList()
}

private fun Game.award(team: Team, points: Int, source: PointSource): Game {
    val updated = score.plus(team, points, cap = config.target)
    val scored = updated[team] - score[team]
    return if (scored == 0) this else copy(score = updated, timeline = timeline + ScoreEvent(team, scored, source))
}

private fun Game.closeTanto(): Game = withHand { copy(envido = null, flor = null, tantoPlayed = true) }

private inline fun Game.withHand(transform: Hand.() -> Hand): Game = copy(hand = hand.transform())
