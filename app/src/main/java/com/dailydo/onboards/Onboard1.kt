package com.dailydo.onboards

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dailydo.R
import com.dailydo.ui.welcome.WelcomeActivity

class Onboard1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboard1)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        val nextButton = findViewById<Button>(R.id.nextButton)
        nextButton.setOnClickListener {
            val intent = Intent(this, Onboard2::class.java)
            startActivity(intent)
            finish()
        }
		val skipView = findViewById<View>(R.id.btn_skip)
		skipView.setOnClickListener {
			val intent = Intent(this, WelcomeActivity::class.java)
			startActivity(intent)
			finish()
		}

		val logo = findViewById<ImageView>(R.id.logo_small)
		logo.setOnClickListener {
			val intent = Intent(this, Onboard2::class.java)
			startActivity(intent)
			finish()
		}
    }
}