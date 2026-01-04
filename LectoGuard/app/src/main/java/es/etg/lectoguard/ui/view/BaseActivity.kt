package es.etg.lectoguard.ui.view

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import es.etg.lectoguard.R
import es.etg.lectoguard.utils.PrefsHelper

abstract class BaseActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
    }
    
    private fun applyTheme() {
        val isDarkMode = PrefsHelper.isDarkModeEnabled(this)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    
    fun refreshTheme() {
        // Aplicar el tema sin recrear la activity para evitar problemas
        val isDarkMode = PrefsHelper.isDarkModeEnabled(this)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        // Solo recrear si realmente es necesario y después de un pequeño delay para evitar problemas
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val shouldBeDark = isDarkMode && currentNightMode != android.content.res.Configuration.UI_MODE_NIGHT_YES
        val shouldBeLight = !isDarkMode && currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
        
        if (shouldBeDark || shouldBeLight) {
            // Usar post para evitar problemas de recreación inmediata
            window.decorView.post {
                recreate()
            }
        }
    }
}

