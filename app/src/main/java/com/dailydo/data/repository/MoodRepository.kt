package com.dailydo.data.repository

import android.content.Context
import com.dailydo.data.models.MoodEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Repository for managing mood entries with local storage and API synchronization
 */
class MoodRepository private constructor(
    private val context: Context,
    private val apiService: Any? = null // TODO: Replace with actual API service type
) {
    
    private val sharedPreferencesManager = SharedPreferencesManager.getInstance(context)
    
    companion object {
        @Volatile
        private var INSTANCE: MoodRepository? = null
        
        fun getInstance(context: Context, apiService: Any? = null): MoodRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MoodRepository(context.applicationContext, apiService).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Add a new mood entry
     */
    suspend fun addMoodEntry(moodEntry: MoodEntry): Result<MoodEntry> = withContext(Dispatchers.IO) {
        try {
            // Save locally first
            sharedPreferencesManager.saveMoodEntry(moodEntry)
            
            // Try to sync with API if available
            if (isApiAvailable()) {
                try {
                    // TODO: Implement actual API call
                    // val apiResult = apiService?.addMoodEntry(moodEntry)
                    // Handle API response and potential conflicts
                } catch (e: Exception) {
                    // API failed, but local save succeeded
                    android.util.Log.w("MoodRepository", "API sync failed: ${e.message}")
                }
            }
            
            Result.success(moodEntry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all mood entries
     */
    suspend fun getMoodEntries(): Result<List<MoodEntry>> = withContext(Dispatchers.IO) {
        try {
            val entries = sharedPreferencesManager.getMoodEntries()
            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get mood entries as Flow for reactive updates
     */
    fun getMoodEntriesFlow(): Flow<List<MoodEntry>> = flow {
        val entries = sharedPreferencesManager.getMoodEntries()
        emit(entries)
    }.flowOn(Dispatchers.IO)
    
    /**
     * Delete a mood entry
     */
    suspend fun deleteMoodEntry(entryId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Delete locally
            sharedPreferencesManager.deleteMoodEntry(entryId)
            
            // Try to sync with API if available
            if (isApiAvailable()) {
                try {
                    // TODO: Implement actual API call
                    // apiService?.deleteMoodEntry(entryId)
                } catch (e: Exception) {
                    android.util.Log.w("MoodRepository", "API delete failed: ${e.message}")
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update a mood entry
     */
    suspend fun updateMoodEntry(moodEntry: MoodEntry): Result<MoodEntry> = withContext(Dispatchers.IO) {
        try {
            // Update locally
            val updatedEntry = sharedPreferencesManager.updateMoodEntry(moodEntry)
            
            // Try to sync with API if available
            if (isApiAvailable()) {
                try {
                    // TODO: Implement actual API call
                    // apiService?.updateMoodEntry(moodEntry)
                } catch (e: Exception) {
                    android.util.Log.w("MoodRepository", "API update failed: ${e.message}")
                }
            }
            
            Result.success(updatedEntry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sync mood entries with API
     */
    suspend fun syncMoodEntries(): Result<List<MoodEntry>> = withContext(Dispatchers.IO) {
        try {
            if (!isApiAvailable()) {
                return@withContext Result.failure(Exception("API not available"))
            }
            
            // TODO: Implement actual sync logic
            // 1. Fetch entries from API
            // 2. Compare with local entries
            // 3. Resolve conflicts
            // 4. Update local storage
            // 5. Return merged entries
            
            val localEntries = sharedPreferencesManager.getMoodEntries()
            Result.success(localEntries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if API service is available
     */
    fun isApiAvailable(): Boolean {
        return apiService != null
    }
    
    /**
     * Get mood entries for a specific date
     */
    suspend fun getMoodEntriesForDate(date: String): Result<List<MoodEntry>> = withContext(Dispatchers.IO) {
        try {
            val allEntries = sharedPreferencesManager.getMoodEntries()
            val dateEntries = allEntries.filter { it.date == date }
            Result.success(dateEntries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get mood entries for today
     */
    suspend fun getTodayMoodEntries(): Result<List<MoodEntry>> = withContext(Dispatchers.IO) {
        try {
            val todayEntries = sharedPreferencesManager.getTodayMoodEntries()
            Result.success(todayEntries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get mood statistics
     */
    suspend fun getMoodStatistics(): Result<Map<String, Int>> = withContext(Dispatchers.IO) {
        try {
            val entries = sharedPreferencesManager.getMoodEntries()
            val stats = entries.groupBy { it.mood.name }
                .mapValues { it.value.size }
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
