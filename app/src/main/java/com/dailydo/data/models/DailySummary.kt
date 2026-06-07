package com.dailydo.data.models

import java.util.Date

data class DailySummary(
    val date: String, // yyyy-MM-dd
    val habitsPercent: Float,
    val moodPercent: Float,
    val hydrationPercent: Float,
    val generatedAt: Date = Date()
)


