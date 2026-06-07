package com.dailydo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import android.content.res.ColorStateList
import androidx.recyclerview.widget.RecyclerView
import com.dailydo.R
import com.dailydo.data.models.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for mood history list
 */
class MoodHistoryAdapter(
    private val onMoodEntryClick: (MoodEntry) -> Unit = {},
    private val onMoodEntryDelete: (MoodEntry) -> Unit = {},
    private val onMoodEntryShare: (MoodEntry) -> Unit = {}
) : RecyclerView.Adapter<MoodHistoryAdapter.MoodHistoryViewHolder>() {

    private var moodEntries: List<MoodEntry> = emptyList()
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    private val todayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun updateMoodEntries(newEntries: List<MoodEntry>) {
        moodEntries = newEntries
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_history, parent, false)
        return MoodHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodHistoryViewHolder, position: Int) {
        val entry = moodEntries[position]
        holder.bind(entry)
    }

    override fun getItemCount(): Int = moodEntries.size

    inner class MoodHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardMoodEmoji: com.google.android.material.card.MaterialCardView = itemView.findViewById(R.id.card_mood_emoji)
        private val tvMoodEmoji: TextView = itemView.findViewById(R.id.tv_mood_emoji)
        private val tvMoodType: TextView = itemView.findViewById(R.id.tv_mood_type)
        private val tvMoodDate: TextView = itemView.findViewById(R.id.tv_mood_date)
        private val tvMoodNote: TextView = itemView.findViewById(R.id.tv_mood_note)
        private val viewMoodIndicator: ImageView = itemView.findViewById(R.id.view_mood_indicator)
        private val btnShareMoodEntry: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.btn_share_mood_entry)
        private val btnDeleteMoodEntry: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.btn_delete_mood_entry)

        fun bind(entry: MoodEntry) {
            tvMoodEmoji.text = entry.emoji
            tvMoodType.text = entry.mood.label

            // Format date
            val today = todayFormat.format(Date())
            val entryDate = todayFormat.format(entry.timestamp)
            
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = todayFormat.format(calendar.time)

            tvMoodDate.text = when (entryDate) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> dateFormat.format(entry.timestamp)
            }

            // Show notes if available
            if (entry.notes.isNotBlank()) {
                tvMoodNote.text = entry.notes
                tvMoodNote.visibility = View.VISIBLE
            } else {
                tvMoodNote.visibility = View.GONE
            }

            // Set up click listeners
            itemView.setOnClickListener {
                onMoodEntryClick(entry)
            }

            // Emoji card click with visual feedback
            cardMoodEmoji.setOnClickListener {
                // Add a nice animation effect
                cardMoodEmoji.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction {
                        cardMoodEmoji.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
                
                onMoodEntryClick(entry)
            }
            
            // Add hover effect for better UX
            cardMoodEmoji.setOnTouchListener { _, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        cardMoodEmoji.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(50)
                            .start()
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        cardMoodEmoji.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(50)
                            .start()
                    }
                }
                false
            }

            btnShareMoodEntry.setOnClickListener {
                onMoodEntryShare(entry)
            }

            btnDeleteMoodEntry.setOnClickListener {
                onMoodEntryDelete(entry)
            }

            // Set mood indicator color based on mood type
            val colorRes = when (entry.mood) {
                com.dailydo.data.models.MoodType.VERY_HAPPY -> R.color.mood_very_happy
                com.dailydo.data.models.MoodType.HAPPY -> R.color.mood_happy
                com.dailydo.data.models.MoodType.EXCITED -> R.color.mood_excited
                com.dailydo.data.models.MoodType.CALM -> R.color.mood_calm
                com.dailydo.data.models.MoodType.NEUTRAL -> R.color.mood_neutral
                com.dailydo.data.models.MoodType.TIRED -> R.color.mood_tired
                com.dailydo.data.models.MoodType.ANXIOUS -> R.color.mood_anxious
                com.dailydo.data.models.MoodType.COOL -> R.color.mood_cool
                com.dailydo.data.models.MoodType.ANGRY -> R.color.mood_angry
                com.dailydo.data.models.MoodType.JOY -> R.color.mood_joy
                com.dailydo.data.models.MoodType.CONFUSED -> R.color.mood_confused
                com.dailydo.data.models.MoodType.FRUSTRATED -> R.color.mood_frustrated
                com.dailydo.data.models.MoodType.LOVED_HEART_EYES -> R.color.mood_loved_heart_eyes
            }
            val indicatorColor = itemView.context.getColor(colorRes)
            viewMoodIndicator.imageTintList = ColorStateList.valueOf(indicatorColor)
            
            // Set emoji card background color with slight transparency
            val emojiCardColor = itemView.context.getColor(colorRes)
            val alphaColor = (emojiCardColor and 0x00FFFFFF) or 0x20000000 // 12% opacity
            cardMoodEmoji.setCardBackgroundColor(alphaColor)
            
            // No click indicator view present; keep card animation only
        }
    }
}