package com.dailydo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.dailydo.data.repository.SharedPreferencesManager
import com.dailydo.receivers.HydrationAlarmScheduler
import com.dailydo.ui.auth.LoginActivity
import com.dailydo.ui.fragments.HabitsFragment
import com.dailydo.ui.fragments.HydrationFragment
import com.dailydo.fragment.MoodJournalFragment
import com.dailydo.ui.fragments.ProgressFragment
import com.dailydo.ui.fragments.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var prefsManager: SharedPreferencesManager
    
    // Permission request code
    private val PERMISSION_REQUEST_CODE = 1001
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            android.util.Log.d("MainActivity", "Starting onCreate")
            
            // Check if user is logged in
            prefsManager = SharedPreferencesManager.getInstance(this)
            val isLoggedIn = prefsManager.isUserLoggedIn()
            android.util.Log.d("MainActivity", "User logged in: $isLoggedIn")
            
            // Skip login check for development purposes
            // if (!isLoggedIn) {
            //     android.util.Log.d("MainActivity", "Redirecting to LoginActivity")
            //     // Redirect to login screen
            //     val intent = Intent(this, LoginActivity::class.java)
            //     startActivity(intent)
            //     finish()
            //     return
            // }
            
            android.util.Log.d("MainActivity", "Setting content view")
            setContentView(R.layout.activity_main)
            
            android.util.Log.d("MainActivity", "Initializing bottom navigation")
            // Initialize bottom navigation
            bottomNavigation = findViewById(R.id.bottom_navigation)
            setupBottomNavigation()
            
            // Set up window insets for proper layout handling
            setupWindowInsets()
            
            // Load default fragment with proper error handling
            if (savedInstanceState == null) {
                android.util.Log.d("MainActivity", "Loading default HabitsFragment")
                try {
                    // Use post to ensure layout is ready
                    findViewById<android.view.View>(R.id.nav_host_fragment).post {
                        try {
                            loadFragment(HabitsFragment())
                        } catch (e: Exception) {
                            android.util.Log.e("MainActivity", "Error loading HabitsFragment", e)
                            android.widget.Toast.makeText(this@MainActivity, "Error loading habits screen: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Error setting up fragment loading", e)
                    android.widget.Toast.makeText(this, "Error setting up screen: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            // Schedule daily summary worker once per day
            scheduleDailySummaryWorker()
            
            // Permission popups disabled per UX request; no dialogs on startup
            
            // Handle intent extras - temporarily disabled for debugging
            // android.util.Log.d("MainActivity", "Handling intent extras")
            // handleIntent(intent)
            
            android.util.Log.d("MainActivity", "onCreate completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in onCreate", e)
            // Show error message to user
            android.widget.Toast.makeText(this, "Error starting app: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    override fun onResume() {
        super.onResume()
    }
    
    override fun onPause() {
        super.onPause()
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_habits -> HabitsFragment()
                R.id.nav_mood -> MoodJournalFragment()
                R.id.nav_hydration -> HydrationFragment()
                R.id.nav_progress -> ProgressFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> HabitsFragment()
            }
            loadFragment(fragment)
            true
        }
        
        // Set the default selected item
        bottomNavigation.selectedItemId = R.id.nav_habits
    }
    
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        android.util.Log.d("MainActivity", "Loading fragment: ${fragment.javaClass.simpleName}")
        try {
            if (supportFragmentManager.isStateSaved) {
                android.util.Log.w("MainActivity", "Fragment manager state is saved, skipping fragment load")
                return
            }
            
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit()
            android.util.Log.d("MainActivity", "Fragment loaded successfully")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error loading fragment", e)
            // Show error message to user
            android.widget.Toast.makeText(this, "Error loading screen: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleDailySummaryWorker() {
        try {
            // Calculate delay until next local midnight
            val now = java.util.Calendar.getInstance()
            val nextMidnight = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val initialDelayMs = (nextMidnight.timeInMillis - now.timeInMillis).coerceAtLeast(0L)

            val workRequest = PeriodicWorkRequestBuilder<com.dailydo.workers.DailySummaryWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .addTag("daily_summary")
                .build()
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "daily_summary",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to schedule daily summary", e)
        }
    }
    
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Show explanation dialog before requesting permission
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Notification Permission Required")
                    .setMessage("DailyDo needs notification permission to send you hydration reminders. Please grant this permission to receive timely reminders.")
                    .setPositiveButton("Grant Permission") { _, _ ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            PERMISSION_REQUEST_CODE
                        )
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
    
    private fun checkAndRequestExactAlarmPermission() {
        // Only check on Android 12+ (API 31)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!HydrationAlarmScheduler.canScheduleExactAlarms(this)) {
                // Show explanation dialog before requesting permission
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Exact Alarm Permission Required")
                    .setMessage("DailyDo needs permission to schedule exact alarms for precise hydration reminders. Please grant this permission to receive timely reminders.")
                    .setPositiveButton("Grant Permission") { _, _ ->
                        HydrationAlarmScheduler.requestExactAlarmPermission(this)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    android.widget.Toast.makeText(
                        this,
                        "Notification permission granted. You'll now receive hydration reminders!",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                } else {
                    // Show explanation why permission is needed
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("Without notification permission, you won't receive hydration reminders. You can enable this permission later in Settings > Apps > DailyDo > Permissions.")
                        .setPositiveButton("Open Settings") { _, _ ->
                            val intent = android.content.Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            )
                            val uri = android.net.Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }
    }
    
    // Method to update the toolbar title from fragments (no longer needed)
    /*
    fun updateToolbarTitle(title: String) {
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)?.title = title
    }
    */
    
    private fun handleIntent(intent: Intent) {
        android.util.Log.d("MainActivity", "Handling intent with extras: ${intent.extras}")
        when {
            intent.getBooleanExtra("open_hydration", false) -> {
                android.util.Log.d("MainActivity", "Opening hydration fragment")
                bottomNavigation.selectedItemId = R.id.nav_hydration
                loadFragment(HydrationFragment())
                
                // Handle quick add water from notification
                if (intent.getBooleanExtra("quick_add_water", false)) {
                    // Add default amount of water (250ml)
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    val intake = com.dailydo.data.models.HydrationIntake(
                        date = today,
                        amountMl = 250,
                        timestamp = java.util.Date()
                    )
                    prefsManager.addHydrationIntake(intake)
                    
                    android.widget.Toast.makeText(
                        this,
                        "Added 250ml of water",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            intent.getBooleanExtra("open_habits", false) -> {
                android.util.Log.d("MainActivity", "Opening habits fragment")
                bottomNavigation.selectedItemId = R.id.nav_habits
                loadFragment(HabitsFragment())
            }
            else -> {
                android.util.Log.d("MainActivity", "Default case - loading habits fragment")
                // Default to habits fragment
                loadFragment(HabitsFragment())
            }
        }
    }
}