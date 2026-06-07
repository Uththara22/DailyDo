package com.dailydo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dailydo.R
import com.dailydo.data.models.Habit
import com.dailydo.data.models.HabitProgress
import com.google.android.material.button.MaterialButton

/**
 * Adapter for displaying habits in RecyclerView
 */
class HabitsAdapter(
    private val onHabitClick: (Habit) -> Unit,
    private val onProgressClick: (Habit, HabitProgress) -> Unit,
    private val onDeleteClick: (Habit) -> Unit,
    private val onEditClick: (Habit) -> Unit,
    private val onShareClick: (Habit) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    private var habitsWithProgress: List<Pair<Habit, HabitProgress>> = emptyList()
    private lateinit var prefsManager: com.dailydo.data.repository.SharedPreferencesManager

    fun updateHabits(newHabitsWithProgress: List<Pair<Habit, HabitProgress>>) {
        habitsWithProgress = newHabitsWithProgress
        // Initialize SharedPreferences manager if not already done
        if (!::prefsManager.isInitialized && habitsWithProgress.isNotEmpty()) {
            // Get context from the first view holder when it's created
        }
        // Ensure dataset changes are applied safely after layout passes
        (this@HabitsAdapter.recyclerViewSafe()?.handler ?: android.os.Handler(android.os.Looper.getMainLooper()))
            .post { notifyDataSetChanged() }
    }

    // Safely get RecyclerView reference if attached
    private fun recyclerViewSafe(): RecyclerView? {
        return try {
            // RecyclerView is the parent of any existing ViewHolder's itemView
            if (itemCount > 0) null else null
        } catch (_: Exception) { null }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val (habit, progress) = habitsWithProgress[position]
        holder.bind(habit, progress)
    }

    override fun getItemCount(): Int = habitsWithProgress.size

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHabitName: TextView = itemView.findViewById(R.id.tv_habit_name)
        private val tvHabitDescription: TextView = itemView.findViewById(R.id.tv_habit_description)
        private val tvHabitTarget: TextView = itemView.findViewById(R.id.tv_habit_target)
        private val tvProgressText: TextView = itemView.findViewById(R.id.tv_progress_text)
        private val tvStreak: TextView = itemView.findViewById(R.id.tv_streak)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar_habit)
        private val checkComplete: com.google.android.material.checkbox.MaterialCheckBox = itemView.findViewById(R.id.check_complete)
        private val btnToggleCompletion: MaterialButton = itemView.findViewById(R.id.btn_toggle_completion)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)

        fun bind(habit: Habit, progress: HabitProgress) {
            // Initialize SharedPreferences manager if needed
            if (!::prefsManager.isInitialized) {
                prefsManager = com.dailydo.data.repository.SharedPreferencesManager.getInstance(itemView.context)
            }
            
            // Basic habit info
            tvHabitName.text = habit.name
            
            if (habit.description.isNotBlank()) {
                tvHabitDescription.text = habit.description
                tvHabitDescription.visibility = View.VISIBLE
            } else {
                tvHabitDescription.visibility = View.GONE
            }
            
            tvHabitTarget.text = "Target: ${habit.targetValue} ${habit.unit}"

            // Progress info
            val progressPercentage = progress.getProgressPercentage(habit.targetValue)
            tvProgressText.text = "Progress: ${progress.currentValue}/${habit.targetValue} ${habit.unit}"
            progressBar.progress = progressPercentage

            // Streak info
            val streak = prefsManager.calculateHabitStreak(habit.id)
            tvStreak.text = itemView.context.getString(R.string.habit_streak, streak)

            // Checkbox reflects and toggles completion
            checkComplete.isChecked = progress.isCompleted
            checkComplete.setOnCheckedChangeListener(null)
            checkComplete.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && !progress.isCompleted) {
                    onProgressClick(habit, progress)
                } else if (!isChecked && progress.isCompleted) {
                    onProgressClick(habit, progress)
                }
            }

            // Completion button
            if (progress.isCompleted) {
                // Show as completed and disable action instead of showing "Mark Incomplete"
                btnToggleCompletion.text = itemView.context.getString(R.string.habit_completed)
                btnToggleCompletion.isEnabled = false
                btnToggleCompletion.alpha = 0.85f
                btnToggleCompletion.setBackgroundColor(
                    itemView.context.getColor(R.color.success_green)
                )
                btnToggleCompletion.setTextColor(itemView.context.getColor(R.color.white))
                btnToggleCompletion.visibility = View.VISIBLE
            } else {
                // Hide the action button before completion; user uses the checkbox
                btnToggleCompletion.visibility = View.GONE
            }

            // Click listeners
            itemView.setOnClickListener { onHabitClick(habit) }
            btnToggleCompletion.setOnClickListener { /* no-op when hidden; completion handled by checkbox */ }
            btnEdit.setOnClickListener { onEditClick(habit) }
            btnDelete.setOnClickListener { onDeleteClick(habit) }
            
            // Update progress bar color based on completion
            val progressColor = if (progress.isCompleted) {
                itemView.context.getColor(R.color.success_green)
            } else {
                itemView.context.getColor(R.color.primary_green)
            }
            progressBar.progressTintList = android.content.res.ColorStateList.valueOf(progressColor)
        }
    }
}