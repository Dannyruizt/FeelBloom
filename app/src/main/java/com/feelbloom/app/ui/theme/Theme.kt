package com.feelbloom.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.feelbloom.app.R

/** Colores de la paleta del prototipo de Figma ("Paleta de colores"). */
object FeelColors {
    val Background = Color(0xFFFFF9F5)
    val Header = Color(0xFFF4B8CC)       // encabezado y tarjetas principales
    val Button = Color(0xFFB8A1E3)       // botones
    val CardMint = Color(0xFFB9E4D0)     // algunas tarjetas
    val Notification = Color(0xFFF8E4A6) // notificaciones
    val Text = Color(0xFF493B5A)         // texto
    val Record = Color.White             // registros
    val Form = Color(0xFFDEF2FF)         // formularios

    /** Texto sobre superficies rosas, como en el prototipo. */
    val OnPink = Color.White

    /** Texto de los botones: oscuro para cumplir el contraste de RNF-11 (en Figma es blanco). */
    val OnButton = Text
}

val Inika = FontFamily(
    Font(R.font.inika_regular, FontWeight.Normal),
    Font(R.font.inika_bold, FontWeight.Bold),
)

private val FeelColorScheme = lightColorScheme(
    primary = FeelColors.Button,
    onPrimary = FeelColors.OnButton,
    secondary = FeelColors.Header,
    onSecondary = FeelColors.Text,
    background = FeelColors.Background,
    onBackground = FeelColors.Text,
    surface = FeelColors.Background,
    onSurface = FeelColors.Text,
)

private fun Typography.withFontFamily(family: FontFamily) = copy(
    displayLarge = displayLarge.copy(fontFamily = family),
    displayMedium = displayMedium.copy(fontFamily = family),
    displaySmall = displaySmall.copy(fontFamily = family),
    headlineLarge = headlineLarge.copy(fontFamily = family),
    headlineMedium = headlineMedium.copy(fontFamily = family),
    headlineSmall = headlineSmall.copy(fontFamily = family),
    titleLarge = titleLarge.copy(fontFamily = family),
    titleMedium = titleMedium.copy(fontFamily = family),
    titleSmall = titleSmall.copy(fontFamily = family),
    bodyLarge = bodyLarge.copy(fontFamily = family),
    bodyMedium = bodyMedium.copy(fontFamily = family),
    bodySmall = bodySmall.copy(fontFamily = family),
    labelLarge = labelLarge.copy(fontFamily = family),
    labelMedium = labelMedium.copy(fontFamily = family),
    labelSmall = labelSmall.copy(fontFamily = family),
)

/** Tema claro con colores pastel (sección 2.3). No hay variante oscura. */
@Composable
fun FeelBloomTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FeelColorScheme,
        typography = Typography().withFontFamily(Inika),
    ) {
        ProvideTextStyle(TextStyle(fontFamily = Inika, color = FeelColors.Text), content)
    }
}
