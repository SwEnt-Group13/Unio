package com.android.unio.model.preferences

import com.android.unio.R
import me.zhanghai.compose.preference.Preferences

/**
 * Constants for the app preferences. The object contains string constants for the keys of the app
 * preferences, as well as nested objects for specific values of the preferences.
 */
object AppPreferences {
  const val THEME = "theme"
  const val NOTIFICATIONS = "notifications"
  const val LOCATION_PERMISSION = "location_permission"
  const val LANGUAGE = "language"
  const val RESET_PASSWORD = "reset_password"

  object Notification {
    val default
      get() = true
  }

  object Theme {
    const val LIGHT = "light"
    const val DARK = "dark"
    const val SYSTEM = "system"

    val default
      get() = SYSTEM

    val asList = listOf(LIGHT, DARK, SYSTEM)

    fun toDisplayText(theme: String) =
        when (theme) {
          LIGHT -> R.string.app_preferences_theme_text_display_light
          DARK -> R.string.app_preferences_theme_text_display_dark
          else -> R.string.app_preferences_theme_text_display_system_default_auto
        }
  }

  object Language {
    const val ENGLISH = "en"
    const val FRENCH = "fr"

    val default
      get() = ENGLISH

    val asList = listOf(ENGLISH, FRENCH)

    fun toDisplayText(language: String) =
        when (language) {
          ENGLISH -> "English"
          FRENCH -> "Français"
          else -> language
        }
  }
}

inline fun <reified T> Preferences.getOrDefault(key: String, defaultValue: T): T {
  return this.asMap().getOrDefault(key, defaultValue) as? T ?: defaultValue
}
