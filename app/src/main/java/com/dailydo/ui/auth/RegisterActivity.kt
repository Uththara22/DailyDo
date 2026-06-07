package com.dailydo.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dailydo.MainActivity
import com.dailydo.R
import com.dailydo.data.repository.SharedPreferencesManager
import androidx.core.widget.addTextChangedListener
// Using plain EditText views in the current register layout
import com.dailydo.ui.welcome.WelcomeActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var actvGender: AutoCompleteTextView
    private lateinit var etPhone: EditText
    private lateinit var cbTerms: CheckBox
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var ivPasswordToggle: android.widget.ImageView
    private lateinit var ivConfirmPasswordToggle: android.widget.ImageView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        // Initialize prefsManager
        prefsManager = SharedPreferencesManager.getInstance(this)
        
        initializeViews()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        etName = findViewById(R.id.edit_text_name)
        etEmail = findViewById(R.id.edit_text_email)
        actvGender = findViewById(R.id.edit_text_gender)
        etPhone = findViewById(R.id.edit_text_phone)
        etPassword = findViewById(R.id.edit_text_password)
        etConfirmPassword = findViewById(R.id.edit_text_confirm_password)
        ivPasswordToggle = findViewById(R.id.password_toggle_reg)
        ivConfirmPasswordToggle = findViewById(R.id.confirm_password_toggle_reg)
        cbTerms = findViewById(R.id.checkbox_terms)
        btnRegister = findViewById(R.id.button_sign_up)
        tvLogin = findViewById(R.id.text_sign_in)

        // Configure gender dropdown with Male/Female and open on click
        val genderItems = resources.getStringArray(R.array.gender_options).toList()
        val genderAdapter = ArrayAdapter(this, R.layout.dropdown_item_white, genderItems)
        actvGender.setAdapter(genderAdapter)
        actvGender.setOnClickListener { actvGender.showDropDown() }
        actvGender.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) actvGender.showDropDown() }

        // Live validation listeners
        etEmail.addTextChangedListener { text ->
            val msg = validateEmail(text?.toString()?.trim().orEmpty())
            etEmail.error = msg
        }

        etPhone.addTextChangedListener { text ->
            val value = text?.toString()?.trim().orEmpty()
            // Only show error when there is input, avoid noise on empty field until submit
            etPhone.error = if (value.isEmpty()) null else validatePhone(value)
        }

        etName.addTextChangedListener { text ->
            val t = text?.toString()?.trim().orEmpty()
            etName.error = if (t.isEmpty()) "Name is required" else null
        }
        // Password visibility toggles
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
        ivConfirmPasswordToggle.setOnClickListener {
            val isHidden = etConfirmPassword.transformationMethod is android.text.method.PasswordTransformationMethod
            if (isHidden) {
                etConfirmPassword.transformationMethod = null
                ivConfirmPasswordToggle.setImageResource(R.drawable.ic_visibility)
            } else {
                etConfirmPassword.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                ivConfirmPasswordToggle.setImageResource(R.drawable.ic_visibility_off)
            }
            etConfirmPassword.setSelection(etConfirmPassword.text.length)
        }
    }
    
    private fun setupClickListeners() {
        btnRegister.setOnClickListener { register() }

        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Back arrow/area navigates to Welcome screen
        findViewById<View>(R.id.back_layout)?.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        findViewById<View>(R.id.back_arrow)?.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    private fun register() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val gender = actvGender.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (!validateInputs(name, email, gender, phone, cbTerms.isChecked)) return

        if (password.length < 8) {
            etPassword.error = "Password must be at least 8 characters"
            return
        } else {
            etPassword.error = null
        }
        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            return
        } else {
            etConfirmPassword.error = null
        }

        val existing = prefsManager.getRegisteredEmail()
        if (!existing.isNullOrEmpty() && existing.equals(email, ignoreCase = true)) {
            etEmail.error = "An account with this email already exists"
            return
        }

        val success = prefsManager.registerUser(name, email, password)
        if (!success) {
            Toast.makeText(this, "Registration failed. Try a different email.", Toast.LENGTH_SHORT).show()
            return
        }

        // Verify it saved correctly
        if (!prefsManager.validateLogin(email, password)) {
            Toast.makeText(this, "Registration error. Please try again.", Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("prefill_email", email)
        startActivity(intent)
        finish()
    }
    
    private fun validateInputs(name: String, email: String, gender: String, phone: String, termsAccepted: Boolean): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            etName.error = "Name is required"
            isValid = false
        } else {
            etName.error = null
        }

        val emailValidation = validateEmail(email)
        if (emailValidation != null) {
            etEmail.error = emailValidation
            isValid = false
        } else {
            etEmail.error = null
        }

        if (gender.isEmpty()) {
            actvGender.error = "Please select your gender"
            isValid = false
        } else {
            actvGender.error = null
        }

        val phoneValidation = validatePhone(phone)
        if (phoneValidation != null) {
            etPhone.error = phoneValidation
            isValid = false
        } else {
            etPhone.error = null
        }

        if (!termsAccepted) {
            Toast.makeText(this, "Please accept Terms & Privacy", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun validatePhone(input: String): String? {
        if (input.isEmpty()) return "Mobile number is required"

        // Allow spaces, dashes, parentheses; keep optional leading +
        val compact = input.replace(Regex("[\\s-()]"), "")

        // Only one optional '+' and only at start
        if (compact.count { it == '+' } > 1 || (compact.contains('+') && !compact.startsWith("+"))) {
            return "Invalid characters in phone number"
        }

        // Sri Lanka mobile formats (UI shows +94 separately): require 9 local digits
        // Pattern: (70|71|72|74|75|76|77|78)XXXXXXX => 9 digits
        val mobilePrefixes = listOf("70","71","72","74","75","76","77","78")

        if (!compact.matches(Regex("^[0-9]{1,9}$"))) return "Enter 9 digits"
        if (compact.length < 9) return "Enter 9 digits"
        if (compact[0] == '0') return "Do not include leading 0 when +94 is shown"
        val prefix = compact.substring(0, 2)
        if (prefix !in mobilePrefixes) return "Allowed prefixes: 70/71/72/74/75/76/77/78"

        // Reject trivial numbers (all same digit excluding optional country code and leading 0)
        val digitsOnly = compact
        if (digitsOnly.isNotEmpty() && digitsOnly.all { it == digitsOnly[0] }) return "Number looks invalid"

        return null
    }

    private fun validateEmail(input: String): String? {
        if (input.isEmpty()) return "Email is required"

        // Quick Android pattern check
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            return "Please enter a valid email"
        }

        // Additional constraints
        if (input.contains(" ")) return "Email cannot contain spaces"
        if (input.length > 254) return "Email is too long"

        val parts = input.split("@")
        if (parts.size != 2) return "Invalid email format"

        val local = parts[0]
        val domain = parts[1]

        if (local.isEmpty() || local.length > 64) return "Invalid local part"
        if (local.startsWith('.') || local.endsWith('.')) return "Local part cannot start/end with dot"
        if (local.contains("..")) return "Local part has consecutive dots"

        // Basic safe char set for local-part (RFC is broader, we keep it pragmatic)
        val localOk = Regex("^[A-Za-z0-9!#$%&'*+/=?^_`{|}~.-]+$")
        if (!localOk.matches(local)) return "Invalid characters in email"

        // Domain rules: labels 1-63 chars, alnum + hyphen, not starting/ending with hyphen
        val labels = domain.split('.')
        if (labels.any { it.isEmpty() }) return "Domain has empty label"
        for (label in labels) {
            if (label.length !in 1..63) return "Domain label length invalid"
            if (!Regex("^[A-Za-z0-9-]+$").matches(label)) return "Invalid domain characters"
            if (label.startsWith('-') || label.endsWith('-')) return "Domain label cannot start/end with -"
        }
        if (labels.last().length < 2) return "Top-level domain too short"

        return null
    }
    
    // Removed simulateRegistration; now using real SharedPreferences-backed registration
}