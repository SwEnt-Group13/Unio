package com.android.unio.ui.association

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.unio.model.association.Association
import com.android.unio.model.user.getUserRoleInAssociation
import com.android.unio.ui.theme.AppTypography
import kotlin.random.Random

/**
 * A small association item that can be used in a list of associations.
 *
 * @param association The association to display.
 * @param onClick Callback when the association is clicked.
 */
@Composable
fun AssociationSmall(association: Association, userUid: String, onClick: () -> Unit) {
  val userRoleName = getUserRoleInAssociation(association, userUid)
  TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()) {
          Text(text = association.name, style = AppTypography.bodyMedium)
          // Role Badge
          Box(
              modifier =
                  Modifier.padding(start = 8.dp)
                      .background(color = getRandomBadgeColor(), shape = RoundedCornerShape(8.dp))
                      .padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    text = userRoleName,
                    style = AppTypography.bodySmall,
                    color = Color.White,
                    textAlign = TextAlign.Center)
              }
          Icon(
              modifier = Modifier.size(18.dp),
              imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
              contentDescription = null,
          )
        }
  }
}

/** Generates a random color for the badge. */
fun getRandomBadgeColor(): Color {
  val colors =
      listOf(
          Color(0xFF1E88E5), // Blue
          Color(0xFFD32F2F), // Red
          Color(0xFF388E3C), // Green
          Color(0xFFFBC02D), // Yellow
          Color(0xFF8E24AA) // Purple
          )
  return colors[Random.nextInt(colors.size)]
}
