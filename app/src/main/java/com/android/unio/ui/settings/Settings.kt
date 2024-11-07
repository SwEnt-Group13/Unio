package com.android.unio.ui.settings

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import com.android.unio.R
import com.android.unio.model.preferences.AppPreferences
import com.android.unio.ui.navigation.NavigationAction
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.util.Locale
import me.zhanghai.compose.preference.LocalPreferenceFlow
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.preference
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
                    contentDescription =
                        context.getString(R.string.settings_back_content_description))
              }
            },
            title = { Text(context.getString(R.string.settings_title)) })
      }) { padding ->
        Column { Box(modifier = Modifier.padding(padding)) { SettingsContainer() } }
      }
}

/**
 * This composable makes use of the ComposePreference API. See
 * https://github.com/zhanghai/ComposePreference
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsContainer() {
  val context = LocalContext.current
  val preferences by LocalPreferenceFlow.current.collectAsState()

  /** Location Permissions * */
  val locationPermissions =
      rememberMultiplePermissionsState(
          permissions =
              listOf(
                  Manifest.permission.ACCESS_FINE_LOCATION,
                  Manifest.permission.ACCESS_COARSE_LOCATION))
  val requestPermissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
        locationPermissions.launchMultiplePermissionRequest()
      }

  /** Language * */
  val language = preferences.get<String>(AppPreferences.LANGUAGE) ?: AppPreferences.Language.default
  val locale = Locale(language)
  Locale.setDefault(locale)

  val configuration = context.resources.configuration
  configuration.setLocale(locale)
  configuration.setLayoutDirection(locale)
  context.createConfigurationContext(configuration)
  context.resources.updateConfiguration(configuration, context.resources.displayMetrics)

  ProvidePreferenceLocals(flow = LocalPreferenceFlow.current) {
    LazyColumn(
        modifier = Modifier.testTag("SettingsContainer"),
    ) {
      listPreference(
          modifier = Modifier.testTag(AppPreferences.THEME),
          key = AppPreferences.THEME,
          title = { Text(context.getString(R.string.settings_theme_title)) },
          summary = { Text(AppPreferences.Theme.toDisplayText(it)) },
          valueToText = { AnnotatedString(AppPreferences.Theme.toDisplayText(it)) },
          values = AppPreferences.Theme.asList,
          defaultValue = AppPreferences.Theme.default,
          icon = {
            Icon(
                imageVector =
                    when (it) {
                      AppPreferences.Theme.DARK -> Icons.Default.Nightlight
                      AppPreferences.Theme.LIGHT -> Icons.Default.WbSunny
                      else -> Icons.Default.Smartphone
                    },
                contentDescription = context.getString(R.string.settings_theme_content_description))
          })
      listPreference(
          modifier = Modifier.testTag(AppPreferences.LANGUAGE),
          key = AppPreferences.LANGUAGE,
          title = { Text(context.getString(R.string.settings_language_title)) },
          summary = { Text(AppPreferences.Language.toDisplayText(it)) },
          valueToText = { AnnotatedString(AppPreferences.Language.toDisplayText(it)) },
          values = AppPreferences.Language.asList,
          defaultValue = AppPreferences.Language.default,
          icon = {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription =
                    context.getString(R.string.settings_language_content_description))
          })
      switchPreference(
          modifier = Modifier.testTag(AppPreferences.NOTIFICATIONS),
          key = AppPreferences.NOTIFICATIONS,
          title = { Text(context.getString(R.string.settings_notifications_title)) },
          summary = { Text(context.getString(R.string.settings_notifications_summary)) },
          icon = {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription =
                    context.getString(R.string.settings_notifications_content_description))
          },
          defaultValue = true)
      preference(
          modifier = Modifier.testTag(AppPreferences.LOCATION_PERMISSION),
          key = AppPreferences.LOCATION_PERMISSION,
          title = { Text(context.getString(R.string.settings_location_title)) },
          summary = {
            if (locationPermissions.allPermissionsGranted) {
              Text(context.getString(R.string.settings_location_summary_granted))
            } else {
              Text(context.getString(R.string.settings_location_summary_denied))
            }
          },
          icon = {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription =
                    context.getString(R.string.settings_location_content_description))
          },
          onClick = {
            if (!locationPermissions.allPermissionsGranted) {
              requestPermissionLauncher.launch(
                  arrayOf(
                      Manifest.permission.ACCESS_FINE_LOCATION,
                      Manifest.permission.ACCESS_COARSE_LOCATION))
            }
          })
    }
  }
}
