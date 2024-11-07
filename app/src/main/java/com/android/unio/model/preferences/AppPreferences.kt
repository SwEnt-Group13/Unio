package com.android.unio.model.preferences

/**
 * Constants for the app preferences. The object contains string constants for the keys of the app
 * preferences, as well as nested objects for specific values of the preferences.
 */
object AppPreferences {
  const val THEME = "theme"
  const val NOTIFICATIONS = "notifications"
  const val LOCATION_PERMISSION = "location_permission"
  const val LANGUAGE = "language"

  object Theme {
    const val LIGHT = "light"
    const val DARK = "dark"
    const val SYSTEM = "system"

    val default
      get() = SYSTEM

    val asList = listOf(LIGHT, DARK, SYSTEM)

    fun toDisplayText(theme: String) =
        when (theme) {
          LIGHT -> "Light"
          DARK -> "Dark"
          SYSTEM -> "System Default"
          else -> theme
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
