package com.android.unio.resources

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object ResourceManager {
  private lateinit var context: Context

  fun init(context: Context) {
    this.context = context
  }

  fun getString(resId: Int): String { // good for files that do not have any Application Context
    if (!::context.isInitialized) {
      throw UninitializedPropertyAccessException(
          "ResourceManager is not initialized. Call init(context) before using it.")
    }
    return context.getString(resId)
  }
}
