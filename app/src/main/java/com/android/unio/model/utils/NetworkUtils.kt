package com.android.unio.model.utils

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat.getSystemService

object NetworkUtils {

  /**
   * Checks if the device is connected to the internet.
   *
   * @param context [Context] : The context of the application.
   * @return True if the application is connected to the internet, false otherwise.
   */
  fun checkInternetConnection(context: Context): Boolean {
    val connectivityManager = getSystemService(context, ConnectivityManager::class.java)
    return connectivityManager?.activeNetwork != null
  }
}
