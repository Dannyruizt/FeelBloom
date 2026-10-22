package com.feelbloom.app.domain

import com.feelbloom.app.model.EmotionRecord
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

/** Periodos de consulta (RF-08). */
enum class Period(val label: String) {
    LAST_7_DAYS("Últimos 7 días"),
    MONTH("Mes"),
    ALL("Todo"),
}

object RecordFilter {

    fun filter(
        records: List<EmotionRecord>,
        period: Period,
        month: YearMonth,
        nowMillis: Long,
        zone: ZoneId,
    ): List<EmotionRecord> = when (period) {
        Period.ALL -> records
        Period.LAST_7_DAYS -> {
            // Hoy y los 6 días anteriores, desde las 00:00 de la hora local.
            val start = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
                .minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()
            records.filter { it.timestamp >= start }
        }
        Period.MONTH -> records.filter {
            YearMonth.from(Instant.ofEpochMilli(it.timestamp).atZone(zone)) == month
        }
    }

    /** Registros del día actual según la hora local del dispositivo. */
    fun today(records: List<EmotionRecord>, nowMillis: Long, zone: ZoneId): List<EmotionRecord> {
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        return records.filter {
            Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() == today
        }
    }
}
