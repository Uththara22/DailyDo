package com.dailydo.services

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.util.Log
import com.dailydo.data.models.MoodEntry
import com.dailydo.data.models.MoodType
import com.dailydo.data.repository.MoodRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * Background service for syncing mood data with API
 */
class MoodSyncService : IntentService("MoodSyncService") {
    
    companion object {
        const val ACTION_ADD_MOOD_VIA_API = "com.dailydo.ACTION_ADD_MOOD_VIA_API"
        const val ACTION_SYNC_MOOD_DATA = "com.dailydo.ACTION_SYNC_MOOD_DATA"
        const val EXTRA_MOOD_TYPE = "mood_type"
        const val EXTRA_EMOJI = "emoji"
        const val EXTRA_NOTES = "notes"
        
        private const val TAG = "MoodSyncService"
    }
    
    private lateinit var moodRepository: MoodRepository
    
    override fun onCreate() {
        super.onCreate()
        moodRepository = MoodRepository.getInstance(this)
    }
    
    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_ADD_MOOD_VIA_API -> {
                handleAddMoodViaApi(intent)
            }
            ACTION_SYNC_MOOD_DATA -> {
                handleSyncMoodData()
            }
        }
    }
    
    private fun handleAddMoodViaApi(intent: Intent) {
        val moodTypeName = intent.getStringExtra(EXTRA_MOOD_TYPE) ?: return
        val emoji = intent.getStringExtra(EXTRA_EMOJI) ?: return
        val notes = intent.getStringExtra(EXTRA_NOTES) ?: ""
        
        try {
            val moodType = MoodType.valueOf(moodTypeName)
            val moodEntry = MoodEntry(
                mood = moodType,
                emoji = emoji,
                notes = notes,
                timestamp = Date()
            )
            
            // Add mood entry using repository
            CoroutineScope(Dispatchers.IO).launch {
                val result = moodRepository.addMoodEntry(moodEntry)
                result.fold(
                    onSuccess = { entry ->
                        Log.d(TAG, "Mood added successfully via API: ${entry.mood.label}")
                        // Broadcast update to UI
                        broadcastMoodDataUpdated()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to add mood via API: ${error.message}")
                    }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling add mood via API: ${e.message}")
        }
    }
    
    private fun handleSyncMoodData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = moodRepository.syncMoodEntries()
                result.fold(
                    onSuccess = { entries ->
                        Log.d(TAG, "Mood data synced successfully: ${entries.size} entries")
                        broadcastMoodDataUpdated()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to sync mood data: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing mood data: ${e.message}")
            }
        }
    }
    
    private fun broadcastMoodDataUpdated() {
        val intent = Intent("com.dailydo.MOOD_DATA_UPDATED")
        sendBroadcast(intent)
    }
}
