package com.dailydo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dailydo.R
import com.dailydo.data.models.*
import com.dailydo.data.repository.SharedPreferencesManager
 
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for displaying 14-day progress charts
 * Shows comprehensive wellness data including habits, mood, and hydration
 */
class ProgressFragment : Fragment() {
    
    private lateinit var tvHabitsAvg: TextView
    private lateinit var tvMoodAvg: TextView
    private lateinit var tvHydrationAvg: TextView
    private lateinit var tvCurrentTitle: TextView
    
    private lateinit var prefsManager: SharedPreferencesManager
    
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_progress, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize components
        initializeViews(view)
        loadProgressData()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when fragment becomes visible
        loadProgressData()
    }
    
    private fun initializeViews(view: View) {
        prefsManager = SharedPreferencesManager.getInstance(requireContext())
        
        tvHabitsAvg = view.findViewById(R.id.tv_habits_avg)
        tvMoodAvg = view.findViewById(R.id.tv_mood_avg)
        tvHydrationAvg = view.findViewById(R.id.tv_hydration_avg)
        tvCurrentTitle = view.findViewById(R.id.tv_current_title)

        // Date picker
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_pick_date)
            ?.setOnClickListener { openDatePicker() }
    }
    
    private fun loadProgressData() {
        val habits = prefsManager.getHabits()
        val moodEntries = prefsManager.getMoodEntries()
        val hydrationData = prefsManager.getHydrationData()
        
        // Build habit progress map
        val habitProgress = mutableMapOf<String, List<HabitProgress>>()
        habits.forEach { habit ->
            val progress = prefsManager.getHabitProgressForHabit(habit.id)
            habitProgress[habit.id] = progress
        }
        
        // Update stats for yesterday by default
        val habitsYesterday = calculateHabitsYesterday(habits, habitProgress)
        val moodYesterday = calculateMoodYesterday(moodEntries)
        val hydrationYesterday = calculateHydrationYesterday(hydrationData)
        
        tvHabitsAvg.text = "${habitsYesterday.toInt()}%"
        tvMoodAvg.text = "${moodYesterday.toInt()}%"
        tvHydrationAvg.text = "${hydrationYesterday.toInt()}%"
        tvCurrentTitle.text = "Yesterday Progress Summary"

    }

    private fun openDatePicker() {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val dialog = android.app.DatePickerDialog(requireContext(), { _, y, m, d ->
            val selected = Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, d)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val iso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selected.time)
            // Block specific dates: 2025-10-06 and 2025-10-07
            val blocked = setOf("2025-10-06", "2025-10-07")
            if (iso in blocked) {
                android.widget.Toast.makeText(requireContext(), "No data available for selected date", android.widget.Toast.LENGTH_SHORT).show()
                return@DatePickerDialog
            }
            showSummaryForDate(iso)
        }, year, month, day)
        // Allow only within range: from 2025-10-08 to yesterday
        dialog.datePicker.maxDate = System.currentTimeMillis() - 24L * 60L * 60L * 1000L
        val minAllowed = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2025)
            set(Calendar.MONTH, Calendar.OCTOBER)
            set(Calendar.DAY_OF_MONTH, 8)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val earliest = getEarliestDataDateMillis()
        dialog.datePicker.minDate = maxOf(minAllowed, earliest ?: minAllowed)
        dialog.show()
    }

    private fun getEarliestDataDateMillis(): Long? {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dates = mutableListOf<Long>()

        // Mood entries (use timestamp)
        runCatching { prefsManager.getMoodEntries().minByOrNull { it.timestamp.time }?.timestamp?.time }
            .getOrNull()?.let { dates.add(it) }

        // Hydration intakes (derive from hydration data entries by parsing date)
        runCatching {
            prefsManager.getHydrationData().minByOrNull { it.date }?.date?.let { sdf.parse(it)?.time }
        }.getOrNull()?.let { if (it != null) dates.add(it) }

        // Habit progress dates
        runCatching {
            val all = prefsManager.getHabitProgress()
            all.minByOrNull { it.date }?.date?.let { sdf.parse(it)?.time }
        }.getOrNull()?.let { if (it != null) dates.add(it) }

        // Saved daily summaries
        runCatching {
            prefsManager.getDailySummaries().minByOrNull { it.date }?.date?.let { sdf.parse(it)?.time }
        }.getOrNull()?.let { if (it != null) dates.add(it) }

        if (dates.isEmpty()) return null
        // Ensure minDate is not in the future
        val min = dates.minOrNull() ?: return null
        return min.coerceAtMost(System.currentTimeMillis())
    }

    private fun showSummaryForDate(dateIso: String) {
        // Always compute from raw data to avoid stale or mismatched cached values
        val habits = prefsManager.getHabits()
        val habitProgress = habits.associate { it.id to prefsManager.getHabitProgressForHabit(it.id) }
        val moodEntries = prefsManager.getMoodEntries()
        val hydrationData = prefsManager.getHydrationData()

        val habitsVal = calculateHabitsForDate(habits, habitProgress, dateIso)
        val moodVal = calculateMoodForDate(moodEntries, dateIso)
        val hydrationVal = calculateHydrationForDate(hydrationData, dateIso)

        tvHabitsAvg.text = "${habitsVal.toInt()}%"
        tvMoodAvg.text = "${moodVal.toInt()}%"
        tvHydrationAvg.text = "${hydrationVal.toInt()}%"

        val human = try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateIso)!!
            SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(parsed)
        } catch (_: Exception) { dateIso }
        tvCurrentTitle.text = "$human Progress Summary"
    }
    
    private fun updateAllProgressStats(
        habits: List<Habit>,
        habitProgress: Map<String, List<HabitProgress>>,
        moodEntries: List<MoodEntry>,
        hydrationData: List<DailyHydration>
    ) {
        // Calculate 14-day averages
        val habitsAvg = calculateHabitsAverage(habits, habitProgress)
        val moodAvg = calculateMoodAverage(moodEntries)
        val hydrationAvg = calculateHydrationAverage(hydrationData)
        
        tvHabitsAvg.text = "${habitsAvg.toInt()}%"
        tvMoodAvg.text = "${moodAvg.toInt()}%"
        tvHydrationAvg.text = "${hydrationAvg.toInt()}%"
    }
    
    private fun updateHabitsOnlyStats(
        habits: List<Habit>,
        habitProgress: Map<String, List<HabitProgress>>
    ) {
        val habitsAvg = calculateHabitsAverage(habits, habitProgress)
        
        tvHabitsAvg.text = "${habitsAvg.toInt()}%"
        tvMoodAvg.text = "N/A"
        tvHydrationAvg.text = "N/A"
    }
    
    private fun calculateHabitsAverage(habits: List<Habit>, habitProgress: Map<String, List<HabitProgress>>): Float {
        if (habits.isEmpty()) return 0f
        
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var totalCompletionRate = 0f
        var daysWithData = 0
        
        // Calculate average for last 14 days
        for (i in 13 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateString = dateFormat.format(calendar.time)
            
            var completedCount = 0
            habits.forEach { habit ->
                val progressList = habitProgress[habit.id] ?: emptyList()
                val dayProgress = progressList.find { it.date == dateString }
                if (dayProgress?.isCompleted == true) {
                    completedCount++
                }
            }
            
            if (habits.isNotEmpty()) {
                val dayCompletionRate = (completedCount.toFloat() / habits.size.toFloat()) * 100f
                totalCompletionRate += dayCompletionRate
                daysWithData++
            }
        }
        
        return if (daysWithData > 0) totalCompletionRate / daysWithData else 0f
    }
    
    private fun calculateMoodAverage(moodEntries: List<MoodEntry>): Float {
        if (moodEntries.isEmpty()) return 0f
        
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var totalMoodScore = 0f
        var daysWithData = 0
        
        // Calculate average for last 14 days
        for (i in 13 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateString = dateFormat.format(calendar.time)
            
            val dayMoods = moodEntries.filter { it.date == dateString }
            if (dayMoods.isNotEmpty()) {
                val averageMood = dayMoods.map { it.mood.value }.average().toFloat()
                val moodScore = ((averageMood - 1f) / 4f) * 100f
                totalMoodScore += moodScore
                daysWithData++
            }
        }
        
        return if (daysWithData > 0) totalMoodScore / daysWithData else 0f
    }
    
    private fun calculateHydrationAverage(hydrationData: List<DailyHydration>): Float {
        if (hydrationData.isEmpty()) return 0f
        
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var totalHydrationRate = 0f
        var daysWithData = 0
        
        // Calculate average for last 14 days
        for (i in 13 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateString = dateFormat.format(calendar.time)
            
            val dayHydration = hydrationData.find { it.date == dateString }
            if (dayHydration != null && dayHydration.goalMl > 0) {
                val hydrationRate = ((dayHydration.totalIntakeMl.toFloat() / dayHydration.goalMl.toFloat()) * 100f).coerceAtMost(100f)
                totalHydrationRate += hydrationRate
                daysWithData++
            }
        }
        
        return if (daysWithData > 0) totalHydrationRate / daysWithData else 0f
    }
    
    private fun calculateHabitsYesterday(
        habits: List<Habit>,
        habitProgress: Map<String, List<HabitProgress>>
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

    
    private fun calculateMoodYesterday(moodEntries: List<MoodEntry>): Float {
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

    
    private fun calculateHydrationYesterday(hydrationData: List<DailyHydration>): Float {
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

    private fun calculateHabitsForDate(
        habits: List<Habit>,
        habitProgress: Map<String, List<HabitProgress>>,
        dateIso: String
    ): Float {
        if (habits.isEmpty()) return 0f
        var completedCount = 0
        habits.forEach { habit ->
            val progressList = habitProgress[habit.id] ?: emptyList()
            val dayProgress = progressList.find { it.date == dateIso }
            if (dayProgress?.isCompleted == true) {
                completedCount++
            }
        }
        return ((completedCount.toFloat() / habits.size.toFloat()) * 100f).coerceIn(0f, 100f)
    }

    private fun calculateMoodForDate(moodEntries: List<MoodEntry>, dateIso: String): Float {
        if (moodEntries.isEmpty()) return 0f
        val dayMoods = moodEntries.filter { it.date == dateIso }
        if (dayMoods.isEmpty()) return 0f
        val averageMood = dayMoods.map { it.mood.value }.average().toFloat()
        return (((averageMood - 1f) / 4f) * 100f).coerceIn(0f, 100f)
    }

    private fun calculateHydrationForDate(hydrationData: List<DailyHydration>, dateIso: String): Float {
        if (hydrationData.isEmpty()) return 0f
        val dayHydration = hydrationData.find { it.date == dateIso }
        if (dayHydration == null || dayHydration.goalMl <= 0) return 0f
        return (((dayHydration.totalIntakeMl.toFloat() / dayHydration.goalMl.toFloat()) * 100f)
            .coerceAtMost(100f))
    }

    
    
    
}
