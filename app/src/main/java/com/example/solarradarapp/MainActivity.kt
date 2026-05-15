package com.example.solarradarapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import com.example.solarradarapp.navigation.AppNavigation
import com.example.solarradarapp.ui.strings.EnglishStrings
import com.example.solarradarapp.ui.strings.LithuanianStrings
import com.example.solarradarapp.ui.strings.LocalAppStrings
import com.example.solarradarapp.ui.theme.SolarRadarAppTheme
import com.example.solarradarapp.util.LanguageManager
import com.example.solarradarapp.util.ThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageManager.init(this)
        ThemeManager.init(this)
        enableEdgeToEdge()
        setContent {
            val language by LanguageManager.currentLanguage.collectAsState()
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()
            val strings = if (language == LanguageManager.LANG_LT) LithuanianStrings else EnglishStrings

            CompositionLocalProvider(LocalAppStrings provides strings) {
                SolarRadarAppTheme(darkTheme = isDarkMode) {
                    AppNavigation()
                }
            }
        }
    }
}
