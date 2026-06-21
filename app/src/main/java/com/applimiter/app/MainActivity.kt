package com.applimiter.app

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs: SharedPreferences = getSharedPreferences("applimiter_prefs", MODE_PRIVATE)
        val minutesInput = findViewById<EditText>(R.id.minutesInput)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val accessibilityButton = findViewById<Button>(R.id.accessibilityButton)
        val usageAccessButton = findViewById<Button>(R.id.usageAccessButton)

        val savedMinutes = prefs.getInt("limitMinutes", 10)
        minutesInput.setText(savedMinutes.toString())

        saveButton.setOnClickListener {
            val minutes = minutesInput.text.toString().toIntOrNull() ?: 10
            prefs.edit().putInt("limitMinutes", minutes).apply()
            Toast.makeText(this, "Saved: $minutes minutes per hour", Toast.LENGTH_SHORT).show()
        }

        accessibilityButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        usageAccessButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }
}
