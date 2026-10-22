package com.feelbloom.app.domain

import com.feelbloom.app.model.Emotion
import com.feelbloom.app.model.EmotionCatalog
import com.feelbloom.app.model.EmotionRecord

data class EmotionStat(
    val emotion: Emotion,
    val count: Int,
    val percent: Double,
)

/**
 * Cálculo de frecuencias (RF-09, RN-06). Solo cuenta y calcula porcentajes:
 * no interpreta los resultados (RN-08).
 */
object StatsCalculator {

    /** Una entrada por emoción del catálogo, ordenadas de mayor a menor cantidad. Vacío si no hay registros. */
    fun compute(records: List<EmotionRecord>): List<EmotionStat> {
        val valid = records.filter { EmotionCatalog.find(it.emotionId) != null }
        if (valid.isEmpty()) return emptyList()

        val total = valid.size
        val counts = valid.groupingBy { it.emotionId }.eachCount()
        return EmotionCatalog.all
            .map { emotion ->
                val count = counts[emotion.id] ?: 0
                EmotionStat(emotion, count, count * 100.0 / total)
            }
            .sortedByDescending { it.count }
    }

    /** Emoción(es) con más registros; si hay empate se devuelven todas. */
    fun predominant(stats: List<EmotionStat>): List<EmotionStat> {
        val max = stats.maxOfOrNull { it.count } ?: return emptyList()
        if (max == 0) return emptyList()
        return stats.filter { it.count == max }
    }
}
