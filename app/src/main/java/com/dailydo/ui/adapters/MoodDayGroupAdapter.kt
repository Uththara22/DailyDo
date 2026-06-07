package com.dailydo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dailydo.R
import com.dailydo.data.models.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for mood entries grouped by day with expandable sections
 */
class MoodDayGroupAdapter(
    private val onMoodEntryClick: (MoodEntry) -> Unit = {},
    private val onMoodEntryDelete: (MoodEntry) -> Unit = {},
    private val onMoodEntryShare: (MoodEntry) -> Unit = {}
) : RecyclerView.Adapter<MoodDayGroupAdapter.DayGroupViewHolder>() {

    private var dayGroups: List<DayMoodGroup> = emptyList()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
    private val todayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    data class DayMoodGroup(
        val date: String,
        val displayDate: String,
        val moodEntries: List<MoodEntry>,
        var isExpanded: Boolean = false
    )

    fun updateMoodEntries(moodEntries: List<MoodEntry>) {
        // Group entries by date
        val groupedEntries = moodEntries.groupBy { it.date }
        
        dayGroups = groupedEntries.map { (date, entries) ->
            val today = todayFormat.format(Date())
            val displayDate = when (date) {
                today -> "Today"
                else -> {
                    try {
                        val parsedDate = dateFormat.parse(date)
                        displayDateFormat.format(parsedDate ?: Date())
                    } catch (e: Exception) {
                        date
                    }
                }
            }
            
            DayMoodGroup(
                date = date,
                displayDate = displayDate,
                moodEntries = entries.sortedByDescending { it.timestamp }
            )
        }.sortedByDescending { it.date }
        
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayGroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_day_group, parent, false)
        return DayGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayGroupViewHolder, position: Int) {
        val dayGroup = dayGroups[position]
        holder.bind(dayGroup)
    }

    override fun getItemCount(): Int = dayGroups.size

    inner class DayGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layoutDayHeader: LinearLayout = itemView.findViewById(R.id.layout_day_header)
        private val tvDayDate: TextView = itemView.findViewById(R.id.tv_day_date)
        private val tvDaySummary: TextView = itemView.findViewById(R.id.tv_day_summary)
        private val ivExpandIcon: ImageView = itemView.findViewById(R.id.iv_expand_icon)
        private val layoutMoodEntries: LinearLayout = itemView.findViewById(R.id.layout_mood_entries)
        private val recyclerDayMoodEntries: RecyclerView = itemView.findViewById(R.id.recycler_day_mood_entries)
        
        private lateinit var moodHistoryAdapter: MoodHistoryAdapter

        fun bind(dayGroup: DayMoodGroup) {
            tvDayDate.text = dayGroup.displayDate
            tvDaySummary.text = "${dayGroup.moodEntries.size} mood entr${if (dayGroup.moodEntries.size == 1) "y" else "ies"}"
            
            // Set up mood entries recycler view
            moodHistoryAdapter = MoodHistoryAdapter(
                onMoodEntryClick = onMoodEntryClick,
                onMoodEntryDelete = onMoodEntryDelete,
                onMoodEntryShare = onMoodEntryShare
            )
            
            recyclerDayMoodEntries.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = moodHistoryAdapter
                setHasFixedSize(true)
            }
            
            moodHistoryAdapter.updateMoodEntries(dayGroup.moodEntries)
            
            // Set up expand/collapse functionality
            layoutDayHeader.setOnClickListener {
                toggleExpansion(dayGroup)
            }
            
            // Update UI based on expansion state
            updateExpansionUI(dayGroup.isExpanded)
        }
        
        private fun toggleExpansion(dayGroup: DayMoodGroup) {
            dayGroup.isExpanded = !dayGroup.isExpanded
            updateExpansionUI(dayGroup.isExpanded)
        }
        
        private fun updateExpansionUI(isExpanded: Boolean) {
            if (isExpanded) {
                layoutMoodEntries.visibility = View.VISIBLE
                ivExpandIcon.animate()
                    .rotation(180f)
                    .setDuration(200)
                    .start()
            } else {
                layoutMoodEntries.visibility = View.GONE
                ivExpandIcon.animate()
                    .rotation(0f)
                    .setDuration(200)
                    .start()
            }
        }
    }
}
