package com.dailydo.data.models

import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing a mood journal entry
 */
data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val mood: MoodType,
    val emoji: String,
    val notes: String = "",
    val timestamp: Date = Date(),
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
)

/**
 * Enum representing different mood types
 */
enum class MoodType(val value: Int, val label: String, val emoji: String) {
    VERY_HAPPY(5, "Very Happy", "😄"),
    HAPPY(4, "Happy", "😊"),
    NEUTRAL(3, "Neutral", "😐"),
    COOL(4, "Cool", "😎"),
    JOY(5, "Joy", "🥳"),
    ANGRY(2, "Angry", "😠"),
    EXCITED(5, "Excited", "🤩"),
    CALM(4, "Calm", "😌"),
    ANXIOUS(2, "Anxious", "😰"),
    TIRED(2, "Tired", "😴"),
    CONFUSED(2, "Confused", "😕"),
    FRUSTRATED(2, "Frustrated", "😤"),
    LOVED_HEART_EYES(4, "Loved", "😍");

    companion object {
        fun fromValue(value: Int): MoodType {
            return values().find { it.value == value } ?: NEUTRAL
        }
        
        fun fromLabel(label: String): MoodType {
            return values().find { it.label == label } ?: NEUTRAL
        }
        
        fun getAllMoods(): List<MoodType> {
            return listOf(VERY_HAPPY, HAPPY, EXCITED, CALM, NEUTRAL, TIRED, ANXIOUS, COOL, ANGRY, JOY)
        }
    }
}

/**
 * Data class for mood statistics
 */
data class MoodStats(
    val averageMood: Float = 0f,
    val mostFrequentMood: MoodType = MoodType.NEUTRAL,
    val totalEntries: Int = 0,
    val moodCounts: Map<MoodType, Int> = emptyMap(),
    val weeklyTrend: List<Float> = emptyList() // Average mood for each day of the week
)