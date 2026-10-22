# FeelBloom

Aplicación Android para registrar emociones diarias y ver con qué frecuencia aparece cada una.
No analiza ni interpreta nada: solo registra, cuenta y calcula porcentajes. Todo se guarda en el dispositivo.

**Stack:** Kotlin · Jetpack Compose (Material 3) · MVVM + `StateFlow` · Preferences DataStore · minSdk 26 (Android 8).
Sin Room, SQLite, servicios web ni permisos.

## Cómo abrirla

1. Abre la carpeta `FeelBloom` con **Android Studio** (Ladybug o más reciente) y espera la sincronización de Gradle.
2. Usa JDK 17 (el que trae Android Studio sirve).
3. Ejecuta `app` en un emulador o dispositivo con Android 8 o superior.
4. Pruebas unitarias: `./gradlew test`.

> Si Gradle avisa de que `compileSdk 35` no está probado con AGP 8.5.2, es solo una advertencia.
> Para quitarla agrega `android.suppressUnsupportedCompileSdk=35` a `gradle.properties` o sube AGP a 8.7+.

## Estructura (MVVM)

```
app/src/main/java/com/feelbloom/app/
├── FeelBloomApp.kt · MainActivity.kt
├── model/    Emotion (catálogo de 8 emociones) · EmotionRecord
├── data/     EmotionRepository (interfaz) · DataStoreEmotionRepository · RecordJson
├── domain/   Period · RecordFilter · StatsCalculator · WeekComparison   (lógica pura, con pruebas)
└── ui/
    ├── FeelBloomViewModel · FeelBloomUiState   estado expuesto con StateFlow
    ├── FeelBloomRoot       menú lateral, navegación y diálogos
    ├── screens/            Home · History · Stats
    ├── components/         tarjetas, botones, selector de periodo…
    └── theme/              paleta pastel de Figma + fuente Inika
```

Flujo: pantalla → evento al `FeelBloomViewModel` → `EmotionRepository` → DataStore → el `Flow` de registros
actualiza el `StateFlow` → Compose recompone. La UI nunca toca DataStore.

Los registros se guardan como un arreglo JSON en una sola preferencia (`RecordJson`, con `org.json` de Android,
sin dependencias nuevas).

## Dónde está cada requerimiento

| Requerimiento | Dónde |
|---|---|
| RF-01 Pantalla principal | `HomeScreen` |
| RF-02 Tarjetas de emociones | `EmotionCatalog` + `EmotionTile` |
| RF-03/04 Registrar y validar | `FeelBloomViewModel.submit()` |
| RF-05 Historial (más reciente primero) | `HistoryScreen`, orden en `buildState` |
| RF-06 Editar conservando id y fecha | `DataStoreEmotionRepository.update` |
| RF-07 / RN-10 Eliminar con confirmación | diálogo en `FeelBloomRoot` |
| RF-08 Filtro por periodo | `Period`, `RecordFilter`, `PeriodSelector` |
| RF-09/10 Estadísticas | `StatsCalculator`, `StatsScreen` |
| RF-11/12 Actualización y persistencia | `Flow` de DataStore → `StateFlow` |
| RF-13 Navegación | menú lateral en `FeelBloomRoot` |
| RNF-09 Privacidad | sin permisos ni red, `allowBackup=false` |

## Decisiones frente al prototipo de Figma

- **Texto de los botones oscuro** en lugar de blanco: blanco sobre `#B8A1E3` no cumple el contraste de RNF-11.
  Se cambia en `FeelColors.OnButton` (`ui/theme/Theme.kt`).
- **Botones y chips de 36 dp** de alto (en Figma 22 dp) para tener un área táctil razonable.
- **Icono del menú:** el icono de Figma no se pudo descargar; se usa el icono de menú de Material.
- **Eliminar:** el prototipo tiene una pantalla "Seleccione el registro que desea eliminar" sin botón de entrada.
  Aquí se elimina desde el formulario de edición (botón *Eliminar*) o tocando un registro del Historial.
- **Registros de días anteriores:** se editan o eliminan tocándolos en el Historial; "Editar registro" en Inicio
  lista solo los de hoy, como en el prototipo.
- **Filtro por periodo** (RF-08) aparece en "Ver registro completo" del Historial y en Estadísticas.
  Figma no lo dibuja, pero el requerimiento lo pide.

## Fuente

Inika (Google Fonts, licencia SIL OFL) en `res/font/`.
