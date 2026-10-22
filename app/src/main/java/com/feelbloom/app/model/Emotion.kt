package com.feelbloom.app.model

/**
 * Emoción del catálogo predefinido (sección 6.2 de los requerimientos).
 * El color se guarda como ARGB para que el modelo no dependa de Compose.
 */
data class Emotion(
    val id: String,
    val name: String,
    val icon: String,
    val colorArgb: Long,
)

object EmotionCatalog {

    // Colores tomados de la paleta del prototipo de Figma.
    val all: List<Emotion> = listOf(
        Emotion("happiness", "Felicidad", "☺️", 0xFFF6D66B),
        Emotion("calm", "Tranquilidad", "☁️", 0xFFA9D5F2),
        Emotion("love", "Amor", "❤️", 0xFFF3A8C4),
        Emotion("enthusiasm", "Entusiasmo", "😋", 0xFFFFBE8A),
        Emotion("sadness", "Tristeza", "😔", 0xFFAEB8E8),
        Emotion("anger", "Enojo", "😠", 0xFFF28F91),
        Emotion("anxiety", "Ansiedad", "😣", 0xFFC5A9E8),
        Emotion("tiredness", "Cansancio", "🤖", 0xFFB7D4C4),
    )

    private val byId = all.associateBy { it.id }

    fun find(id: String): Emotion? = byId[id]
}
