package com.android.unio.model.event

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.android.unio.R
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

fun addAlphaToColor(color: Color, alpha: Int): Color {
  val red = (color.red * 255).toInt()
  val green = (color.green * 255).toInt()
  val blue = (color.blue * 255).toInt()

  return Color(red, green, blue, alpha)
}

@Composable
fun DynamicImage(eventImageName: String): Painter {
  val context = LocalContext.current

  val resourceId = context.resources.getIdentifier(eventImageName, "drawable", context.packageName)

  val painter =
      if (resourceId != 0) {
        return painterResource(id = resourceId)
      } else {
        return painterResource(id = R.drawable.weskic)
      }
}

fun getContrastingColorBlackAndWhite(backgroundColor: Color): Color {
  // Extract RGB components from the Color
  val r = backgroundColor.red
  val g = backgroundColor.green
  val b = backgroundColor.blue

  // Calculate luminance using the formula
  val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b

  // Return black if the luminance is high, otherwise return white
  return if (luminance > 0.5) Color.Black else Color.White
}

fun darkenColor(color: Color, factor: Float): Color {
  val r = (color.red * 255).toInt()
  val g = (color.green * 255).toInt()
  val b = (color.blue * 255).toInt()

  // Darken the color by a factor (0.0 to 1.0)
  val darkenedR = (r * (1 - factor)).coerceIn(0f, 255f).toInt()
  val darkenedG = (g * (1 - factor)).coerceIn(0f, 255f).toInt()
  val darkenedB = (b * (1 - factor)).coerceIn(0f, 255f).toInt()

  return Color(darkenedR, darkenedG, darkenedB)
}

fun getContrastingColor(backgroundColor: Color): Color {

  val candidateColors =
      listOf(
          Color.Black,
          Color.White,
          Color.Red,
          Color.Green,
          Color.Blue,
          Color.Yellow,
          Color.Cyan,
          Color.Magenta,
          Color.Gray)

  val rBg = backgroundColor.red
  val gBg = backgroundColor.green
  val bBg = backgroundColor.blue

  val luminanceBg = 0.2126 * rBg + 0.7152 * gBg + 0.0722 * bBg

  fun contrastRatio(color: Color): Double {
    val r = color.red
    val g = color.green
    val b = color.blue

    val luminanceColor = 0.2126 * r + 0.7152 * g + 0.0722 * b

    return if (luminanceBg < luminanceColor) {
      (luminanceColor + 0.05) / (luminanceBg + 0.05)
    } else {
      (luminanceBg + 0.05) / (luminanceColor + 0.05)
    }
  }

  return candidateColors.maxByOrNull { contrastRatio(it) } ?: Color.White
}

fun formatTimestampToMMDD(timestamp: Timestamp): String {

  if (timestamp == null) {
    return "Invalid Timestamp"
  }

  val date = timestamp.toDate()
  val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
  return dateFormat.format(date)
}

fun formatTimestampToHHMM(timestamp: Timestamp?): String {
  if (timestamp == null) {
    return "Invalid Timestamp"
  }

  val date = timestamp.toDate()

  val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
  return timeFormat.format(date)
}
