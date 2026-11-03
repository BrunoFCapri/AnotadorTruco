package com.apptruco.data

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.apptruco.domain.Session
import com.apptruco.domain.model.GameAction
import com.apptruco.domain.model.GameConfig
import com.apptruco.domain.model.Team
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreGameRepositoryTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private val inicio = Session.start(GameConfig(), Team.NOSOTROS)
    private val conTres = inicio.apply(GameAction.AddPoints(Team.NOSOTROS, 3))

    private fun abrir(archivo: File): Pair<DataStore<SavedState>, CoroutineScope> {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        return DataStoreFactory.create(SavedStateSerializer, scope = scope, produceFile = { archivo }) to scope
    }

    @Test
    fun `la jugada se ve enseguida y despues queda guardada`() = runBlocking {
        val (store, scope) = abrir(File(carpeta.root, "partida.json"))
        val repository = DataStoreGameRepository(store, scope)

        repository.update { inicio }
        repository.update { it?.apply(GameAction.AddPoints(Team.NOSOTROS, 3)) }

        assertEquals(conTres, repository.session.first())
        withTimeout(5_000) { store.data.first { it.session == conTres } }
        scope.coroutineContext.job.cancelAndJoin()
    }

    @Test
    fun `al volver a abrir se lee la ultima partida guardada`() = runBlocking {
        val archivo = File(carpeta.root, "partida.json")
        val (store, scope) = abrir(archivo)
        val repository = DataStoreGameRepository(store, scope)
        repository.update { inicio }
        repository.update { it?.apply(GameAction.AddPoints(Team.NOSOTROS, 3)) }
        withTimeout(5_000) { store.data.first { it.session == conTres } }
        scope.coroutineContext.job.cancelAndJoin()

        val (otroStore, otroScope) = abrir(archivo)
        assertEquals(conTres, DataStoreGameRepository(otroStore, otroScope).session.first())
        otroScope.coroutineContext.job.cancelAndJoin()
    }
}
