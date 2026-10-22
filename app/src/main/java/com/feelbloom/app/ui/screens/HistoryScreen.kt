package com.feelbloom.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feelbloom.app.domain.Period
import com.feelbloom.app.model.EmotionCatalog
import com.feelbloom.app.model.EmotionRecord
import com.feelbloom.app.ui.FeelBloomUiState
import com.feelbloom.app.ui.components.EmotionTile
import com.feelbloom.app.ui.components.PeriodSelector
import com.feelbloom.app.ui.components.PillButton
import com.feelbloom.app.ui.components.TileGrid
import com.feelbloom.app.ui.components.color
import com.feelbloom.app.ui.theme.FeelColors
import com.feelbloom.app.ui.util.Formatting
import androidx.compose.ui.text.font.FontWeight

class HistoryActions(
    val onToggleExpanded: (Boolean) -> Unit,
    val onPeriodChange: (Period) -> Unit,
    val onMonthShift: (Long) -> Unit,
    val onRecordClick: (String) -> Unit,
)

/**
 * Historial (RF-05, RF-08). Vista resumida como en el prototipo: registros recientes y
 * la semana pasada; "Ver registro completo" muestra la lista filtrable por periodo.
 */
@Composable
fun HistoryScreen(
    state: FeelBloomUiState,
    actions: HistoryActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text("Historial <3", fontSize = 14.sp)
        Spacer(Modifier.height(18.dp))

        if (state.historyExpanded) {
            FullHistory(state, actions)
        } else {
            SummaryHistory(state, actions)
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            PillButton(
                text = if (state.historyExpanded) "Ver menos" else "Ver registro completo",
                onClick = { actions.onToggleExpanded(!state.historyExpanded) },
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryHistory(state: FeelBloomUiState, actions: HistoryActions) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(FeelColors.Header)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Registros recientes ❀", fontSize = 14.sp, color = FeelColors.OnPink)
        if (state.recentRecords.isEmpty()) {
            Text("Aún no hay registros.", fontSize = 12.sp, color = FeelColors.OnPink)
        } else {
            RecordTiles(state.recentRecords, actions)
        }
    }

    Spacer(Modifier.height(28.dp))

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(FeelColors.CardMint)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("La semana pasada", fontSize = 14.sp)
        if (state.lastWeekRecords.isEmpty()) {
            Text("No hay registros de la semana pasada.", fontSize = 12.sp)
        } else {
            RecordTiles(state.lastWeekRecords.take(SUMMARY_LIMIT), actions)
            if (state.lastWeekRecords.size > SUMMARY_LIMIT) {
                Text(
                    "Y ${state.lastWeekRecords.size - SUMMARY_LIMIT} más en el registro completo.",
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun RecordTiles(records: List<EmotionRecord>, actions: HistoryActions) {
    TileGrid(records) { record ->
        EmotionCatalog.find(record.emotionId)?.let { emotion ->
            EmotionTile(emotion, onClick = { actions.onRecordClick(record.id) })
        }
    }
}

@Composable
private fun FullHistory(state: FeelBloomUiState, actions: HistoryActions) {
    PeriodSelector(
        period = state.historyPeriod,
        month = state.historyMonth,
        onPeriodChange = actions.onPeriodChange,
        onMonthShift = actions.onMonthShift,
    )
    Spacer(Modifier.height(14.dp))

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(FeelColors.CardMint)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            when (state.historyPeriod) {
                Period.LAST_7_DAYS -> "Últimos 7 días"
                Period.MONTH -> Formatting.month(state.historyMonth)
                Period.ALL -> "Todo el historial"
            },
            fontSize = 14.sp,
        )
        if (state.historyRecords.isEmpty()) {
            Text("No hay registros en este periodo.", fontSize = 12.sp)
        } else {
            state.historyRecords.forEach { record ->
                RecordRow(record) { actions.onRecordClick(record.id) }
            }
        }
    }
}

@Composable
private fun RecordRow(record: EmotionRecord, onClick: () -> Unit) {
    val emotion = EmotionCatalog.find(record.emotionId) ?: return
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(emotion.color())
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(emotion.icon, fontSize = 28.sp)
        Column(Modifier.weight(1f)) {
            Text(emotion.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(Formatting.dateTime(record.timestamp), fontSize = 12.sp)
            if (record.note.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(record.note, fontSize = 12.sp)
            }
        }
    }
}

private const val SUMMARY_LIMIT = 6
