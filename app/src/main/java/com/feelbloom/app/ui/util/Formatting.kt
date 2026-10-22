package com.feelbloom.app.ui.util

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.format.TextStyle as JavaTextStyle

object Formatting {
    private val locale = Locale("es", "MX")
    private val dateTimeFormat = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", locale)
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm", locale)

    fun dateTime(timestamp: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        dateTimeFormat.format(Instant.ofEpochMilli(timestamp).atZone(zone))

    fun time(timestamp: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        timeFormat.format(Instant.ofEpochMilli(timestamp).atZone(zone))

    fun month(month: YearMonth): String =
        month.month.getDisplayName(JavaTextStyle.FULL, locale)
            .replaceFirstChar { it.uppercase() } + " " + month.year

    fun percent(value: Double): String = String.format(locale, "%.1f%%", value)
}
