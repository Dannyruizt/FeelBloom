package com.feelbloom.app.data

import com.feelbloom.app.model.EmotionRecord
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Estrategia de serialización para DataStore (sección 7.4): la colección de registros
 * se guarda como un arreglo JSON dentro de una sola preferencia de tipo String.
 * Usa org.json, que ya viene con Android, así que no añade dependencias.
 */
object RecordJson {

    fun encode(records: List<EmotionRecord>): String {
        val array = JSONArray()
        records.forEach { record ->
            array.put(
                JSONObject()
                    .put("id", record.id)
                    .put("emotionId", record.emotionId)
                    .put("timestamp", record.timestamp)
                    .put("note", record.note)
            )
        }
        return array.toString()
    }

    fun decode(json: String?): List<EmotionRecord> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            List(array.length()) { index ->
                val obj = array.getJSONObject(index)
                EmotionRecord(
                    id = obj.getString("id"),
                    emotionId = obj.getString("emotionId"),
                    timestamp = obj.getLong("timestamp"),
                    note = obj.optString("note", ""),
                )
            }
        } catch (e: JSONException) {
            emptyList()
        }
    }
}
