package com.android.unio.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import com.android.unio.R
import com.android.unio.model.preferences.PreferenceKeys
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.Theme
import me.zhanghai.compose.preference.LocalPreferenceFlow
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.switchPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navigationAction: NavigationAction) {
  val context = LocalContext.current

  Scaffold(
      modifier = Modifier.testTag("SettingsScreen"),
      topBar = {
        TopAppBar(
            navigationIcon = {
              IconButton(onClick = { navigationAction.goBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Go back")
              }
            },
            title = { Text(context.getString(R.string.settings_title)) })
      }) { padding ->
        Column { Box(modifier = Modifier.padding(padding)) { SettingsContainer() } }
      }
}

@Composable
fun SettingsContainer() {
  val context = LocalContext.current

  ProvidePreferenceLocals(flow = LocalPreferenceFlow.current) {
    LazyColumn(
        modifier = Modifier.testTag("SettingsContainer"),
    ) {
      listPreference(
          modifier = Modifier.testTag(PreferenceKeys.THEME),
          key = PreferenceKeys.THEME,
          title = { Text(context.getString(R.string.settings_theme_title)) },
          valueToText = { AnnotatedString(it) },
          summary = { Text(it) },
          values = listOf(Theme.LIGHT, Theme.DARK, Theme.SYSTEM),
          defaultValue = Theme.SYSTEM,
          icon = {
            Icon(
                imageVector =
                    when (it) {
                      Theme.DARK -> Icons.Default.Nightlight
                      Theme.LIGHT -> Icons.Default.WbSunny
                      else -> Icons.Default.Smartphone
                    },
                contentDescription = context.getString(R.string.settings_theme_content_description))
          })
      switchPreference(
          modifier = Modifier.testTag(PreferenceKeys.NOTIFICATIONS),
          key = PreferenceKeys.NOTIFICATIONS,
          title = { Text(context.getString(R.string.settings_notifications_title)) },
          summary = { Text(context.getString(R.string.settings_notifications_summary)) },
          icon = {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription =
                    context.getString(R.string.settings_notifications_content_description))
          },
          defaultValue = true)
    }
  }
}
