package com.android.unio.model.utils

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat.getSystemService

object Utils {
  const val MIN_INPUT_LENGTH = 1
  const val MAX_INPUT_LENGTH_SHORT = 30
  const val MAX_INPUT_LENGTH_LONG = 200


  fun checkInternetConnection(context: Context): Boolean {
    val connectivityManager = getSystemService(context, ConnectivityManager::class.java)
    return connectivityManager?.activeNetwork != null
  }

  fun checkInputLengthShort(input: String) : Boolean {
    return input.length <= MAX_INPUT_LENGTH_SHORT
  }

  fun checkInputLengthLong(input: String) : Boolean {
    return input.length <= MAX_INPUT_LENGTH_LONG
  }

//  fun checkInputLengthIsClose(input: String): Boolean{
//    return input.length in MIN_INPUT_LENGTH..MAX_INPUT_LENGTH_SHORT
//  }
}