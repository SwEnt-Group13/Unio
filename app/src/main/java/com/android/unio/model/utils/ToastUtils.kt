package com.android.unio.model.utils

import android.content.Context
import android.widget.Toast

object ToastUtils {
  private var currentToast: Toast? = null

  /**
   * Shows a Toast message and ensures no spamming by canceling any currently showing Toast.
   *
   * @param context The context to use for displaying the Toast.
   * @param message The message to display in the Toast.
   * @param duration The duration for which the Toast should be displayed (default: SHORT).
   */
  fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
    currentToast?.cancel()
    currentToast = Toast.makeText(context, message, duration).also { it.show() }
  }
}
