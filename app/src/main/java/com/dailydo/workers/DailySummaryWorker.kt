package com.dailydo.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dailydo.data.models.DailySummary
import com.dailydo.data.repository.SharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DailySummaryWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    override fun doWork(): Result {
        return try {
            val prefs = SharedPreferencesManager.getInstance(applicationContext)
            val habits = prefs.getHabits()
            val habitProgress = habits.associate { habit ->
                habit.id to prefs.getHabitProgressForHabit(habit.id)
            }
            val moodEntries = prefs.getMoodEntries()
            val hydrationData = prefs.getHydrationData()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val date = dateFormat.format(cal.time)

            // Reuse calculations from ProgressFragment logic
            val habitsPercent = calculateHabitsYesterday(habits.map { it }, habitProgress)
            val moodPercent = calculateMoodYesterday(moodEntries)
            val hydrationPercent = calculateHydrationYesterday(hydrationData)

            prefs.saveDailySummary(
                DailySummary(
                    date = date,
                    habitsPercent = habitsPercent,
                    moodPercent = moodPercent,
                    hydrationPercent = hydrationPercent
                )
            )

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun calculateHabitsYesterday(
        habits: List<com.dailydo.data.models.Habit>,
        habitProgress: Map<String, List<com.dailydo.data.models.HabitProgress>>
    ): Float {
        if (habits.isEmpty()) return 0f
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(calendar.time)
        var completedCount = 0
        habits.forEach { habit ->
            val progressList = habitProgress[habit.id] ?: emptyList()
            val dayProgress = progressList.find { it.date == dateString }
            if (dayProgress?.isCompleted == true) {
                completedCount++
            }
        }
        return ((completedCount.toFloat() / habits.size.toFloat()) * 100f).coerceIn(0f, 100f)
    }

    private fun calculateMoodYesterday(
        moodEntries: List<com.dailydo.data.models.MoodEntry>
    ): Float {
        if (moodEntries.isEmpty()) return 0f
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(calendar.time)
        val dayMoods = moodEntries.filter { it.date == dateString }
        if (dayMoods.isEmpty()) return 0f
        val averageMood = dayMoods.map { it.mood.value }.average().toFloat()
        return (((averageMood - 1f) / 4f) * 100f).coerceIn(0f, 100f)
    }

    private fun calculateHydrationYesterday(
        hydrationData: List<com.dailydo.data.models.DailyHydration>
    ): Float {
        if (hydrationData.isEmpty()) return 0f
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(calendar.time)
        val dayHydration = hydrationData.find { it.date == dateString }
        if (dayHydration == null || dayHydration.goalMl <= 0) return 0f
        return (((dayHydration.totalIntakeMl.toFloat() / dayHydration.goalMl.toFloat()) * 100f)
            .coerceAtMost(100f))
    }
}


