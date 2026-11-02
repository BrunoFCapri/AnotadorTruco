package com.apptruco.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.apptruco.domain.GameRepository
import com.apptruco.domain.Session
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/** Cada jugada se escribe a disco antes de mostrarse: si el sistema mata la app, no se pierde nada. */
class DataStoreGameRepository(private val store: DataStore<SavedState>) : GameRepository {

    override val session: Flow<Session?> = store.data
        .catch { error -> if (error is IOException) emit(SavedStateSerializer.defaultValue) else throw error }
        .map { it.session }

    override suspend fun update(transform: (Session?) -> Session?) {
        store.updateData { saved -> saved.copy(session = transform(saved.session)) }
    }

    companion object {
        fun create(context: Context) = DataStoreGameRepository(
            DataStoreFactory.create(
                serializer = SavedStateSerializer,
                corruptionHandler = ReplaceFileCorruptionHandler { SavedStateSerializer.defaultValue },
                produceFile = { context.dataStoreFile("partida.json") },
            ),
        )
    }
}
