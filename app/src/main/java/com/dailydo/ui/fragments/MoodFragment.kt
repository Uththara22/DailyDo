package com.dailydo.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dailydo.MainActivity
import com.dailydo.R
import com.dailydo.data.models.MoodEntry
import com.dailydo.data.models.MoodType
import com.dailydo.data.repository.MoodRepository
import com.dailydo.data.repository.SharedPreferencesManager
import com.dailydo.services.MoodSyncService
import com.dailydo.ui.adapters.MoodSelectorAdapter
import com.dailydo.ui.adapters.MoodHistoryAdapter
import com.dailydo.ui.adapters.MoodDayGroupAdapter
import com.dailydo.ui.charts.MoodChartHelper
import com.github.mikephil.charting.charts.LineChart
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Fragment for mood journaling with emoji selector
 */
class MoodFragment : Fragment() {
    
    private lateinit var recyclerMoodSelector: RecyclerView
    private lateinit var recyclerMoodHistory: RecyclerView
    private lateinit var btnSaveMood: MaterialButton
    private lateinit var btnShareMood: MaterialButton
    private lateinit var chartMoodTrend: LineChart
    private lateinit var tabLayoutMoodView: TabLayout
    private lateinit var layoutMoodCalendar: View
    
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var moodRepository: MoodRepository
    private lateinit var moodSelectorAdapter: MoodSelectorAdapter
    private lateinit var moodHistoryAdapter: MoodHistoryAdapter
    private lateinit var moodDayGroupAdapter: MoodDayGroupAdapter
    private lateinit var moodChartHelper: MoodChartHelper
    
    private var selectedMood: MoodType? = null
    
    // Broadcast receiver for API updates
    private val moodDataUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.dailydo.MOOD_DATA_UPDATED") {
                // Refresh the mood data and graph when API updates occur
                loadMoodHistory()
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize components
        initializeViews(view)
        setupMoodSelector()
        setupMoodHistory()
        setupMoodViewTabs()
        setupClickListeners()
        registerBroadcastReceiver()
        loadMoodHistory()
    }
    
    override fun onResume() {
        super.onResume()
        loadMoodHistory()
        // Trigger sync when fragment becomes visible
        syncMoodData()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterBroadcastReceiver()
    }
    
    private fun initializeViews(view: View) {
        prefsManager = SharedPreferencesManager.getInstance(requireContext())
        moodRepository = MoodRepository.getInstance(requireContext(), null) // TODO: Initialize with actual API service
        moodChartHelper = MoodChartHelper(requireContext())
        recyclerMoodSelector = view.findViewById(R.id.recycler_mood_selector)
        recyclerMoodHistory = view.findViewById(R.id.recycler_mood_history)
        btnSaveMood = view.findViewById(R.id.btn_save_mood)
        btnShareMood = view.findViewById(R.id.btn_share_mood)
        chartMoodTrend = view.findViewById(R.id.chart_mood_trend)
        tabLayoutMoodView = view.findViewById(R.id.tab_layout_mood_view)
        layoutMoodCalendar = view.findViewById(R.id.layout_mood_calendar)
    }
    
    private fun setupMoodSelector() {
        moodSelectorAdapter = MoodSelectorAdapter { mood ->
            selectedMood = mood
            updateSaveButtonState()
        }
        
        recyclerMoodSelector.apply {
            layoutManager = GridLayoutManager(context, 5) // 5 columns for emojis
            adapter = moodSelectorAdapter
            setHasFixedSize(true)
        }
        
        // Load all available moods
        moodSelectorAdapter.updateMoods(MoodType.getAllMoods())
    }
    
    private fun setupMoodHistory() {
        moodDayGroupAdapter = MoodDayGroupAdapter(
            onMoodEntryClick = { entry -> showMoodEntryDetails(entry) },
            onMoodEntryDelete = { entry -> deleteMoodEntry(entry) },
            onMoodEntryShare = { entry -> shareMoodEntry(entry) }
        )
        
        recyclerMoodHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moodDayGroupAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupMoodViewTabs() {
        // Hide the tab layout since we're using day-based grouping
        tabLayoutMoodView.visibility = View.GONE
    }
    
    private fun setupClickListeners() {
        btnSaveMood.setOnClickListener {
            saveMoodEntry()
        }
        
        btnShareMood.setOnClickListener {
            shareTodaysMood()
        }
    }
    
    private fun updateSaveButtonState() {
        btnSaveMood.isEnabled = selectedMood != null
    }
    
    private fun saveMoodEntry() {
        val mood = selectedMood ?: return
        
        val moodEntry = MoodEntry(
            mood = mood,
            emoji = mood.emoji,
            notes = "", // Removed notes field
            timestamp = Date()
        )
        
        // Save mood entry using repository (handles both local and API sync)
        lifecycleScope.launch {
            val result = moodRepository.addMoodEntry(moodEntry)
            result.fold(
                onSuccess = { entry ->
                    // Reset form
                    selectedMood = null
                    moodSelectorAdapter.clearSelection()
                    updateSaveButtonState()
                    
                    // Refresh history
                    loadMoodHistory()
                    
                    // Show success message
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Mood saved successfully!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                },
                onFailure = { error ->
                    // Show error message
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Failed to save mood: ${error.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }
    
    private fun loadMoodHistory() {
        // Load mood entries using repository (handles API sync)
        lifecycleScope.launch {
            val result = moodRepository.getMoodEntries()
            result.fold(
                onSuccess = { moodEntries ->
                    moodDayGroupAdapter.updateMoodEntries(moodEntries)
                    
                    // Update mood trend chart
                    moodChartHelper.setupMoodTrendChart(chartMoodTrend, moodEntries)
                    
                    // Update share button state
                    val todayEntries = prefsManager.getTodayMoodEntries()
                    btnShareMood.isEnabled = todayEntries.isNotEmpty()
                    
                    // Update empty state visibility
                    val isEmpty = moodEntries.isEmpty()
                    recyclerMoodHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
                    view?.findViewById<View>(R.id.layout_empty_mood_history)?.visibility = if (isEmpty) View.VISIBLE else View.GONE
                },
                onFailure = { error ->
                    // Fallback to local data if API fails
                    val localEntries = prefsManager.getMoodEntries()
                    moodDayGroupAdapter.updateMoodEntries(localEntries)
                    moodChartHelper.setupMoodTrendChart(chartMoodTrend, localEntries)
                    
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Using offline data: ${error.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }
    
    private fun showMoodEntryDetails(entry: MoodEntry) {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
        
        val message = buildString {
            append("Mood: ${entry.mood.label} ${entry.emoji}\n")
            append("Date: ${dateFormat.format(entry.timestamp)}\n")
            append("Time: ${timeFormat.format(entry.timestamp)}")
            if (entry.notes.isNotBlank()) {
                append("\n\nNotes: ${entry.notes}")
            }
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Mood Entry Details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNeutralButton("Share") { _, _ -> shareMoodEntry(entry) }
            .setNegativeButton("Delete") { _, _ -> deleteMoodEntry(entry) }
            .show()
    }

    private fun deleteMoodEntry(entry: MoodEntry) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                // Delete using repository (handles API sync)
                lifecycleScope.launch {
                    val result = moodRepository.deleteMoodEntry(entry.id)
                    result.fold(
                        onSuccess = {
                            loadMoodHistory()
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Mood entry deleted successfully!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = { error ->
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Failed to delete mood entry: ${error.message}",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun shareMoodEntry(entry: MoodEntry) {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        
        val shareText = buildString {
            append("My mood: ${entry.mood.label} ${entry.emoji}\n")
            append("Date: ${dateFormat.format(entry.timestamp)} at ${timeFormat.format(entry.timestamp)}")
            if (entry.notes.isNotBlank()) {
                append("\nNotes: ${entry.notes}")
            }
        }
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        startActivity(Intent.createChooser(shareIntent, "Share Mood Entry"))
    }
    
    private fun shareTodaysMood() {
        val todayEntries = prefsManager.getTodayMoodEntries()
        if (todayEntries.isEmpty()) return
        
        val moodSummary = if (todayEntries.size == 1) {
            "${todayEntries.first().mood.label} ${todayEntries.first().emoji}"
        } else {
            val moods = todayEntries.joinToString(", ") { "${it.mood.label} ${it.emoji}" }
            "Multiple moods today: $moods"
        }
        
        val shareText = getString(R.string.share_mood_summary, moodSummary)
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
    }
    
    /**
     * Register broadcast receiver for API updates
     */
    private fun registerBroadcastReceiver() {
        val filter = IntentFilter("com.dailydo.MOOD_DATA_UPDATED")
        requireContext().registerReceiver(moodDataUpdateReceiver, filter)
    }
    
    /**
     * Unregister broadcast receiver
     */
    private fun unregisterBroadcastReceiver() {
        try {
            requireContext().unregisterReceiver(moodDataUpdateReceiver)
        } catch (e: Exception) {
            // Receiver was not registered
        }
    }
    
    /**
     * Sync mood data with API
     */
    private fun syncMoodData() {
        lifecycleScope.launch {
            if (moodRepository.isApiAvailable()) {
                val result = moodRepository.syncMoodEntries()
                result.fold(
                    onSuccess = { entries ->
                        // Data synced successfully, refresh UI
                        loadMoodHistory()
                    },
                    onFailure = { error ->
                        // Sync failed, but continue with local data
                        android.util.Log.w("MoodFragment", "Sync failed: ${error.message}")
                    }
                )
            }
        }
    }
    
    /**
     * Add mood via API (for external triggers)
     */
    fun addMoodViaApi(moodType: MoodType, emoji: String, notes: String = "") {
        val intent = Intent(requireContext(), MoodSyncService::class.java).apply {
            action = MoodSyncService.ACTION_ADD_MOOD_VIA_API
            putExtra(MoodSyncService.EXTRA_MOOD_TYPE, moodType.name)
            putExtra(MoodSyncService.EXTRA_EMOJI, emoji)
            putExtra(MoodSyncService.EXTRA_NOTES, notes)
        }
        requireContext().startService(intent)
    }
    
    
    
}