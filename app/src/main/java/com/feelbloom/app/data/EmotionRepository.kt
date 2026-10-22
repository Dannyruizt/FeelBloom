package com.feelbloom.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.feelbloom.app.model.EmotionRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Punto único de acceso a los registros (sección 7.3). La UI nunca toca DataStore. */
interface EmotionRepository {
    val records: Flow<List<EmotionRecord>>

    suspend fun add(record: EmotionRecord)

    /** Cambia emoción y nota conservando id y fecha originales (RF-06). */
    suspend fun update(id: String, emotionId: String, note: String)

    suspend fun delete(id: String)
}

val Context.feelBloomDataStore: DataStore<Preferences> by preferencesDataStore(name = "feelbloom")

class DataStoreEmotionRepository(
    private val store: DataStore<Preferences>,
) : EmotionRepository {

    private val recordsKey = stringPreferencesKey("records_json")

    override val records: Flow<List<EmotionRecord>> = store.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs -> RecordJson.decode(prefs[recordsKey]) }

    // DataStore.edit es atómico y se ejecuta fuera del hilo principal (RNF-06).

    override suspend fun add(record: EmotionRecord) {
        store.edit { prefs ->
            val current = RecordJson.decode(prefs[recordsKey])
            if (current.none { it.id == record.id }) { // evita duplicados (RNF-10)
                prefs[recordsKey] = RecordJson.encode(current + record)
            }
        }
    }

    override suspend fun update(id: String, emotionId: String, note: String) {
        store.edit { prefs ->
            val updated = RecordJson.decode(prefs[recordsKey]).map {
                if (it.id == id) it.copy(emotionId = emotionId, note = note) else it
            }
            prefs[recordsKey] = RecordJson.encode(updated)
        }
    }

    override suspend fun delete(id: String) {
        store.edit { prefs ->
            val remaining = RecordJson.decode(prefs[recordsKey]).filterNot { it.id == id }
            prefs[recordsKey] = RecordJson.encode(remaining)
        }
    }
}
