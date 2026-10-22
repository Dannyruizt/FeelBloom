package com.feelbloom.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feelbloom.app.domain.EmotionStat
import com.feelbloom.app.domain.Period
import com.feelbloom.app.model.Emotion
import com.feelbloom.app.ui.FeelBloomUiState
import com.feelbloom.app.ui.components.EmotionTile
import com.feelbloom.app.ui.components.PeriodSelector
import com.feelbloom.app.ui.components.TileGrid
import com.feelbloom.app.ui.components.color
import com.feelbloom.app.ui.theme.FeelColors
import com.feelbloom.app.ui.util.Formatting

class StatsActions(
    val onPeriodChange: (Period) -> Unit,
    val onMonthShift: (Long) -> Unit,
)

/**
 * Estadísticas (RF-09, RF-10). Solo muestra cantidades y porcentajes; no interpreta
 * los resultados ni los califica (RN-08).
 */
@Composable
fun StatsScreen(
    state: FeelBloomUiState,
    actions: StatsActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text("Estadísticas <3", fontSize = 14.sp)
        Spacer(Modifier.height(18.dp))

        PredominantCard(
            title = "Tu emoción predominante de la semana fue ❀",
            emptyText = "Sin registros en los últimos 7 días.",
            emotions = state.topThisWeek,
            background = FeelColors.Header,
            textColor = FeelColors.OnPink,
            shape = RoundedCornerShape(30.dp),
        )
        Spacer(Modifier.height(22.dp))
        PredominantCard(
            title = "Tu emoción predominante de la semana pasada fue:",
            emptyText = "Sin registros la semana pasada.",
            emotions = state.topLastWeek,
            background = FeelColors.CardMint,
            textColor = FeelColors.Text,
            shape = RoundedCornerShape(20.dp),
        )
        Spacer(Modifier.height(22.dp))

        FrequencyCard(state, actions)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PredominantCard(
    title: String,
    emptyText: String,
    emotions: List<Emotion>,
    background: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    shape: androidx.compose.ui.graphics.Shape,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(title, fontSize = 14.sp, color = textColor)
        if (emotions.isEmpty()) {
            Text(emptyText, fontSize = 12.sp, color = textColor)
        } else {
            TileGrid(emotions) { EmotionTile(it) }
        }
    }
}

@Composable
private fun FrequencyCard(state: FeelBloomUiState, actions: StatsActions) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(FeelColors.Form)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Frecuencia de tus emociones ❀", fontSize = 14.sp)
        PeriodSelector(
            period = state.statsPeriod,
            month = state.statsMonth,
            onPeriodChange = actions.onPeriodChange,
            onMonthShift = actions.onMonthShift,
        )

        if (state.stats.isEmpty()) {
            // RF-10: mensaje informativo en lugar de resultados engañosos.
            Text("Aún no hay registros en este periodo.", fontSize = 12.sp)
        } else {
            Text(
                "Total de registros: ${state.statsTotal}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            state.stats.forEach { StatCard(it) }
        }
    }
}

@Composable
private fun StatCard(stat: EmotionStat) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(stat.emotion.color())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stat.emotion.icon, fontSize = 24.sp)
        Text(stat.emotion.name, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                if (stat.count == 1) "1 registro" else "${stat.count} registros",
                fontSize = 12.sp,
            )
            Text(Formatting.percent(stat.percent), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
