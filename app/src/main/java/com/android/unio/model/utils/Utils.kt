package com.android.unio.model.utils

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat.getSystemService

object Utils {

  fun checkInternetConnection(context: Context): Boolean {
    val connectivityManager = getSystemService(context, ConnectivityManager::class.java)
    return connectivityManager?.activeNetwork != null
  }
}
