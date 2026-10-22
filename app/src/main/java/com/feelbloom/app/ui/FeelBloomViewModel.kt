package com.feelbloom.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.feelbloom.app.data.EmotionRepository
import com.feelbloom.app.domain.Period
import com.feelbloom.app.domain.RecordFilter
import com.feelbloom.app.domain.StatsCalculator
import com.feelbloom.app.domain.WeekComparison
import com.feelbloom.app.model.EmotionCatalog
import com.feelbloom.app.model.EmotionRecord
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID

/**
 * Gestiona el estado de presentación y procesa los eventos de la UI (sección 7.2).
 * Nunca toca DataStore directamente: todo pasa por el [EmotionRepository].
 */
class FeelBloomViewModel(
    private val repository: EmotionRepository,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    /** Estado que no viene del almacenamiento. */
    private data class Local(
        val form: FormState = FormState(),
        val historyExpanded: Boolean = false,
        val historyPeriod: Period = Period.LAST_7_DAYS,
        val historyMonth: YearMonth,
        val statsPeriod: Period = Period.LAST_7_DAYS,
        val statsMonth: YearMonth,
        val message: String? = null,
        val detailRecordId: String? = null,
        val pendingDeleteId: String? = null,
    )

    private val local = MutableStateFlow(
        Local(historyMonth = currentMonth(), statsMonth = currentMonth())
    )

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private var messageJob: Job? = null

    val uiState: StateFlow<FeelBloomUiState> =
        combine(repository.records, local) { records, l -> buildState(records, l) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeelBloomUiState())

    // ---------- Formulario de registro / edición ----------

    fun toggleCreate() {
        val isOpen = local.value.form.mode == FormMode.Create
        _note.value = ""
        local.update {
            it.copy(form = if (isOpen) FormState() else FormState(mode = FormMode.Create))
        }
    }

    fun togglePickToEdit() {
        val isOpen = local.value.form.mode == FormMode.PickToEdit
        _note.value = ""
        local.update {
            it.copy(form = if (isOpen) FormState() else FormState(mode = FormMode.PickToEdit))
        }
    }

    fun closeForm() {
        _note.value = ""
        local.update { it.copy(form = FormState()) }
    }

    fun selectEmotion(emotionId: String) {
        local.update {
            it.copy(form = it.form.copy(selectedEmotionId = emotionId, validationError = null))
        }
    }

    fun onNoteChange(text: String) {
        _note.value = text.take(MAX_NOTE_LENGTH)
    }

    /** Abre el formulario con los datos de un registro existente (RF-06). */
    fun startEdit(recordId: String) {
        val record = uiState.value.records.firstOrNull { it.id == recordId } ?: return
        _note.value = record.note
        local.update {
            it.copy(
                form = FormState(FormMode.Editing(recordId), record.emotionId),
                detailRecordId = null,
            )
        }
    }

    fun submit() {
        val form = local.value.form
        val mode = form.mode
        if (mode != FormMode.Create && mode !is FormMode.Editing) return

        val emotionId = form.selectedEmotionId
        if (emotionId == null) { // RF-04 / RN-02
            local.update {
                it.copy(form = it.form.copy(validationError = "Selecciona una emoción para continuar."))
            }
            return
        }

        val noteText = _note.value.trim() // la nota es opcional (RN-03)
        // Se cierra el formulario antes de lanzar la escritura para que un doble toque
        // no cree registros duplicados (RNF-10).
        closeForm()

        when (mode) {
            FormMode.Create -> {
                val record = EmotionRecord(
                    id = UUID.randomUUID().toString(),
                    emotionId = emotionId,
                    timestamp = clock(), // fecha automática con hora local (RN-04)
                    note = noteText,
                )
                viewModelScope.launch { repository.add(record) }
                showMessage("Tu emoción ha sido registrada <3")
            }
            is FormMode.Editing -> {
                viewModelScope.launch { repository.update(mode.recordId, emotionId, noteText) }
                showMessage("Se ha editado correctamente <3")
            }
            else -> Unit
        }
    }

    // ---------- Detalle y eliminación ----------

    fun showDetail(recordId: String?) {
        local.update { it.copy(detailRecordId = recordId) }
    }

    fun requestDelete(recordId: String) {
        local.update { it.copy(pendingDeleteId = recordId, detailRecordId = null) }
    }

    fun cancelDelete() {
        local.update { it.copy(pendingDeleteId = null) }
    }

    /** Solo se ejecuta tras la confirmación explícita del usuario (RN-10). */
    fun confirmDelete() {
        val id = local.value.pendingDeleteId ?: return
        val editingThis = (local.value.form.mode as? FormMode.Editing)?.recordId == id
        local.update { it.copy(pendingDeleteId = null, detailRecordId = null) }
        if (editingThis) closeForm()
        viewModelScope.launch { repository.delete(id) }
        showMessage("Se ha eliminado correctamente <3")
    }

    // ---------- Historial y estadísticas ----------

    fun setHistoryExpanded(expanded: Boolean) {
        local.update { it.copy(historyExpanded = expanded) }
    }

    fun setHistoryPeriod(period: Period) {
        local.update { it.copy(historyPeriod = period) }
    }

    fun shiftHistoryMonth(delta: Long) {
        local.update { it.copy(historyMonth = shifted(it.historyMonth, delta)) }
    }

    fun setStatsPeriod(period: Period) {
        local.update { it.copy(statsPeriod = period) }
    }

    fun shiftStatsMonth(delta: Long) {
        local.update { it.copy(statsMonth = shifted(it.statsMonth, delta)) }
    }

    // ---------- Internos ----------

    private fun shifted(month: YearMonth, delta: Long): YearMonth {
        val next = month.plusMonths(delta)
        return if (next.isAfter(currentMonth())) month else next
    }

    private fun currentMonth(): YearMonth = YearMonth.from(Instant.ofEpochMilli(clock()).atZone(zone))

    private fun showMessage(text: String) {
        messageJob?.cancel()
        local.update { it.copy(message = text) }
        messageJob = viewModelScope.launch {
            delay(MESSAGE_DURATION_MS)
            local.update { it.copy(message = null) }
        }
    }

    private fun buildState(records: List<EmotionRecord>, l: Local): FeelBloomUiState {
        val now = clock()
        val sorted = records.sortedByDescending { it.timestamp } // más reciente primero (RF-05)

        val historyRecords = RecordFilter.filter(sorted, l.historyPeriod, l.historyMonth, now, zone)
        val statsRecords = RecordFilter.filter(sorted, l.statsPeriod, l.statsMonth, now, zone)
        val stats = StatsCalculator.compute(statsRecords).filter { it.count > 0 }

        val thisWeek = RecordFilter.filter(sorted, Period.LAST_7_DAYS, l.statsMonth, now, zone)
        val lastWeek = WeekComparison.previousSevenDays(sorted, now, zone)

        return FeelBloomUiState(
            isLoading = false,
            records = sorted,
            todayRecords = RecordFilter.today(sorted, now, zone).sortedBy { it.timestamp },
            recentRecords = sorted.take(RECENT_COUNT),
            lastWeekRecords = lastWeek,
            historyExpanded = l.historyExpanded,
            historyPeriod = l.historyPeriod,
            historyMonth = l.historyMonth,
            historyRecords = historyRecords,
            statsPeriod = l.statsPeriod,
            statsMonth = l.statsMonth,
            stats = stats,
            statsTotal = stats.sumOf { it.count },
            topThisWeek = StatsCalculator.predominant(StatsCalculator.compute(thisWeek)).map { it.emotion },
            topLastWeek = StatsCalculator.predominant(StatsCalculator.compute(lastWeek)).map { it.emotion },
            form = l.form,
            message = l.message,
            detailRecord = l.detailRecordId?.let { id -> sorted.firstOrNull { it.id == id } },
            pendingDeleteRecord = l.pendingDeleteId?.let { id -> sorted.firstOrNull { it.id == id } },
        )
    }

    companion object {
        const val MAX_NOTE_LENGTH = 280
        private const val RECENT_COUNT = 3
        private const val MESSAGE_DURATION_MS = 3_000L

        fun factory(repository: EmotionRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { FeelBloomViewModel(repository) }
        }
    }
}
