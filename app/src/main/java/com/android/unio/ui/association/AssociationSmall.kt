package com.android.unio.ui.association

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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

/**
 * A small association item that can be used in a list of associations.
 *
 * @param association The association to display.
 * @param onClick Callback when the association is clicked.
 */
@Composable
fun AssociationSmall(association: Association, userUid: String, onClick: () -> Unit) {
  val userRole = getUserRoleInAssociation(association, userUid)
  TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()) {
          Text(text = association.name, style = AppTypography.bodyMedium)
          // Role Badge
          if (userRole != null) {
            Box(
                modifier =
                    Modifier.padding(start = 8.dp)
                        .background(color = Color(userRole.color), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)) {
                  Text(
                      text = userRole.displayName,
                      style = AppTypography.bodySmall,
                      color = Color.White,
                      textAlign = TextAlign.Center)
                }
          }
          Icon(
              modifier = Modifier.size(18.dp),
              imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
              contentDescription = null,
          )
        }
  }
}
