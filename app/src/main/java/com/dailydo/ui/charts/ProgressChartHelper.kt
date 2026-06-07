package com.dailydo.ui.charts

import android.content.Context
import com.dailydo.R
import com.dailydo.data.models.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class for creating 14-day progress charts
 * Shows comprehensive wellness data over the last 14 days
 */
class ProgressChartHelper(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    
    /**
     * Setup a comprehensive 14-day progress chart showing habits, mood, and hydration
     */
    fun setupProgressChart(
        chart: LineChart, 
        habits: List<Habit>,
        habitProgress: Map<String, List<HabitProgress>>,
        moodEntries: List<MoodEntry>,
        hydrationData: List<DailyHydration>
    ) {
        // Configure chart appearance
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
            setBackgroundColor(context.getColor(R.color.card_background))
            
            // Configure X-axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelCount = 7
                textColor = context.getColor(R.color.text_secondary)
                textSize = 10f
                setLabelCount(7, true)
            }
            
            // Configure Y-axis
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 100f
                setDrawGridLines(true)
                textColor = context.getColor(R.color.text_secondary)
                textSize = 10f
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}%"
                    }
                }
            }
            
            axisRight.isEnabled = false
            
            // Configure legend
            legend.apply {
                isEnabled = true
                textColor = context.getColor(R.color.text_primary)
                textSize = 12f
                formSize = 12f
            }
        }
        
        // Prepare data for the last 14 days
        val chartData = prepareProgressData(habits, habitProgress, moodEntries, hydrationData)
        
        if (chartData.isNotEmpty()) {
            val dataSets = mutableListOf<LineDataSet>()
            
            // Habits completion rate line
            val habitsData = chartData.map { Entry(it.dayIndex.toFloat(), it.habitsCompletionRate) }
            val habitsDataSet = LineDataSet(habitsData, "Habits").apply {
                color = context.getColor(R.color.primary_green)
                setCircleColor(context.getColor(R.color.primary_green))
                lineWidth = 3f
                circleRadius = 4f
                setDrawCircleHole(false)
                valueTextSize = 10f
                valueTextColor = context.getColor(R.color.text_primary)
                setDrawFilled(false)
            }
            dataSets.add(habitsDataSet)
            
            // Mood score line
            val moodData = chartData.map { Entry(it.dayIndex.toFloat(), it.moodScore) }
            val moodDataSet = LineDataSet(moodData, "Mood").apply {
                color = context.getColor(R.color.accent_blue)
                setCircleColor(context.getColor(R.color.accent_blue))
                lineWidth = 3f
                circleRadius = 4f
                setDrawCircleHole(false)
                valueTextSize = 10f
                valueTextColor = context.getColor(R.color.text_primary)
                setDrawFilled(false)
            }
            dataSets.add(moodDataSet)
            
            // Hydration completion rate line
            val hydrationData = chartData.map { Entry(it.dayIndex.toFloat(), it.hydrationCompletionRate) }
            val hydrationDataSet = LineDataSet(hydrationData, "Hydration").apply {
                color = context.getColor(R.color.accent_orange)
                setCircleColor(context.getColor(R.color.accent_orange))
                lineWidth = 3f
                circleRadius = 4f
                setDrawCircleHole(false)
                valueTextSize = 10f
                valueTextColor = context.getColor(R.color.text_primary)
                setDrawFilled(false)
            }
            dataSets.add(hydrationDataSet)
            
            val lineData = LineData(dataSets as List<ILineDataSet>)
            chart.data = lineData
            
            // Set up X-axis labels
            val labels = getLast14DaysLabels()
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            
            chart.invalidate() // Refresh chart
        } else {
            chart.clear()
            chart.invalidate()
        }
    }
    
    /**
     * Setup a simple 14-day habits completion chart
     */
    fun setupHabitsChart(chart: LineChart, habits: List<Habit>, habitProgress: Map<String, List<HabitProgress>>) {
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
            setBackgroundColor(context.getColor(R.color.card_background))
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelCount = 7
                textColor = context.getColor(R.color.text_secondary)
                textSize = 10f
            }
            
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 100f
                setDrawGridLines(true)
                textColor = context.getColor(R.color.text_secondary)
                textSize = 10f
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}%"
                    }
                }
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
        
        val chartData = prepareHabitsData(habits, habitProgress)
        
        if (chartData.isNotEmpty()) {
            val habitsData = chartData.map { Entry(it.dayIndex.toFloat(), it.habitsCompletionRate) }
            val habitsDataSet = LineDataSet(habitsData, "Habits Completion").apply {
                color = context.getColor(R.color.primary_green)
                setCircleColor(context.getColor(R.color.primary_green))
                lineWidth = 3f
                circleRadius = 4f
                setDrawCircleHole(false)
                valueTextSize = 10f
                valueTextColor = context.getColor(R.color.text_primary)
                setDrawFilled(true)
                fillColor = context.getColor(R.color.primary_green_light)
                fillAlpha = 50
            }
            
            val lineData = LineData(habitsDataSet)
            chart.data = lineData
            
            val labels = getLast14DaysLabels()
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            
            chart.invalidate()
        } else {
            chart.clear()
            chart.invalidate()
        }
    }
    
    private fun prepareProgressData(
        habits: List<Habit>,
        habitProgress: Map<String, List<HabitProgress>>,
        moodEntries: List<MoodEntry>,
        hydrationData: List<DailyHydration>
    ): List<DayProgressData> {
        val calendar = Calendar.getInstance()
        val entries = mutableListOf<DayProgressData>()
        
        // Get data for the last 14 days
        for (i in 13 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateString = dateFormat.format(calendar.time)
            
            // Calculate habits completion rate
            val habitsCompletionRate = calculateHabitsCompletionRate(habits, habitProgress, dateString)
            
            // Calculate mood score
            val moodScore = calculateMoodScore(moodEntries, dateString)
            
            // Calculate hydration completion rate
            val hydrationCompletionRate = calculateHydrationCompletionRate(hydrationData, dateString)
            
            entries.add(
                DayProgressData(
                    dayIndex = 13 - i,
                    date = dateString,
                    habitsCompletionRate = habitsCompletionRate,
                    moodScore = moodScore,
                    hydrationCompletionRate = hydrationCompletionRate
                )
            )
        }
        
        return entries
    }
    
    private fun prepareHabitsData(
        habits: List<Habit>,
        habitProgress: Map<String, List<HabitProgress>>
    ): List<DayProgressData> {
        val calendar = Calendar.getInstance()
        val entries = mutableListOf<DayProgressData>()
        
        for (i in 13 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateString = dateFormat.format(calendar.time)
            
            val habitsCompletionRate = calculateHabitsCompletionRate(habits, habitProgress, dateString)
            
            entries.add(
                DayProgressData(
                    dayIndex = 13 - i,
                    date = dateString,
                    habitsCompletionRate = habitsCompletionRate,
                    moodScore = 0f,
                    hydrationCompletionRate = 0f
                )
            )
        }
        
        return entries
    }
    
    private fun calculateHabitsCompletionRate(
        habits: List<Habit>,
        habitProgress: Map<String, List<HabitProgress>>,
        date: String
    ): Float {
        if (habits.isEmpty()) return 0f
        
        var completedCount = 0
        habits.forEach { habit ->
            val progressList = habitProgress[habit.id] ?: emptyList()
            val dayProgress = progressList.find { it.date == date }
            if (dayProgress?.isCompleted == true) {
                completedCount++
            }
        }
        
        return ((completedCount.toFloat() / habits.size.toFloat()) * 100f)
    }
    
    private fun calculateMoodScore(moodEntries: List<MoodEntry>, date: String): Float {
        val dayMoods = moodEntries.filter { it.date == date }
        if (dayMoods.isEmpty()) return 0f
        
        val averageMood = dayMoods.map { it.mood.value }.average().toFloat()
        // Convert 1-5 scale to 0-100 scale
        return ((averageMood - 1f) / 4f) * 100f
    }
    
    private fun calculateHydrationCompletionRate(hydrationData: List<DailyHydration>, date: String): Float {
        val dayHydration = hydrationData.find { it.date == date }
        if (dayHydration == null) return 0f
        
        return if (dayHydration.goalMl > 0) {
            ((dayHydration.totalIntakeMl.toFloat() / dayHydration.goalMl.toFloat()) * 100f).coerceAtMost(100f)
        } else {
            0f
        }
    }
    
    private fun getLast14DaysLabels(): List<String> {
        val calendar = Calendar.getInstance()
        val labels = mutableListOf<String>()
        
        for (i in 13 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            labels.add(dayFormat.format(calendar.time))
        }
        
        return labels
    }
    
    /**
     * Data class representing daily progress metrics
     */
    private data class DayProgressData(
        val dayIndex: Int,
        val date: String,
        val habitsCompletionRate: Float,
        val moodScore: Float,
        val hydrationCompletionRate: Float
    )
}
