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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feelbloom.app.model.EmotionCatalog
import com.feelbloom.app.ui.FeelBloomUiState
import com.feelbloom.app.ui.FormMode
import com.feelbloom.app.ui.FormState
import com.feelbloom.app.ui.components.Banner
import com.feelbloom.app.ui.components.EmotionPill
import com.feelbloom.app.ui.components.EmotionTile
import com.feelbloom.app.ui.components.NoteField
import com.feelbloom.app.ui.components.PillButton
import com.feelbloom.app.ui.components.color
import com.feelbloom.app.ui.theme.FeelColors
import com.feelbloom.app.ui.util.Formatting

class HomeActions(
    val onRegisterClick: () -> Unit,
    val onEditClick: () -> Unit,
    val onSelectEmotion: (String) -> Unit,
    val onNoteChange: (String) -> Unit,
    val onSubmit: () -> Unit,
    val onCancel: () -> Unit,
    val onPickRecord: (String) -> Unit,
    val onDelete: (String) -> Unit,
)

/** Inicio / registrar (RF-01, RF-02, RF-03, RF-04, RF-06). */
@Composable
fun HomeScreen(
    state: FeelBloomUiState,
    note: String,
    actions: HomeActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            "¡Bienvenido/a! <3\nObserva como florecen tus emociones :3",
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 0.dp),
        )
        Spacer(Modifier.height(24.dp))

        TodayCard(state)
        Spacer(Modifier.height(14.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            PillButton("+ Registrar emoción", actions.onRegisterClick)
            PillButton("Editar registro", actions.onEditClick)
        }
        Spacer(Modifier.height(16.dp))

        when (val mode = state.form.mode) {
            FormMode.Hidden -> Unit
            FormMode.Create -> EmotionForm(
                title = "¿Qué emoción estás sintiendo?",
                subtitle = null,
                form = state.form,
                note = note,
                submitLabel = "Registrar",
                actions = actions,
                deleteId = null,
            )
            FormMode.PickToEdit -> PickRecordPanel(state, actions)
            is FormMode.Editing -> {
                val record = state.records.firstOrNull { it.id == mode.recordId }
                EmotionForm(
                    title = "¿Qué emoción estás sintiendo?",
                    subtitle = record?.let { "Registro del ${Formatting.dateTime(it.timestamp)}" },
                    form = state.form,
                    note = note,
                    submitLabel = "Guardar",
                    actions = actions,
                    deleteId = mode.recordId,
                )
            }
        }

        state.message?.let {
            Spacer(Modifier.height(12.dp))
            Banner(it)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TodayCard(state: FeelBloomUiState) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(FeelColors.Header)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Tus emociones de hoy ❀", fontSize = 14.sp, color = FeelColors.OnPink)
        when {
            state.isLoading -> Text("Cargando…", fontSize = 12.sp, color = FeelColors.OnPink)
            state.todayRecords.isEmpty() -> Text(
                "Todavía no registras emociones hoy.",
                fontSize = 12.sp,
                color = FeelColors.OnPink,
            )
            else -> state.todayRecords.forEach { record ->
                EmotionCatalog.find(record.emotionId)?.let { EmotionPill(it) }
            }
        }
    }
}

@Composable
private fun PickRecordPanel(state: FeelBloomUiState, actions: HomeActions) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(FeelColors.Form)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Seleccione el registro que desea editar:", fontSize = 14.sp)
        if (state.todayRecords.isEmpty()) {
            Text(
                "Hoy no tienes registros para editar. Los de días anteriores se editan desde el Historial.",
                fontSize = 12.sp,
            )
        } else {
            state.todayRecords.forEach { record ->
                EmotionCatalog.find(record.emotionId)?.let { emotion ->
                    EmotionPill(
                        emotion = emotion,
                        background = emotion.color(),
                        label = "${emotion.name} · ${Formatting.time(record.timestamp)}",
                        onClick = { actions.onPickRecord(record.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmotionForm(
    title: String,
    subtitle: String?,
    form: FormState,
    note: String,
    submitLabel: String,
    actions: HomeActions,
    deleteId: String?,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(FeelColors.Form)
            .padding(horizontal = 19.dp, vertical = 13.dp),
    ) {
        Text(title, fontSize = 14.sp)
        subtitle?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, fontSize = 12.sp)
        }
        Spacer(Modifier.height(16.dp))

        EmotionCatalog.all.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                row.forEach { emotion ->
                    EmotionTile(
                        emotion = emotion,
                        selected = emotion.id == form.selectedEmotionId,
                        onClick = { actions.onSelectEmotion(emotion.id) },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        form.validationError?.let {
            Text(
                it,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(EmotionCatalog.all.first { e -> e.id == "anger" }.color())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
            Spacer(Modifier.height(12.dp))
        }

        Text("Agregar nota (opcional):", fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        NoteField(value = note, onValueChange = actions.onNoteChange)
        Spacer(Modifier.height(16.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            if (deleteId != null) {
                PillButton(
                    "Eliminar",
                    onClick = { actions.onDelete(deleteId) },
                    containerColor = FeelColors.Header,
                )
            }
            PillButton("Cancelar", actions.onCancel, containerColor = FeelColors.Record)
            PillButton(submitLabel, actions.onSubmit)
        }
    }
}
