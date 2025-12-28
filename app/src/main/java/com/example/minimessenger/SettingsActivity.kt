package com.example.minimessenger

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)

        val prefs = getSharedPreferences("MiniMessengerPrefs", Context.MODE_PRIVATE)

        val switchDarkMode = findViewById<SwitchMaterial>(R.id.switch_dark_mode)
        val switchHideClutter = findViewById<SwitchMaterial>(R.id.switch_hide_clutter)

        // Set initial state
        switchDarkMode.isChecked = prefs.getBoolean("dark_mode", true)
        switchHideClutter.isChecked = prefs.getBoolean("hide_clutter", true)

        // Listeners
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
        }

        switchHideClutter.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("hide_clutter", isChecked).apply()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
