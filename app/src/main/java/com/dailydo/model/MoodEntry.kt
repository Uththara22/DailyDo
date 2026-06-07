package com.dailydo.model

import java.util.Date

data class MoodEntry(
    val id: Long = 0,
    val moodType: MoodType,
    val note: String? = null,
    val timestamp: Date = Date(),
    val emoji: String
)

enum class MoodType(val displayName: String, val emoji: String, val colorRes: Int) {
    VERY_HAPPY("Great", "😄", android.R.color.holo_green_light),
    HAPPY("Good", "😊", android.R.color.holo_green_dark),
    NEUTRAL("Okay", "😐", android.R.color.holo_orange_light),
    SAD("Sad", "😔", android.R.color.holo_orange_dark),
    VERY_SAD("Down", "😢", android.R.color.holo_red_light),
    ANXIOUS("Anxious", "😰", android.R.color.holo_red_dark)
}
