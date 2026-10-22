package com.feelbloom.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feelbloom.app.model.EmotionCatalog
import com.feelbloom.app.ui.components.FeelTopBar
import com.feelbloom.app.ui.screens.HistoryActions
import com.feelbloom.app.ui.screens.HistoryScreen
import com.feelbloom.app.ui.screens.HomeActions
import com.feelbloom.app.ui.screens.HomeScreen
import com.feelbloom.app.ui.screens.StatsActions
import com.feelbloom.app.ui.screens.StatsScreen
import com.feelbloom.app.ui.theme.FeelColors
import com.feelbloom.app.ui.util.Formatting
import kotlinx.coroutines.launch

/** Secciones principales (RF-13). */
enum class Screen(val label: String) {
    HOME("Inicio/registrar"),
    HISTORY("Historial"),
    STATS("Estadísticas"),
}

@Composable
fun FeelBloomRoot(viewModel: FeelBloomViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val note by viewModel.note.collectAsStateWithLifecycle()

    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }
    BackHandler(enabled = !drawerState.isOpen && screen != Screen.HOME) { screen = Screen.HOME }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(220.dp),
                drawerShape = RectangleShape,
                drawerContainerColor = FeelColors.Header,
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 16.dp)) {
                    Text(
                        "Menú",
                        modifier = Modifier.padding(start = 5.dp, bottom = 12.dp),
                        color = FeelColors.OnPink,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Screen.entries.forEach { target ->
                        DrawerItem(
                            label = target.label,
                            selected = target == screen,
                            onClick = {
                                screen = target
                                scope.launch { drawerState.close() }
                            },
                        )
                    }
                }
            }
        },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(FeelColors.Background)
        ) {
            FeelTopBar(onMenuClick = { scope.launch { drawerState.open() } })

            Box(
                Modifier
                    .weight(1f)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                when (screen) {
                    Screen.HOME -> HomeScreen(
                        state = state,
                        note = note,
                        actions = HomeActions(
                            onRegisterClick = viewModel::toggleCreate,
                            onEditClick = viewModel::togglePickToEdit,
                            onSelectEmotion = viewModel::selectEmotion,
                            onNoteChange = viewModel::onNoteChange,
                            onSubmit = viewModel::submit,
                            onCancel = viewModel::closeForm,
                            onPickRecord = viewModel::startEdit,
                            onDelete = viewModel::requestDelete,
                        ),
                    )
                    Screen.HISTORY -> HistoryScreen(
                        state = state,
                        actions = HistoryActions(
                            onToggleExpanded = viewModel::setHistoryExpanded,
                            onPeriodChange = viewModel::setHistoryPeriod,
                            onMonthShift = viewModel::shiftHistoryMonth,
                            onRecordClick = viewModel::showDetail,
                        ),
                    )
                    Screen.STATS -> StatsScreen(
                        state = state,
                        actions = StatsActions(
                            onPeriodChange = viewModel::setStatsPeriod,
                            onMonthShift = viewModel::shiftStatsMonth,
                        ),
                    )
                }
            }
        }
    }

    // Detalle de un registro del historial: permite editarlo o eliminarlo.
    state.detailRecord?.let { record ->
        val emotion = EmotionCatalog.find(record.emotionId)
        AlertDialog(
            onDismissRequest = { viewModel.showDetail(null) },
            containerColor = FeelColors.Background,
            title = { Text("${emotion?.icon.orEmpty()} ${emotion?.name.orEmpty()}") },
            text = {
                Column {
                    Text(Formatting.dateTime(record.timestamp), fontSize = 14.sp)
                    if (record.note.isNotBlank()) {
                        Text(record.note, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            },
            confirmButton = {
                DialogButton("Editar") {
                    viewModel.startEdit(record.id)
                    screen = Screen.HOME
                }
            },
            dismissButton = { DialogButton("Eliminar") { viewModel.requestDelete(record.id) } },
        )
    }

    // Confirmación explícita antes de eliminar (RF-07, RN-10).
    state.pendingDeleteRecord?.let {
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            containerColor = FeelColors.Background,
            title = { Text("¿Eliminar registro?") },
            text = { Text("Se quitará esta emoción de tu historial. No se puede deshacer.") },
            confirmButton = { DialogButton("Eliminar", viewModel::confirmDelete) },
            dismissButton = { DialogButton("Cancelar", viewModel::cancelDelete) },
        )
    }
}

@Composable
private fun DrawerItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color.White.copy(alpha = 0.35f) else Color.Transparent)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(label, color = FeelColors.OnPink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DialogButton(text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = FeelColors.Text),
    ) { Text(text) }
}
