package com.android.unio.components.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.unio.TearDown
import com.android.unio.model.preferences.AppPreferences
import com.android.unio.ui.theme.AppTheme
import com.android.unio.ui.theme.primaryDark
import com.android.unio.ui.theme.primaryLight
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import me.zhanghai.compose.preference.MutablePreferences
import me.zhanghai.compose.preference.Preferences
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.junit.Rule
import org.junit.Test

class ThemeTest : TearDown() {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun testLightTheme() {
        val preferencesFlow: MutableStateFlow<Preferences> =
            MutableStateFlow(MapPreferences(mapOf(AppPreferences.THEME to AppPreferences.Theme.LIGHT)))

        composeTestRule.setContent {
            ProvidePreferenceLocals(flow = preferencesFlow) {
                AppTheme { assertEquals(primaryLight, MaterialTheme.colorScheme.primary) }
            }
        }
    }

    @Test
    fun testDarkTheme() {
        val preferencesFlow: MutableStateFlow<Preferences> =
            MutableStateFlow(MapPreferences(mapOf(AppPreferences.THEME to AppPreferences.Theme.DARK)))

        composeTestRule.setContent {
            ProvidePreferenceLocals(flow = preferencesFlow) {
                AppTheme { assertEquals(primaryDark, MaterialTheme.colorScheme.primary) }
            }
        }
    }

    @Test
    fun testSystemTheme() {
        val preferencesFlow: MutableStateFlow<Preferences> =
            MutableStateFlow(MapPreferences(mapOf(AppPreferences.THEME to AppPreferences.Theme.SYSTEM)))

        composeTestRule.setContent {
            ProvidePreferenceLocals(flow = preferencesFlow) {
                AppTheme {
                    if (isSystemInDarkTheme()) {
                        assertEquals(primaryDark, MaterialTheme.colorScheme.primary)
                    } else {
                        assertEquals(primaryLight, MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    class MapMutablePreferences(private val map: MutableMap<String, Any> = mutableMapOf()) :
        MutablePreferences {
        @Suppress("UNCHECKED_CAST") override fun <T> get(key: String): T? = map[key] as T?

        override fun asMap(): Map<String, Any> = map

        override fun toMutablePreferences(): MutablePreferences =
            MapMutablePreferences(map.toMutableMap())

        override fun <T> set(key: String, value: T?) {
            if (value != null) {
                map[key] = value
            } else {
                map -= key
            }
        }

        override fun clear() {
            map.clear()
        }
    }

    class MapPreferences(private val map: Map<String, Any> = emptyMap()) : Preferences {
        @Suppress("UNCHECKED_CAST") override fun <T> get(key: String): T? = map[key] as T?

        override fun asMap(): Map<String, Any> = map

        override fun toMutablePreferences(): MutablePreferences =
            MapMutablePreferences(map.toMutableMap())
    }
}