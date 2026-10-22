package com.feelbloom.app.ui

import com.feelbloom.app.domain.EmotionStat
import com.feelbloom.app.domain.Period
import com.feelbloom.app.model.Emotion
import com.feelbloom.app.model.EmotionRecord
import java.time.YearMonth

/** Qué muestra el panel de formulario de la pantalla de inicio. */
sealed interface FormMode {
    data object Hidden : FormMode
    data object Create : FormMode
    data object PickToEdit : FormMode
    data class Editing(val recordId: String) : FormMode
}

data class FormState(
    val mode: FormMode = FormMode.Hidden,
    val selectedEmotionId: String? = null,
    val validationError: String? = null,
)

/**
 * Estado de presentación (sección 6.3). El texto de la nota se expone aparte
 * ([FeelBloomViewModel.note]) para que el campo de texto se actualice de forma síncrona.
 */
data class FeelBloomUiState(
    val isLoading: Boolean = true,
    val records: List<EmotionRecord> = emptyList(),
    val todayRecords: List<EmotionRecord> = emptyList(),

    // Historial
    val recentRecords: List<EmotionRecord> = emptyList(),
    val lastWeekRecords: List<EmotionRecord> = emptyList(),
    val historyExpanded: Boolean = false,
    val historyPeriod: Period = Period.LAST_7_DAYS,
    val historyMonth: YearMonth = YearMonth.now(),
    val historyRecords: List<EmotionRecord> = emptyList(),

    // Estadísticas
    val statsPeriod: Period = Period.LAST_7_DAYS,
    val statsMonth: YearMonth = YearMonth.now(),
    val stats: List<EmotionStat> = emptyList(),
    val statsTotal: Int = 0,
    val topThisWeek: List<Emotion> = emptyList(),
    val topLastWeek: List<Emotion> = emptyList(),

    // Interacción
    val form: FormState = FormState(),
    val message: String? = null,
    val detailRecord: EmotionRecord? = null,
    val pendingDeleteRecord: EmotionRecord? = null,
)
