package com.android.unio.model.utils

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat.getSystemService

enum class TextSize(val size: Int){
    SMALL(30),
    MEDIUM(100),
    LARGE(300)
}

object Utils {

  fun checkInternetConnection(context: Context): Boolean {
    val connectivityManager = getSystemService(context, ConnectivityManager::class.java)
    return connectivityManager?.activeNetwork != null
  }

  fun checkInputLength(input: String, textSize: TextSize) : Boolean {
    return when(textSize) {
      TextSize.SMALL -> input.length <= textSize.size
      TextSize.MEDIUM -> input.length <= textSize.size
      TextSize.LARGE -> input.length <= textSize.size
    }
  }


  fun checkInputLengthIsClose(input: String, textSize: TextSize) : Boolean {
    return when(textSize) {
      TextSize.SMALL -> input.length >= (textSize.size - textSize.size/30)
      TextSize.MEDIUM -> input.length >= (textSize.size - textSize.size/20)
      TextSize.LARGE -> input.length >= (textSize.size - textSize.size/10)
    }
  }
}