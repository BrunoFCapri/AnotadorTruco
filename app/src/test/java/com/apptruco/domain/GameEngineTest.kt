package com.apptruco.domain

import com.apptruco.domain.model.EnvidoCall
import com.apptruco.domain.model.EnvidoCall.ENVIDO
import com.apptruco.domain.model.EnvidoCall.FALTA_ENVIDO
import com.apptruco.domain.model.EnvidoCall.REAL_ENVIDO
import com.apptruco.domain.model.FlorCall.CONTRAFLOR
import com.apptruco.domain.model.FlorCall.FLOR
import com.apptruco.domain.model.Game
import com.apptruco.domain.model.GameAction
import com.apptruco.domain.model.GameAction.AddPoints
import com.apptruco.domain.model.GameAction.Answer
import com.apptruco.domain.model.GameAction.CancelCanto
import com.apptruco.domain.model.GameAction.Sing
import com.apptruco.domain.model.GameAction.WinHand
import com.apptruco.domain.model.GameAction.WinTanto
import com.apptruco.domain.model.GameConfig
import com.apptruco.domain.model.Hand
import com.apptruco.domain.model.PointSource
import com.apptruco.domain.model.Prompt
import com.apptruco.domain.model.Score
import com.apptruco.domain.model.ScoreEvent
import com.apptruco.domain.model.Standing
import com.apptruco.domain.model.Team
import com.apptruco.domain.model.Team.ELLOS
import com.apptruco.domain.model.Team.NOSOTROS
import com.apptruco.domain.model.TrucoCall.RETRUCO
import com.apptruco.domain.model.TrucoCall.TRUCO
import com.apptruco.domain.rules.Scoring
import com.apptruco.domain.rules.cantosFor
import com.apptruco.domain.rules.performance
import com.apptruco.domain.rules.prompt
import com.apptruco.domain.rules.reduce
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    private val aTreinta = GameConfig(target = 30)
    private val conFlor = aTreinta.copy(withFlor = true)

    private fun game(
        nosotros: Int = 0,
        ellos: Int = 0,
        config: GameConfig = aTreinta,
        mano: Team = NOSOTROS,
    ) = Game(config = config, hand = Hand(mano = mano), score = Score(nosotros, ellos))

    private fun Game.play(vararg actions: GameAction): Game =
        actions.fold(this) { game, action -> game.reduce(action) }

    @Test
    fun `envido no querido da un punto al que canto`() {
        val result = game().play(Sing(NOSOTROS, ENVIDO), Answer(accept = false))

        assertEquals(Score(1, 0), result.score)
        assertTrue(result.hand.tantoPlayed)
    }

    @Test
    fun `envido envido real envido querido vale siete`() {
        val result = game().play(
            Sing(NOSOTROS, ENVIDO),
            Sing(ELLOS, ENVIDO),
            Sing(NOSOTROS, REAL_ENVIDO),
            Answer(accept = true),
            WinTanto(ELLOS),
        )

        assertEquals(Score(0, 7), result.score)
    }

    @Test
    fun `real envido no querido despues de envido envido vale cuatro`() {
        val result = game().play(
            Sing(NOSOTROS, ENVIDO),
            Sing(ELLOS, ENVIDO),
            Sing(NOSOTROS, REAL_ENVIDO),
            Answer(accept = false),
        )

        assertEquals(Score(4, 0), result.score)
    }

    @Test
    fun `falta envido con los dos en las malas da el partido`() {
        val result = game(nosotros = 10, ellos = 4).play(
            Sing(ELLOS, FALTA_ENVIDO),
            Answer(accept = true),
            WinTanto(ELLOS),
        )

        assertEquals(30, result.score.ellos)
        assertEquals(ELLOS, result.winner)
    }

    @Test
    fun `falta envido en las buenas da lo que le falta al que va ganando`() {
        val result = game(nosotros = 22, ellos = 12).play(
            Sing(ELLOS, FALTA_ENVIDO),
            Answer(accept = true),
            WinTanto(ELLOS),
        )

        assertEquals(20, result.score.ellos)
    }

    @Test
    fun `retruco no querido da dos al que lo canto y pasa la mano`() {
        val result = game().play(Sing(NOSOTROS, TRUCO), Sing(ELLOS, RETRUCO), Answer(accept = false))

        assertEquals(Score(0, 2), result.score)
        assertEquals(ELLOS, result.hand.mano)
        assertNull(result.hand.truco)
    }

    @Test
    fun `solo quien tiene el quiero puede subir el truco`() {
        val accepted = game().play(Sing(NOSOTROS, TRUCO), Answer(accept = true))

        assertEquals(listOf(RETRUCO), accepted.cantosFor(ELLOS))
        assertTrue(accepted.cantosFor(NOSOTROS).isEmpty())
        assertEquals(accepted, accepted.reduce(Sing(NOSOTROS, RETRUCO)))
    }

    @Test
    fun `el envido esta primero y despues se responde el truco`() {
        val afterEnvido = game().play(
            Sing(NOSOTROS, TRUCO),
            Sing(ELLOS, ENVIDO),
            Answer(accept = true),
            WinTanto(NOSOTROS),
        )

        assertEquals(Score(2, 0), afterEnvido.score)
        assertTrue(afterEnvido.prompt() is Prompt.Answer)

        val result = afterEnvido.play(Answer(accept = true), WinHand(ELLOS))
        assertEquals(Score(2, 2), result.score)
    }

    @Test
    fun `con el truco querido ya no se canta envido`() {
        val accepted = game().play(Sing(NOSOTROS, TRUCO), Answer(accept = true))

        assertTrue(EnvidoCall.entries.none { it in accepted.cantosFor(ELLOS) })
    }

    @Test
    fun `la flor anula el envido`() {
        val result = game(config = conFlor).play(Sing(NOSOTROS, ENVIDO), Sing(ELLOS, FLOR), Answer(accept = true))

        assertEquals(Score(0, 3), result.score)
        assertTrue(result.hand.tantoPlayed)
    }

    @Test
    fun `con flor me achico ante la contraflor da cuatro`() {
        val result = game(config = conFlor).play(Sing(NOSOTROS, FLOR), Sing(ELLOS, CONTRAFLOR), Answer(accept = false))

        assertEquals(Score(0, 4), result.score)
    }

    @Test
    fun `sin flor configurada no se ofrece flor`() {
        assertTrue(FLOR !in game().cantosFor(NOSOTROS))
    }

    @Test
    fun `el puntaje se corta en el objetivo y define al ganador`() {
        val result = game(nosotros = 28).play(AddPoints(NOSOTROS, 3))

        assertEquals(30, result.score.nosotros)
        assertEquals(NOSOTROS, result.winner)
        assertTrue(result.prompt() is Prompt.GameOver)
    }

    @Test
    fun `malas y buenas`() {
        assertEquals(Standing(malas = 15, buenas = 0), Scoring.standing(15, aTreinta))
        assertEquals(Standing(malas = 15, buenas = 3), Scoring.standing(18, aTreinta))
        assertEquals(Standing(malas = 12, buenas = 0), Scoring.standing(12, GameConfig(target = 15)))
    }

    @Test
    fun `pica-pica arranca al entrar en las buenas y alterna con la redonda`() {
        val seis = GameConfig(target = 30, players = 6, picaPica = true)
        val first = game(nosotros = 15, config = seis).play(WinHand(NOSOTROS))

        assertTrue(first.hand.picaPica)
        assertEquals(1, first.hand.duel)

        val redonda = first.play(WinHand(ELLOS), WinHand(ELLOS), WinHand(ELLOS))
        assertFalse(redonda.hand.picaPica)
        assertEquals(first.hand.mano.opponent, redonda.hand.mano)

        assertTrue(redonda.play(WinHand(ELLOS)).hand.picaPica)
    }

    @Test
    fun `a cinco de terminar se vuelve a la redonda`() {
        val seis = GameConfig(target = 30, players = 6, picaPica = true)

        assertFalse(game(nosotros = 24, config = seis).play(WinHand(NOSOTROS)).hand.picaPica)
    }

    @Test
    fun `cancelar un canto lo saca como si no se hubiera cantado`() {
        val start = game()

        assertEquals(start, start.play(Sing(NOSOTROS, TRUCO), CancelCanto))
        assertEquals(start, start.play(Sing(ELLOS, FALTA_ENVIDO), CancelCanto))
    }

    @Test
    fun `cancelar una subida vuelve a la cadena anterior`() {
        val envido = game().play(Sing(NOSOTROS, ENVIDO))

        val canceled = envido.play(Sing(ELLOS, REAL_ENVIDO), CancelCanto)

        assertEquals(envido, canceled)
        assertEquals(ELLOS, (canceled.prompt() as Prompt.Answer).responder)
    }

    @Test
    fun `cancelar el retruco deja el truco querido como estaba`() {
        val accepted = game().play(Sing(NOSOTROS, TRUCO), Answer(accept = true))

        val canceled = accepted.play(Sing(ELLOS, RETRUCO), CancelCanto)

        assertEquals(accepted, canceled)
        assertEquals(Prompt.HandWinner(points = 2, truco = TRUCO), canceled.prompt())
    }

    @Test
    fun `cancelar la flor devuelve el envido que tapaba`() {
        val envido = game(config = conFlor).play(Sing(NOSOTROS, ENVIDO))

        assertEquals(envido, envido.play(Sing(ELLOS, FLOR), CancelCanto))
    }

    @Test
    fun `cancelar sin canto pendiente no hace nada`() {
        val accepted = game().play(Sing(NOSOTROS, TRUCO), Answer(accept = true))

        assertSame(accepted, accepted.reduce(CancelCanto))
    }

    @Test
    fun `el rendimiento registra cada punto con su origen`() {
        val result = game().play(
            Sing(NOSOTROS, ENVIDO),
            Answer(accept = true),
            WinTanto(ELLOS),
            Sing(NOSOTROS, TRUCO),
            Answer(accept = true),
            WinHand(NOSOTROS),
            AddPoints(ELLOS, -1),
        )

        val performance = result.performance()

        assertEquals(
            listOf(Score(0, 0), Score(0, 2), Score(2, 2), Score(2, 1)),
            performance.progression,
        )
        assertEquals(2, performance.pointsBySource.getValue(ELLOS).getValue(PointSource.ENVIDO))
        assertEquals(-1, performance.pointsBySource.getValue(ELLOS).getValue(PointSource.MANUAL))
        assertEquals(2, performance.pointsBySource.getValue(NOSOTROS).getValue(PointSource.HAND))
        assertEquals(mapOf(NOSOTROS to 1, ELLOS to 2), performance.biggestLead)
    }

    @Test
    fun `el rendimiento anota lo que entro y no lo que se pidio`() {
        val result = game(nosotros = 28).play(AddPoints(NOSOTROS, 3))

        assertEquals(listOf(ScoreEvent(NOSOTROS, 2, PointSource.MANUAL)), result.timeline)
    }

    @Test
    fun `deshacer vuelve paso a paso`() {
        val start = Session.start(aTreinta, NOSOTROS)
        val played = start.apply(AddPoints(NOSOTROS, 3)).apply(Sing(ELLOS, TRUCO))

        val undone = played.undo()
        assertEquals(Score(3, 0), undone.game.score)
        assertNull(undone.game.hand.truco)
        assertEquals(start.game, undone.undo().game)
        assertFalse(undone.undo().canUndo)
    }

    @Test
    fun `una accion que no corresponde no entra al historial`() {
        val start = Session.start(aTreinta, NOSOTROS)

        assertSame(start, start.apply(Answer(accept = true)))
    }
}
