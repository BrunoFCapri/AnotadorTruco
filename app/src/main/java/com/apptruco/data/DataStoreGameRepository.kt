package com.apptruco.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.apptruco.domain.GameRepository
import com.apptruco.domain.Session
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * La partida vive en memoria: cada jugada se muestra apenas se toca y se escribe a disco enseguida,
 * en orden y siempre la última. Si el sistema mata la app en los milisegundos que tarda en
 * guardarse, se pierde solo esa última jugada.
 */
class DataStoreGameRepository(
    private val store: DataStore<SavedState>,
    scope: CoroutineScope,
) : GameRepository {

    private val enDisco: Flow<Session?> = store.data
        .catch { error -> if (error is IOException) emit(SavedStateSerializer.defaultValue) else throw error }
        .map { it.session }

    // null hasta que se lee el disco; después, la partida actual (que puede no haber empezado).
    private val actual = MutableStateFlow<Partida?>(null)
    private val mutex = Mutex()

    override val session: Flow<Session?> = flow {
        cargar()
        emitAll(actual.filterNotNull().map { it.session })
    }

    init {
        // Un solo escritor: guarda la última partida cada vez que cambia, sin pisar una escritura en curso.
        scope.launch {
            actual.filterNotNull()
                .filterNot { it.leidaDelDisco }
                .conflate()
                .collect { partida -> guardar(partida.session) }
        }
    }

    override suspend fun update(transform: (Session?) -> Session?) {
        cargar()
        mutex.withLock {
            val anterior = checkNotNull(actual.value) { "La partida ya se cargó" }.session
            val nueva = transform(anterior)
            if (nueva !== anterior) actual.value = Partida(nueva)
        }
    }

    private suspend fun cargar() {
        if (actual.value != null) return
        mutex.withLock {
            if (actual.value == null) actual.value = Partida(enDisco.first(), leidaDelDisco = true)
        }
    }

    private suspend fun guardar(session: Session?) {
        try {
            store.updateData { saved -> saved.copy(session = session) }
        } catch (e: IOException) {
            // No se pudo escribir: la próxima jugada vuelve a guardar la partida entera.
        }
    }

    /** Sin equals a propósito: cada jugada es un valor nuevo para el escritor, aunque se repita la partida. */
    private class Partida(val session: Session?, val leidaDelDisco: Boolean = false)

    companion object {
        fun create(context: Context) = DataStoreGameRepository(
            store = DataStoreFactory.create(
                serializer = SavedStateSerializer,
                corruptionHandler = ReplaceFileCorruptionHandler { SavedStateSerializer.defaultValue },
                produceFile = { context.dataStoreFile("partida.json") },
            ),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        )
    }
}
