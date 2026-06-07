package com.dailydo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dailydo.R
import com.dailydo.ui.adapters.MoodHistoryAdapter
import com.dailydo.databinding.FragmentMoodJournalBinding
import com.dailydo.data.models.MoodEntry
import com.dailydo.data.models.MoodType
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MoodJournalFragment : Fragment() {

    private var _binding: FragmentMoodJournalBinding? = null
    private val binding get() = _binding!!

    private lateinit var moodAdapter: MoodHistoryAdapter
    private var selectedMood: MoodType? = null
    private val moodEntries = mutableListOf<MoodEntry>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupMoodSelection()
        setupClickListeners()
        loadMoodHistory()
    }

    private fun setupRecyclerView() {
        moodAdapter = MoodHistoryAdapter(
            onMoodEntryClick = { entry -> showMoodEntryDetails(entry) },
            onMoodEntryDelete = { entry -> deleteMoodEntry(entry) },
            onMoodEntryShare = { entry -> shareMoodEntry(entry) }
        )
        binding.recyclerMoodHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moodAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupMoodSelection() {
        val moodCards = listOf(
            binding.moodCard1 to MoodType.VERY_HAPPY,
            binding.moodCard2 to MoodType.HAPPY,
            binding.moodCard3 to MoodType.NEUTRAL,
            binding.moodCard4 to MoodType.COOL,
            binding.moodCard5 to MoodType.JOY,
            binding.moodCard6 to MoodType.ANXIOUS,
            binding.moodCard7 to MoodType.EXCITED,
            binding.moodCard8 to MoodType.CALM,
            binding.moodCard9 to MoodType.TIRED,
            binding.moodCard10 to MoodType.CONFUSED,
            binding.moodCard11 to MoodType.FRUSTRATED,
            binding.moodCard12 to MoodType.LOVED_HEART_EYES
        )

        moodCards.forEach { (card, moodType) ->
            card.setOnClickListener {
                selectMood(card, moodType)
            }
        }
    }

    private fun selectMood(selectedCard: MaterialCardView, moodType: MoodType) {
        // Reset all cards
        val allCards = listOf(
            binding.moodCard1, binding.moodCard2, binding.moodCard3,
            binding.moodCard4, binding.moodCard5, binding.moodCard6,
            binding.moodCard7, binding.moodCard8, binding.moodCard9,
            binding.moodCard10, binding.moodCard11, binding.moodCard12
        )
        
        allCards.forEach { card ->
            card.strokeWidth = 0
            card.elevation = 2f
        }

        // Highlight selected card
        selectedCard.strokeWidth = 4
        selectedCard.elevation = 8f
        selectedMood = moodType
    }

    private fun setupClickListeners() {
        binding.btnSaveMood.setOnClickListener {
            saveMood()
        }

        // Removed btnViewAllMoods click listener as button was removed from layout
    }

    private fun saveMood() {
        if (selectedMood == null) {
            Toast.makeText(context, "Please select a mood first", Toast.LENGTH_SHORT).show()
            return
        }

        val note = binding.etMoodNote.text.toString().trim()
        
        lifecycleScope.launch {
            try {
                val moodEntry = MoodEntry(
                    mood = selectedMood!!,
                    notes = note,
                    timestamp = Date(),
                    emoji = selectedMood!!.emoji
                )

                // Save to database (you'll need to implement this)
                saveMoodToDatabase(moodEntry)
                
                // Add to local list
                moodEntries.add(0, moodEntry)
                
                withContext(Dispatchers.Main) {
                    moodAdapter.updateMoodEntries(moodEntries)
                    updateEmptyState()
                    clearForm()
                    Toast.makeText(context, "Mood saved successfully!", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to save mood: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun saveMoodToDatabase(moodEntry: MoodEntry) {
        // Save to SharedPreferences using simple string format
        withContext(Dispatchers.IO) {
            // Get existing mood entries
            val existingEntries = getStoredMoodEntries()
            val updatedEntries = existingEntries.toMutableList()
            updatedEntries.add(0, moodEntry) // Add to beginning
            
            // Save as simple string format: mood|emoji|notes|timestamp
            val entriesString = updatedEntries.joinToString("||") { entry ->
                "${entry.mood.name}|${entry.emoji}|${entry.notes}|${entry.timestamp.time}"
            }
            
            requireContext().getSharedPreferences("mood_entries", android.content.Context.MODE_PRIVATE)
                .edit()
                .putString("mood_list", entriesString)
                .apply()
        }
    }

    private fun clearForm() {
        selectedMood = null
        binding.etMoodNote.text?.clear()
        
        // Reset all mood cards
        val allCards = listOf(
            binding.moodCard1, binding.moodCard2, binding.moodCard3,
            binding.moodCard4, binding.moodCard5, binding.moodCard6,
            binding.moodCard7, binding.moodCard8, binding.moodCard9,
            binding.moodCard10, binding.moodCard11, binding.moodCard12
        )
        
        allCards.forEach { card ->
            card.strokeWidth = 0
            card.elevation = 2f
        }
    }

    private fun loadMoodHistory() {
        lifecycleScope.launch {
            try {
                // Load from database (you'll need to implement this)
                val loadedMoods = loadMoodsFromDatabase()
                
                withContext(Dispatchers.Main) {
                    moodEntries.clear()
                    moodEntries.addAll(loadedMoods)
                    moodAdapter.updateMoodEntries(moodEntries)
                    updateEmptyState()
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to load mood history: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun loadMoodsFromDatabase(): List<MoodEntry> {
        return withContext(Dispatchers.IO) {
            getStoredMoodEntries()
        }
    }

    private fun updateEmptyState() {
        if (moodEntries.isEmpty()) {
            binding.layoutEmptyMood.visibility = View.VISIBLE
            binding.recyclerMoodHistory.visibility = View.GONE
        } else {
            binding.layoutEmptyMood.visibility = View.GONE
            binding.recyclerMoodHistory.visibility = View.VISIBLE
        }
    }

    private fun getStoredMoodEntries(): List<MoodEntry> {
        return try {
            val prefs = requireContext().getSharedPreferences("mood_entries", android.content.Context.MODE_PRIVATE)
            val entriesString = prefs.getString("mood_list", null)
            if (entriesString != null && entriesString.isNotEmpty()) {
                entriesString.split("||").mapNotNull { entryString ->
                    try {
                        val parts = entryString.split("|")
                        if (parts.size >= 4) {
                            val mood = MoodType.valueOf(parts[0])
                            val emoji = parts[1]
                            val notes = parts[2]
                            val timestamp = Date(parts[3].toLong())
                            MoodEntry(mood = mood, emoji = emoji, notes = notes, timestamp = timestamp)
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun showMoodEntryDetails(entry: MoodEntry) {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
        
        val message = buildString {
            append("Mood: ${entry.mood.label} ${entry.emoji}\n")
            append("Date: ${dateFormat.format(entry.timestamp)}\n")
            append("Time: ${timeFormat.format(entry.timestamp)}\n")
            if (!entry.notes.isNullOrBlank()) {
                append("Note: ${entry.notes}")
            }
        }
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Mood Entry Details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .create()

        dialog.setOnShowListener {
            // Title text color to black
            dialog.findViewById<android.widget.TextView>(com.google.android.material.R.id.alertTitle)
                ?.setTextColor(requireContext().getColor(com.dailydo.R.color.black))
            // Message text color to black
            dialog.findViewById<android.widget.TextView>(android.R.id.message)
                ?.setTextColor(requireContext().getColor(com.dailydo.R.color.black))
            // OK button text color to red
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(requireContext().getColor(com.dailydo.R.color.error_red))
        }

        dialog.show()
    }

    private fun deleteMoodEntry(entry: MoodEntry) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        // Remove from local list
                        moodEntries.removeAll { it.timestamp == entry.timestamp }
                        
                        // Update adapter
                        withContext(Dispatchers.Main) {
                            moodAdapter.updateMoodEntries(moodEntries)
                            updateEmptyState()
                            Toast.makeText(context, "Mood entry deleted successfully!", Toast.LENGTH_SHORT).show()
                        }
                        
                        // Save updated list to SharedPreferences
                        saveMoodEntriesToDatabase(moodEntries)
                        
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Failed to delete mood entry: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareMoodEntry(entry: MoodEntry) {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        
        val shareText = buildString {
            append("My mood today: ${entry.mood.label} ${entry.emoji}\n")
            append("Date: ${dateFormat.format(entry.timestamp)}\n")
            append("Time: ${timeFormat.format(entry.timestamp)}\n")
            if (!entry.notes.isNullOrBlank()) {
                append("Note: ${entry.notes}\n")
            }
            append("Tracked with DailyDo app")
        }
        
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        }
        
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Mood Entry"))
    }

    private suspend fun saveMoodEntriesToDatabase(entries: List<MoodEntry>) {
        withContext(Dispatchers.IO) {
            // Save as simple string format: moodType|emoji|note|timestamp
            val entriesString = entries.joinToString("||") { entry ->
                "${entry.mood.name}|${entry.emoji}|${entry.notes}|${entry.timestamp.time}"
            }
            
            requireContext().getSharedPreferences("mood_entries", android.content.Context.MODE_PRIVATE)
                .edit()
                .putString("mood_list", entriesString)
                .apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
