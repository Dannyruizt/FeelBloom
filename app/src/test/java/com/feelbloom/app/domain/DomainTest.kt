package com.feelbloom.app.domain

import com.feelbloom.app.data.RecordJson
import com.feelbloom.app.model.EmotionRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

class DomainTest {

    private val zone = ZoneId.of("America/Mexico_City")

    // "Ahora" fijo: miércoles 21 de octubre de 2026, 12:00.
    private val now = at(2026, 10, 21, 12, 0)

    private fun at(y: Int, m: Int, d: Int, h: Int, min: Int): Long =
        LocalDateTime.of(y, m, d, h, min).atZone(zone).toInstant().toEpochMilli()

    private fun record(id: String, emotionId: String, timestamp: Long, note: String = "") =
        EmotionRecord(id, emotionId, timestamp, note)

    // ---------- StatsCalculator (RF-09, RN-06) ----------

    @Test
    fun `sin registros no hay estadisticas ni emocion predominante`() {
        val stats = StatsCalculator.compute(emptyList())
        assertTrue(stats.isEmpty())
        assertTrue(StatsCalculator.predominant(stats).isEmpty())
    }

    @Test
    fun `cuenta y porcentajes son correctos y suman 100`() {
        val records = listOf(
            record("1", "happiness", now),
            record("2", "happiness", now),
            record("3", "calm", now),
            record("4", "love", now),
        )
        val stats = StatsCalculator.compute(records).filter { it.count > 0 }

        assertEquals(listOf("happiness", "calm", "love"), stats.map { it.emotion.id })
        assertEquals(listOf(2, 1, 1), stats.map { it.count })
        assertEquals(50.0, stats[0].percent, 0.001)
        assertEquals(25.0, stats[1].percent, 0.001)
        assertEquals(100.0, stats.sumOf { it.percent }, 0.001)
    }

    @Test
    fun `en caso de empate la emocion predominante devuelve todas las empatadas`() {
        val records = listOf(record("1", "anger", now), record("2", "calm", now))
        val top = StatsCalculator.predominant(StatsCalculator.compute(records))
        assertEquals(setOf("anger", "calm"), top.map { it.emotion.id }.toSet())
    }

    // ---------- RecordFilter (RF-08) ----------

    @Test
    fun `ultimos 7 dias incluye hoy y 6 dias atras, nada mas`() {
        val inside = record("in", "calm", at(2026, 10, 15, 0, 0))
        val outside = record("out", "calm", at(2026, 10, 14, 23, 59))
        val result = RecordFilter.filter(
            listOf(inside, outside), Period.LAST_7_DAYS, YearMonth.of(2026, 10), now, zone,
        )
        assertEquals(listOf("in"), result.map { it.id })
    }

    @Test
    fun `filtro por mes y por todo el historial`() {
        val october = record("oct", "calm", at(2026, 10, 2, 9, 0))
        val september = record("sep", "calm", at(2026, 9, 30, 23, 0))
        val all = listOf(october, september)

        assertEquals(
            listOf("oct"),
            RecordFilter.filter(all, Period.MONTH, YearMonth.of(2026, 10), now, zone).map { it.id },
        )
        assertEquals(
            listOf("sep"),
            RecordFilter.filter(all, Period.MONTH, YearMonth.of(2026, 9), now, zone).map { it.id },
        )
        assertEquals(2, RecordFilter.filter(all, Period.ALL, YearMonth.of(2026, 10), now, zone).size)
    }

    @Test
    fun `today solo devuelve registros del dia local`() {
        val today = record("today", "calm", at(2026, 10, 21, 0, 1))
        val yesterday = record("yesterday", "calm", at(2026, 10, 20, 23, 59))
        assertEquals(
            listOf("today"),
            RecordFilter.today(listOf(today, yesterday), now, zone).map { it.id },
        )
    }

    // ---------- WeekComparison ----------

    @Test
    fun `la semana pasada abarca de hace 13 a hace 7 dias`() {
        val records = listOf(
            record("a", "calm", at(2026, 10, 14, 18, 0)), // hace 7 días: dentro
            record("b", "calm", at(2026, 10, 8, 0, 0)),   // hace 13 días: dentro
            record("c", "calm", at(2026, 10, 7, 23, 59)), // hace 14 días: fuera
            record("d", "calm", at(2026, 10, 15, 0, 0)),  // pertenece a los últimos 7 días: fuera
        )
        val result = WeekComparison.previousSevenDays(records, now, zone)
        assertEquals(setOf("a", "b"), result.map { it.id }.toSet())
    }

    // ---------- RecordJson (persistencia) ----------

    @Test
    fun `serializar y deserializar conserva todos los campos`() {
        val original = listOf(
            record("id-1", "happiness", 1_700_000_000_000, "Nota con \"comillas\", acentos áéí y emoji 😊"),
            record("id-2", "anger", 1_700_000_100_000),
        )
        assertEquals(original, RecordJson.decode(RecordJson.encode(original)))
    }

    @Test
    fun `json vacio, nulo o corrupto devuelve lista vacia`() {
        assertTrue(RecordJson.decode(null).isEmpty())
        assertTrue(RecordJson.decode("").isEmpty())
        assertTrue(RecordJson.decode("{esto no es json").isEmpty())
    }
}
