package com.android.unio.model.event

import androidx.compose.ui.graphics.Color
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat

object EventUtils {

  /** Temporary method to add a background color to the event type */
  fun addAlphaToColor(color: Color, alpha: Int): Color {
    val red = (color.red * 255).toInt()
    val green = (color.green * 255).toInt()
    val blue = (color.blue * 255).toInt()

    return Color(red, green, blue, alpha)
  }

  /**
   * Formats the timestamp to a string using the provided date format
   *
   * @param timestamp The timestamp to format
   * @param dateFormat The date format to use
   * @return The formatted timestamp
   */
  fun formatTimestamp(timestamp: Timestamp?, dateFormat: SimpleDateFormat): String {
    if (timestamp == null) {
      return "Invalid Timestamp"
    }

    val date = timestamp.toDate()
    return dateFormat.format(date)
  }
}
