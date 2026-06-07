package com.dailydo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dailydo.R
import com.dailydo.model.MoodEntry
import com.dailydo.model.MoodType
import java.text.SimpleDateFormat
import java.util.*

class MoodHistoryAdapter(
    private var moodEntries: List<MoodEntry> = emptyList()
) : RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
    private val todayFormat = SimpleDateFormat("'Today', h:mm a", Locale.getDefault())

    class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMoodEmoji: TextView = itemView.findViewById(R.id.tv_mood_emoji)
        val tvMoodType: TextView = itemView.findViewById(R.id.tv_mood_type)
        val tvMoodDate: TextView = itemView.findViewById(R.id.tv_mood_date)
        val tvMoodNote: TextView = itemView.findViewById(R.id.tv_mood_note)
        val viewMoodIndicator: View = itemView.findViewById(R.id.view_mood_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_history, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val moodEntry = moodEntries[position]
        
        holder.tvMoodEmoji.text = moodEntry.emoji
        holder.tvMoodType.text = moodEntry.moodType.displayName
        
        // Format date
        val isToday = isToday(moodEntry.timestamp)
        val formattedDate = if (isToday) {
            todayFormat.format(moodEntry.timestamp)
        } else {
            dateFormat.format(moodEntry.timestamp)
        }
        holder.tvMoodDate.text = formattedDate
        
        // Handle note
        if (!moodEntry.note.isNullOrBlank()) {
            holder.tvMoodNote.text = moodEntry.note
            holder.tvMoodNote.visibility = View.VISIBLE
        } else {
            holder.tvMoodNote.visibility = View.GONE
        }
        
        // Set mood indicator color
        val color = ContextCompat.getColor(holder.itemView.context, moodEntry.moodType.colorRes)
        holder.viewMoodIndicator.setBackgroundColor(color)
    }

    override fun getItemCount(): Int = moodEntries.size

    fun updateMoodEntries(newMoodEntries: List<MoodEntry>) {
        moodEntries = newMoodEntries
        notifyDataSetChanged()
    }

    private fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val entryDate = Calendar.getInstance()
        entryDate.time = date
        
        return today.get(Calendar.YEAR) == entryDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == entryDate.get(Calendar.DAY_OF_YEAR)
    }
}
