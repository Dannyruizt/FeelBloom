package com.feelbloom.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feelbloom.app.domain.Period
import com.feelbloom.app.model.Emotion
import com.feelbloom.app.ui.theme.FeelColors
import com.feelbloom.app.ui.theme.Inika
import com.feelbloom.app.ui.util.Formatting
import java.time.YearMonth

fun Emotion.color(): Color = Color(colorArgb)

/** Barra superior: menú a la izquierda y nombre de la app centrado. */
@Composable
fun FeelTopBar(onMenuClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(FeelColors.Header)
            .statusBarsPadding()
            .height(50.dp)
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp),
        ) {
            Icon(
                Icons.Filled.Menu,
                contentDescription = "Abrir menú",
                tint = FeelColors.Text,
            )
        }
        Text(
            "FeelBloom ❀",
            modifier = Modifier.align(Alignment.Center),
            color = FeelColors.OnPink,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = FeelColors.Button,
) {
    Box(
        modifier
            .heightIn(min = 36.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 12.sp, color = FeelColors.OnButton, maxLines = 1)
    }
}

/** Tarjeta cuadrada de color con icono y nombre de la emoción (RF-02, RNF-11). */
@Composable
fun EmotionTile(
    emotion: Emotion,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    var tile = modifier
        .defaultMinSize(minWidth = 72.dp, minHeight = 72.dp)
        .background(emotion.color())
    if (selected) tile = tile.border(3.dp, FeelColors.Text)
    if (onClick != null) {
        tile = tile.selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
    }
    Column(
        tile.padding(horizontal = 2.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(emotion.icon, fontSize = 28.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(2.dp))
        Text(emotion.name, fontSize = 11.sp, maxLines = 1, softWrap = false)
    }
}

/** Píldora con el nombre y el icono de la emoción, usada en "Tus emociones de hoy". */
@Composable
fun EmotionPill(
    emotion: Emotion,
    modifier: Modifier = Modifier,
    background: Color = FeelColors.Record,
    label: String = emotion.name,
    onClick: (() -> Unit)? = null,
) {
    var pill = modifier
        .fillMaxWidth()
        .widthIn(max = 203.dp)
        .heightIn(min = 32.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(background)
    if (onClick != null) pill = pill.clickable(role = Role.Button, onClick = onClick)
    Row(
        pill.padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 12.sp)
        Text(emotion.icon, fontSize = 14.sp)
    }
}

/** Cuadrícula de [columns] columnas; la última fila incompleta conserva el ancho de las celdas. */
@Composable
fun <T> TileGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    columns: Int = 3,
    cell: @Composable (T) -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { item ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { cell(item) }
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** Campo de nota opcional. */
@Composable
fun NoteField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(fontFamily = Inika, fontSize = 14.sp, color = FeelColors.Text),
        cursorBrush = SolidColor(FeelColors.Text),
        maxLines = 3,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Nota opcional" }
            .background(Color.White, RoundedCornerShape(20.dp)),
        decorationBox = { inner ->
            Box(
                Modifier
                    .heightIn(min = 40.dp)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart,
            ) { inner() }
        },
    )
}

/** Aviso temporal (color de "notificaciones"). Se anuncia a los lectores de pantalla. */
@Composable
fun Banner(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(FeelColors.Notification)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Text(text, fontSize = 12.sp)
    }
}

@Composable
private fun SelectorChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .heightIn(min = 36.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) FeelColors.Button else Color.White)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 12.sp, color = FeelColors.Text)
    }
}

/** Selector de periodo (RF-08): últimos 7 días, mes seleccionado o todo el historial. */
@Composable
fun PeriodSelector(
    period: Period,
    month: YearMonth,
    onPeriodChange: (Period) -> Unit,
    onMonthShift: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Period.entries.forEach { p ->
                SelectorChip(p.label, selected = p == period, onClick = { onPeriodChange(p) })
            }
        }
        if (period == Period.MONTH) {
            val isCurrent = month == YearMonth.now()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .clickable(role = Role.Button, onClick = { onMonthShift(-1) })
                        .semantics { contentDescription = "Mes anterior" },
                    contentAlignment = Alignment.Center,
                ) { Text("‹", fontSize = 22.sp) }
                Text(Formatting.month(month), fontSize = 14.sp)
                Box(
                    Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .then(
                            if (isCurrent) Modifier
                            else Modifier.clickable(role = Role.Button, onClick = { onMonthShift(1) })
                        )
                        .semantics { contentDescription = "Mes siguiente" },
                    contentAlignment = Alignment.Center,
                ) { Text("›", fontSize = 22.sp, color = if (isCurrent) Color.Gray else FeelColors.Text) }
            }
        }
    }
}
