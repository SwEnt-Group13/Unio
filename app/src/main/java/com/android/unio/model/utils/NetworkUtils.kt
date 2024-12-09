package com.android.unio.model.utils

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat.getSystemService

enum class TextLength(val length: Int) {
  SMALL(30),
  MEDIUM(100),
  LARGE(300)
}

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

object Utils {
  fun checkInputLength(input: String, textLength: TextLength): Boolean {
    return when (textLength) {
      TextLength.SMALL -> input.length <= textLength.length
      TextLength.MEDIUM -> input.length <= textLength.length
      TextLength.LARGE -> input.length <= textLength.length
    }
  }

  fun checkInputLengthIsClose(input: String, textLength: TextLength): Boolean {
    return when (textLength) {
      TextLength.SMALL -> input.length >= (textLength.length - textLength.length / 3)
      TextLength.MEDIUM -> input.length >= (textLength.length - textLength.length / 5)
      TextLength.LARGE -> input.length >= (textLength.length - textLength.length / 10)
    }
  }
}
