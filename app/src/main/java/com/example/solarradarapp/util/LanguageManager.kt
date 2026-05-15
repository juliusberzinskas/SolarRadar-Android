package com.example.solarradarapp.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object LanguageManager {

    private const val PREFS_NAME = "solar_radar_prefs"
    private const val KEY_LANGUAGE = "language"
    const val LANG_EN = "en"
    const val LANG_LT = "lt"

    private lateinit var prefs: android.content.SharedPreferences
    private val _currentLanguage = MutableStateFlow(LANG_EN)
    val currentLanguage: StateFlow<String> = _currentLanguage

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _currentLanguage.value = prefs.getString(KEY_LANGUAGE, LANG_EN) ?: LANG_EN
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _currentLanguage.value = lang
    }
}
