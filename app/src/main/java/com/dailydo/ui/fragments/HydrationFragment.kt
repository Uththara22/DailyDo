package com.dailydo.ui.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dailydo.MainActivity
import com.dailydo.R
import com.dailydo.data.models.HydrationIntake
import com.dailydo.data.models.HydrationSettings
import com.dailydo.data.repository.SharedPreferencesManager
import com.dailydo.receivers.HydrationAlarmScheduler
import com.dailydo.ui.adapters.HydrationHistoryAdapter
import com.dailydo.workers.HydrationReminderWorker
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Fragment for hydration tracking and reminders
 */
class HydrationFragment : Fragment() {
    
    private lateinit var tvDailyGoal: TextView
    private lateinit var tvCurrentIntake: TextView
    private lateinit var tvProgressText: TextView
    private lateinit var circularProgressHydration: com.google.android.material.progressindicator.CircularProgressIndicator
    private lateinit var btnAddWater: MaterialButton
    private lateinit var btnSetReminder: MaterialButton
    private lateinit var recyclerHydrationHistory: RecyclerView
    
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var hydrationHistoryAdapter: HydrationHistoryAdapter
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hydration, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize components
        initializeViews(view)
        setupHydrationHistory()
        setupClickListeners()
        updateHydrationDisplay()
    }
    
    override fun onResume() {
        super.onResume()
        updateHydrationDisplay()
    }
    
    private fun initializeViews(view: View) {
        prefsManager = SharedPreferencesManager.getInstance(requireContext())
        tvDailyGoal = view.findViewById(R.id.tv_daily_goal)
        tvCurrentIntake = view.findViewById(R.id.tv_current_intake)
        tvProgressText = view.findViewById(R.id.tv_progress_text)
        circularProgressHydration = view.findViewById(R.id.circular_progress_hydration)
        btnAddWater = view.findViewById(R.id.btn_add_water)
        btnSetReminder = view.findViewById(R.id.btn_set_reminder)
        view.findViewById<MaterialButton>(R.id.btn_set_goal)?.setOnClickListener {
            showSetGoalDialog()
        }
        recyclerHydrationHistory = view.findViewById(R.id.recycler_hydration_history)
    }
    
    private fun setupHydrationHistory() {
        hydrationHistoryAdapter = HydrationHistoryAdapter()
        
        recyclerHydrationHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = hydrationHistoryAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupClickListeners() {
        btnAddWater.setOnClickListener { showAddWaterDialog() }
        
        btnSetReminder.setOnClickListener {
            showReminderSettingsDialog()
        }
        
        // View stats button removed
        
        // Removed btnViewAll click listener as button was removed from layout
    }

    private fun showSetGoalDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_set_goal, null)

        val etGoal = dialogView.findViewById<TextInputEditText>(R.id.et_goal_ml)
        val btn1500 = dialogView.findViewById<MaterialButton>(R.id.btn_goal_1500)
        val btn2000 = dialogView.findViewById<MaterialButton>(R.id.btn_goal_2000)
        val btn2500 = dialogView.findViewById<MaterialButton>(R.id.btn_goal_2500)

        val currentSettings = prefsManager.getHydrationSettings()
        etGoal.setText(currentSettings.dailyGoalMl.toString())

        val setPreset: (Int) -> Unit = { value ->
            etGoal.setText(value.toString())
        }

        btn1500.setOnClickListener { setPreset(1500) }
        btn2000.setOnClickListener { setPreset(2000) }
        btn2500.setOnClickListener { setPreset(2500) }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.set_goal))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val text = etGoal.text?.toString()
                if (!text.isNullOrBlank()) {
                    try {
                        val goal = text.toInt()
                        val bounded = goal.coerceIn(
                            HydrationSettings.MIN_GOAL,
                            HydrationSettings.MAX_GOAL
                        )
                        if (goal != bounded) {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Goal adjusted to ${bounded}ml (allowed ${HydrationSettings.MIN_GOAL}-${HydrationSettings.MAX_GOAL})",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                        val newSettings = currentSettings.copy(dailyGoalMl = bounded)
                        prefsManager.saveHydrationSettings(newSettings)
                        updateHydrationDisplay()
                    } catch (_: NumberFormatException) {
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Please enter a valid number",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun updateHydrationDisplay() {
        val settings = prefsManager.getHydrationSettings()
        val todayIntake = prefsManager.getTodayTotalHydration()
        val todayIntakeList = prefsManager.getTodayHydrationIntake()
        
        // Update goal and current intake
        tvDailyGoal.text = "${settings.dailyGoalMl} ${getString(R.string.ml_unit)}"
        tvCurrentIntake.text = "$todayIntake ${getString(R.string.ml_unit)}"
        
        // Update progress
        val progressPercentage = if (settings.dailyGoalMl > 0) {
            ((todayIntake.toFloat() / settings.dailyGoalMl.toFloat()) * 100).toInt().coerceAtMost(100)
        } else {
            0
        }
        
        circularProgressHydration.setProgress(progressPercentage, true)
        tvProgressText.text = "$progressPercentage% of Goal Reached"
        
        // Update history - show all entries instead of limiting to 10
        val allIntakes = prefsManager.getHydrationIntake() // Show all entries
        hydrationHistoryAdapter.updateIntakes(allIntakes)
        
        // Check if goal is reached
        if (progressPercentage >= 100) {
            // Show congratulations
            android.widget.Toast.makeText(
                requireContext(),
                getString(R.string.goal_reached),
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun showAddWaterDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_water, null)
        
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.et_water_amount)
        
        // Setup preset buttons
        val btnPreset200 = dialogView.findViewById<MaterialButton>(R.id.btn_preset_200)
        val btnPreset250 = dialogView.findViewById<MaterialButton>(R.id.btn_preset_250)
        val btnPreset300 = dialogView.findViewById<MaterialButton>(R.id.btn_preset_300)
        val btnPreset330 = dialogView.findViewById<MaterialButton>(R.id.btn_preset_330)
        val btnPreset500 = dialogView.findViewById<MaterialButton>(R.id.btn_preset_500)
        val btnPreset750 = dialogView.findViewById<MaterialButton>(R.id.btn_preset_750)
        
        // Set up click listeners for preset buttons
        btnPreset200.setOnClickListener { etAmount.setText("200") }
        btnPreset250.setOnClickListener { etAmount.setText("250") }
        btnPreset300.setOnClickListener { etAmount.setText("300") }
        btnPreset330.setOnClickListener { etAmount.setText("330") }
        btnPreset500.setOnClickListener { etAmount.setText("500") }
        btnPreset750.setOnClickListener { etAmount.setText("750") }
        
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_water))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val amountText = etAmount.text?.toString()
                if (!amountText.isNullOrBlank()) {
                    try {
                        val amount = amountText.toInt()
                        if (amount > 0) {
                            addWaterIntake(amount)
                        } else {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Please enter a valid amount",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: NumberFormatException) {
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Please enter a valid number",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Please enter an amount",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun addWaterIntake(amountMl: Int) {
        val today = dateFormat.format(Date())
        val intake = HydrationIntake(
            date = today,
            amountMl = amountMl,
            timestamp = Date()
        )
        
        prefsManager.addHydrationIntake(intake)
        updateHydrationDisplay()
        
        // Check if goal is reached after adding this intake
        checkAndNotifyGoalReached()
        
        android.widget.Toast.makeText(
            requireContext(),
            "Added ${amountMl}ml of water",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun checkAndNotifyGoalReached() {
        val settings = prefsManager.getHydrationSettings()
        val todayIntake = prefsManager.getTodayTotalHydration()
        
        // Check if user just reached their goal
        if (todayIntake >= settings.dailyGoalMl) {
            sendGoalReachedNotification()
        }
    }
    
    private fun sendGoalReachedNotification() {
        // Create intent to open the app when notification is tapped
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_hydration", true)
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            requireContext(),
            2, // Different request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Get default alarm sound
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        
        // Create notification channel first
        createNotificationChannel()
        
        // Build the goal reached notification
        val notification = NotificationCompat.Builder(requireContext(), "hydration_reminders")
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("Goal Reached! 🎉")
            .setContentText("Congratulations! You've reached your daily hydration goal.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Great job staying hydrated! You've reached your daily water intake goal.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(soundUri) // Add alarm sound for goal reached
            .setVibrate(longArrayOf(0, 500, 250, 500)) // Vibrate pattern
            .build()
        
        // Show the notification
        try {
            NotificationManagerCompat.from(requireContext()).notify(1002, notification)
        } catch (e: SecurityException) {
            // Handle the case where notification permission is not granted
            android.widget.Toast.makeText(
                requireContext(),
                "Please enable notification permission to receive goal alerts",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_hydration_name)
            val descriptionText = getString(R.string.channel_hydration_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("hydration_reminders", name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showReminderSettingsDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_reminder_settings, null)
        
        val tvReminderTime = dialogView.findViewById<TextView>(R.id.tv_reminder_time)
        val tilCustomMinutes = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.til_custom_minutes)
        val etCustomMinutes = dialogView.findViewById<TextInputEditText>(R.id.et_custom_minutes)
        val cardNotificationPermission = dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_notification_permission)
        val btnGrantPermission = dialogView.findViewById<MaterialButton>(R.id.btn_grant_permission)
        
        // Frequency preset buttons
        val btnFreq30 = dialogView.findViewById<MaterialButton>(R.id.btn_freq_30)
        val btnFreq1 = dialogView.findViewById<MaterialButton>(R.id.btn_freq_1)
        val btnFreq2 = dialogView.findViewById<MaterialButton>(R.id.btn_freq_2)
        val btnFreq3 = dialogView.findViewById<MaterialButton>(R.id.btn_freq_3)
        val btnFreq4 = dialogView.findViewById<MaterialButton>(R.id.btn_freq_4)
        val btnFreqCustom = dialogView.findViewById<MaterialButton>(R.id.btn_freq_custom)
        
        val currentSettings = prefsManager.getHydrationSettings()
        
        // Format reminder time (now with minutes)
        val reminderTimeText = String.format("%02d:%02d", currentSettings.startTime, currentSettings.startMinute)
        tvReminderTime.text = reminderTimeText
        
        // Variable to track selected frequency
        var selectedFrequency = currentSettings.reminderIntervalMinutes
        
        // Variable to track selected time (hour and minute)
        var selectedHour = currentSettings.startTime
        var selectedMinute = currentSettings.startMinute
        
        // Set up frequency button listeners
        btnFreq30.setOnClickListener { 
            selectedFrequency = 30
            etCustomMinutes.setText("30")
            // Update button states
            btnFreq30.setStrokeColorResource(R.color.primary_green)
            btnFreq1.setStrokeColorResource(R.color.text_hint)
            btnFreq2.setStrokeColorResource(R.color.text_hint)
            btnFreq3.setStrokeColorResource(R.color.text_hint)
            btnFreq4.setStrokeColorResource(R.color.text_hint)
            btnFreqCustom.setStrokeColorResource(R.color.text_hint)
        }
        
        btnFreq1.setOnClickListener { 
            selectedFrequency = 60
            etCustomMinutes.setText("60")
            // Update button states
            btnFreq30.setStrokeColorResource(R.color.text_hint)
            btnFreq1.setStrokeColorResource(R.color.primary_green)
            btnFreq2.setStrokeColorResource(R.color.text_hint)
            btnFreq3.setStrokeColorResource(R.color.text_hint)
            btnFreq4.setStrokeColorResource(R.color.text_hint)
            btnFreqCustom.setStrokeColorResource(R.color.text_hint)
        }
        
        btnFreq2.setOnClickListener { 
            selectedFrequency = 120
            etCustomMinutes.setText("120")
            // Update button states
            btnFreq30.setStrokeColorResource(R.color.text_hint)
            btnFreq1.setStrokeColorResource(R.color.text_hint)
            btnFreq2.setStrokeColorResource(R.color.primary_green)
            btnFreq3.setStrokeColorResource(R.color.text_hint)
            btnFreq4.setStrokeColorResource(R.color.text_hint)
            btnFreqCustom.setStrokeColorResource(R.color.text_hint)
        }
        
        btnFreq3.setOnClickListener { 
            selectedFrequency = 180
            etCustomMinutes.setText("180")
            // Update button states
            btnFreq30.setStrokeColorResource(R.color.text_hint)
            btnFreq1.setStrokeColorResource(R.color.text_hint)
            btnFreq2.setStrokeColorResource(R.color.text_hint)
            btnFreq3.setStrokeColorResource(R.color.primary_green)
            btnFreq4.setStrokeColorResource(R.color.text_hint)
            btnFreqCustom.setStrokeColorResource(R.color.text_hint)
        }
        
        btnFreq4.setOnClickListener { 
            selectedFrequency = 240
            etCustomMinutes.setText("240")
            // Update button states
            btnFreq30.setStrokeColorResource(R.color.text_hint)
            btnFreq1.setStrokeColorResource(R.color.text_hint)
            btnFreq2.setStrokeColorResource(R.color.text_hint)
            btnFreq3.setStrokeColorResource(R.color.text_hint)
            btnFreq4.setStrokeColorResource(R.color.primary_green)
            btnFreqCustom.setStrokeColorResource(R.color.text_hint)
        }
        
        btnFreqCustom.setOnClickListener { 
            selectedFrequency = 60 // Default custom to 1 hour
            etCustomMinutes.setText("60")
            // Update button states
            btnFreq30.setStrokeColorResource(R.color.text_hint)
            btnFreq1.setStrokeColorResource(R.color.text_hint)
            btnFreq2.setStrokeColorResource(R.color.text_hint)
            btnFreq3.setStrokeColorResource(R.color.text_hint)
            btnFreq4.setStrokeColorResource(R.color.text_hint)
            btnFreqCustom.setStrokeColorResource(R.color.primary_green)
        }
        
        // Set initial selected button based on current settings
        when (currentSettings.reminderIntervalMinutes) {
            30 -> {
                selectedFrequency = 30
                btnFreq30.setStrokeColorResource(R.color.primary_green)
            }
            60 -> {
                selectedFrequency = 60
                btnFreq1.setStrokeColorResource(R.color.primary_green)
            }
            120 -> {
                selectedFrequency = 120
                btnFreq2.setStrokeColorResource(R.color.primary_green)
            }
            180 -> {
                selectedFrequency = 180
                btnFreq3.setStrokeColorResource(R.color.primary_green)
            }
            240 -> {
                selectedFrequency = 240
                btnFreq4.setStrokeColorResource(R.color.primary_green)
            }
            else -> {
                selectedFrequency = currentSettings.reminderIntervalMinutes
                btnFreqCustom.setStrokeColorResource(R.color.primary_green)
                etCustomMinutes.setText(selectedFrequency.toString())
            }
        }
        
        // Set up time picker (now with minutes)
        tvReminderTime.setOnClickListener {
            val timePicker = TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    selectedHour = hour
                    selectedMinute = minute
                    tvReminderTime.text = String.format("%02d:%02d", hour, minute)
                },
                selectedHour,
                selectedMinute,
                true
            )
            timePicker.show()
        }
        
        // Show the notification permission card and adapt action based on current state
        val notificationsEnabled = checkNotificationPermission()
        cardNotificationPermission.visibility = View.VISIBLE
        if (notificationsEnabled) {
            // If already enabled, let user manage settings
            btnGrantPermission.text = getString(R.string.manage)
            btnGrantPermission.setOnClickListener { openAppNotificationSettings() }
        } else {
            // If not enabled, request permission
            btnGrantPermission.text = getString(R.string.enable)
            btnGrantPermission.setOnClickListener { requestNotificationPermission() }
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.set_reminder))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                // Manual time input removed by request; rely on picker

                // If user typed custom minutes, prefer that
                val typed = etCustomMinutes.text?.toString()?.toIntOrNull()
                if (typed != null) {
                    selectedFrequency = typed.coerceIn(15, 720)
                }

                val newSettings = currentSettings.copy(
                    reminderIntervalMinutes = selectedFrequency,
                    startTime = selectedHour,
                    startMinute = selectedMinute,
                    endTime = if (selectedHour + 1 > 23) 23 else selectedHour + 1
                )

                prefsManager.saveHydrationSettings(newSettings)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (HydrationAlarmScheduler.canScheduleExactAlarms(requireContext())) {
                        HydrationAlarmScheduler.scheduleRecurringAlarmWithMinutes(
                            requireContext(), newSettings, selectedMinute
                        )
                    } else {
                        HydrationAlarmScheduler.requestExactAlarmPermission(requireContext())
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Please grant exact alarm permission in settings",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        return@setPositiveButton
                    }
                } else {
                    HydrationAlarmScheduler.scheduleRecurringAlarmWithMinutes(
                        requireContext(), newSettings, selectedMinute
                    )
                }

                updateHydrationDisplay()

                val nextAlarmText = HydrationAlarmScheduler.getNextAlarmTimeFormatted(requireContext())
                android.widget.Toast.makeText(
                    requireContext(),
                    "Settings saved. $nextAlarmText",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permissions are automatically granted on older versions
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Notifications already enabled",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return
            }

            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (shouldShowRationale) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            } else {
                // User has denied with "Don't ask again" or first time on some OEMs -> open settings
                openAppNotificationSettings()
            }
        }
    }
    
    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1001 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Notification permission granted",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Notification permission denied. You can enable it in settings",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun openAppNotificationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent().apply {
                action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            }
            runCatching { startActivity(intent) }
                .onFailure {
                    // Fallback to app details
                    val appDetails = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.fromParts("package", requireContext().packageName, null)
                    }
                    runCatching { startActivity(appDetails) }
                }
        } else {
            val appDetails = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", requireContext().packageName, null)
            }
            runCatching { startActivity(appDetails) }
        }
    }
    
    private fun setupHydrationReminders(settings: HydrationSettings) {
        if (!settings.reminderEnabled) {
            HydrationAlarmScheduler.cancelAlarm(requireContext())
            return
        }
        
        // Check for exact alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (HydrationAlarmScheduler.canScheduleExactAlarms(requireContext())) {
                // Use the new alarm scheduler
                HydrationAlarmScheduler.scheduleRecurringAlarm(requireContext(), settings)
            }
            // If we can't schedule exact alarms, the alarms will remain unscheduled
            // The user will need to manually enable them in the app
        } else {
            // For older versions, directly schedule the alarm
            HydrationAlarmScheduler.scheduleRecurringAlarm(requireContext(), settings)
        }
    }
}