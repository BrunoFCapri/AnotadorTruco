package com.apptruco.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.apptruco.domain.Session
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SavedState(val session: Session? = null)

object SavedStateSerializer : Serializer<SavedState> {
    private val json = Json { ignoreUnknownKeys = true }

    override val defaultValue = SavedState()

    override suspend fun readFrom(input: InputStream): SavedState =
        try {
            json.decodeFromString(SavedState.serializer(), input.readBytes().decodeToString())
        } catch (e: IllegalArgumentException) {
            throw CorruptionException("No se pudo leer la partida guardada", e)
        }

    override suspend fun writeTo(t: SavedState, output: OutputStream) {
        output.write(json.encodeToString(SavedState.serializer(), t).encodeToByteArray())
    }
}
