package com.apptruco.data

import androidx.datastore.core.CorruptionException
import com.apptruco.domain.Session
import com.apptruco.domain.model.EnvidoCall
import com.apptruco.domain.model.GameAction
import com.apptruco.domain.model.GameConfig
import com.apptruco.domain.model.Team
import com.apptruco.domain.model.TrucoCall
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SavedStateSerializerTest {

    @Test
    fun `una partida a mitad de canto sobrevive el guardado`() = runBlocking {
        val session = Session.start(GameConfig(withFlor = true, players = 6, picaPica = true), Team.ELLOS)
            .apply(GameAction.AddPoints(Team.NOSOTROS, 17))
            .apply(GameAction.Sing(Team.NOSOTROS, TrucoCall.TRUCO))
            .apply(GameAction.Sing(Team.ELLOS, EnvidoCall.REAL_ENVIDO))
            .apply(GameAction.Sing(Team.NOSOTROS, EnvidoCall.FALTA_ENVIDO))
        val saved = SavedState(session)

        val bytes = ByteArrayOutputStream().also { SavedStateSerializer.writeTo(saved, it) }.toByteArray()

        assertEquals(saved, SavedStateSerializer.readFrom(ByteArrayInputStream(bytes)))
    }

    @Test(expected = CorruptionException::class)
    fun `un archivo roto se reporta como corrupto`(): Unit = runBlocking {
        SavedStateSerializer.readFrom(ByteArrayInputStream("{no es json".encodeToByteArray()))
    }
}
