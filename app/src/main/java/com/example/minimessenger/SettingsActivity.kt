package com.example.minimessenger

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import com.google.android.material.slider.Slider
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
        val switchAppLock = findViewById<SwitchMaterial>(R.id.switch_app_lock)
        val switchDesktopMode = findViewById<SwitchMaterial>(R.id.switch_desktop_mode)
        val switchDataSaver = findViewById<SwitchMaterial>(R.id.switch_data_saver)
        val sliderTextZoom = findViewById<Slider>(R.id.slider_text_zoom)

        // Set initial state
        switchDarkMode.isChecked = prefs.getBoolean("dark_mode", true)
        switchHideClutter.isChecked = prefs.getBoolean("hide_clutter", true)
        switchAppLock.isChecked = prefs.getBoolean("app_lock", false)
        switchDesktopMode.isChecked = prefs.getBoolean("desktop_mode", false)
        switchDataSaver.isChecked = prefs.getBoolean("data_saver", false)
        sliderTextZoom.value = prefs.getInt("text_zoom", 100).toFloat()

        // Listeners
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
        }

        switchHideClutter.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("hide_clutter", isChecked).apply()
        }

        switchDesktopMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("desktop_mode", isChecked).apply()
        }

        switchDataSaver.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("data_saver", isChecked).apply()
        }

        sliderTextZoom.addOnChangeListener { _, value, _ ->
            prefs.edit().putInt("text_zoom", value.toInt()).apply()
        }

        switchAppLock.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Verify biometrics are actually available before enabling
                val biometricManager = BiometricManager.from(this)
                val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

                if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                    prefs.edit().putBoolean("app_lock", true).apply()
                } else {
                    // Revert switch and show error
                    buttonView.isChecked = false
                    Toast.makeText(this, "Biometric authentication is not available on this device.", Toast.LENGTH_LONG).show()
                }
            } else {
                prefs.edit().putBoolean("app_lock", false).apply()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
