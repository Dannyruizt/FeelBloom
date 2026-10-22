package com.feelbloom.app.domain

import com.feelbloom.app.model.EmotionRecord
import java.time.Instant
import java.time.ZoneId

/**
 * Ventana de "la semana pasada" para las pantallas de Historial y Estadísticas:
 * los 7 días inmediatamente anteriores a los "últimos 7 días" (hoy y 6 días atrás).
 */
object WeekComparison {

    fun previousSevenDays(
        records: List<EmotionRecord>,
        nowMillis: Long,
        zone: ZoneId,
    ): List<EmotionRecord> {
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        val start = today.minusDays(13).atStartOfDay(zone).toInstant().toEpochMilli()
        val endExclusive = today.minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()
        return records.filter { it.timestamp in start until endExclusive }
    }
}
