package com.dailydo.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dailydo.MainActivity
import com.dailydo.R
import com.dailydo.data.repository.SharedPreferencesManager
import com.dailydo.ui.welcome.WelcomeActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {
    
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var prefsManager: SharedPreferencesManager  // Add this line
    private lateinit var ivPasswordToggle: android.widget.ImageView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Initialize prefsManager
        prefsManager = SharedPreferencesManager.getInstance(this)
        
        initializeViews()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        etEmail = findViewById(R.id.edit_text_name)
        etPassword = findViewById(R.id.password_input2)
        btnLogin = findViewById(R.id.button_sign_in)
        tvRegister = findViewById(R.id.text_sign_up)
        ivPasswordToggle = findViewById(R.id.password_toggle2)
    }
    
    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            login()
        }
        
        tvRegister.setOnClickListener {
            // Navigate to RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Back area/arrow navigates to Welcome screen
        findViewById<android.view.View>(R.id.back_layout)?.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        findViewById<android.view.View>(R.id.back_arrow)?.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Toggle password visibility
        ivPasswordToggle.setOnClickListener {
            val isHidden = etPassword.transformationMethod is android.text.method.PasswordTransformationMethod
            if (isHidden) {
                etPassword.transformationMethod = null
                ivPasswordToggle.setImageResource(R.drawable.ic_visibility)
            } else {
                etPassword.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                ivPasswordToggle.setImageResource(R.drawable.ic_visibility_off)
            }
            etPassword.setSelection(etPassword.text.length)
        }
    }
    
    private fun login() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        
        // Validate inputs
        if (!validateInputs(email, password)) {
            return
        }

        // For development: allow any email/password combination
        if (!prefsManager.isUserRegistered()) {
            // Auto-register for development
            val registered = prefsManager.registerUser("Dev User", email, password)
            android.util.Log.d("LoginActivity", "Auto-registered: $registered")
        }

        val ok = prefsManager.validateLogin(email, password)
        android.util.Log.d("LoginActivity", "Login validation: $ok")
        if (!ok) {
            etEmail.error = "Invalid email or password"
            etPassword.error = "Invalid email or password"
            return
        }

        // Successful login
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
        prefsManager.setUserLoggedIn(true)
        
        // Verify login state was saved
        val loginState = prefsManager.isUserLoggedIn()
        android.util.Log.d("LoginActivity", "Login state saved: $loginState")
        
        // Small delay to ensure SharedPreferences are saved
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            intent.putExtra("open_habits", true)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            android.util.Log.d("LoginActivity", "Starting MainActivity with open_habits: true")
            startActivity(intent)
            finish()
        }, 100) // 100ms delay
    }
    
    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true
        
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Please enter a valid email"
            isValid = false
        } else {
            etEmail.error = null
        }
        
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            etPassword.error = null
        }
        
        return isValid
    }
    
    // Removed simulateLogin; now validating against stored credentials
}