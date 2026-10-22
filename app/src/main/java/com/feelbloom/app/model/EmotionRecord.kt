package com.feelbloom.app.model

/**
 * Registro emocional (sección 6.1). Cada entrada es independiente (RN-01)
 * y conserva su [id] aunque se edite (RN-05).
 *
 * @param timestamp instante en milisegundos desde epoch; se asigna al crear (RN-04).
 */
data class EmotionRecord(
    val id: String,
    val emotionId: String,
    val timestamp: Long,
    val note: String = "",
)
