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

fun checkInputLengthShort(input: String) : Boolean {
  return input.length in 1..29
}

fun checkInputLengthLong(input: String) : Boolean {
  return input.length in 1..199
}