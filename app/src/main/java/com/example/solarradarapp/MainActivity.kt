package com.example.solarradarapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.example.solarradarapp.navigation.AppNavigation
import com.example.solarradarapp.ui.strings.EnglishStrings
import com.example.solarradarapp.ui.strings.LithuanianStrings
import com.example.solarradarapp.ui.strings.LocalAppStrings
import com.example.solarradarapp.ui.theme.SolarRadarAppTheme
import com.example.solarradarapp.util.LanguageManager
import com.example.solarradarapp.util.NotificationHelper
import com.example.solarradarapp.util.ThemeManager
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // No special action needed — the system handles the result
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageManager.init(this)
        ThemeManager.init(this)

        // Create the notification channel (safe to call multiple times)
        NotificationHelper.createChannel(this)

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // If the user is already authenticated (session restored), register the FCM token.
        // For fresh logins this is handled in LoginViewModel after auth succeeds.
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            NotificationHelper.registerToken(uid)
        }

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
