package com.android.unio.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.android.unio.R
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat

object EventUtils {

  fun addAlphaToColor(color: Color, alpha: Int): Color {
    val red = (color.red * 255).toInt()
    val green = (color.green * 255).toInt()
    val blue = (color.blue * 255).toInt()

    return Color(red, green, blue, alpha)
  }

  @Composable
  fun DynamicImage(eventImageName: String): Painter {
    val context = LocalContext.current
    val resourceId =
        context.resources.getIdentifier(eventImageName, "drawable", context.packageName)

    return if (resourceId != 0) {
      painterResource(id = resourceId)
    } else {
      painterResource(id = R.drawable.weskic)
    }
  }

  fun getContrastingColorBlackAndWhite(backgroundColor: Color): Color {
    val r = backgroundColor.red
    val g = backgroundColor.green
    val b = backgroundColor.blue
    val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b

    return if (luminance > 0.5) Color.Black else Color.White
  }

  fun darkenColor(color: Color, factor: Float): Color {
    val r = (color.red * 255).toInt()
    val g = (color.green * 255).toInt()
    val b = (color.blue * 255).toInt()

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

  fun formatTimestamp(timestamp: Timestamp?, dateFormat: SimpleDateFormat): String {
    if (timestamp == null) {
      return "Invalid Timestamp"
    }

    val date = timestamp.toDate()
    return dateFormat.format(date)
  }
}
