package com.android.unio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.unio.model.association.Role
import com.android.unio.ui.theme.AppTypography
import com.android.unio.ui.theme.getReadableTextColor

@Composable
fun RoleBadge(userRole: Role) {
  Box(
      modifier =
          Modifier.padding(start = 8.dp)
              .background(color = Color(userRole.color), shape = RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(
            text = userRole.displayName,
            style = AppTypography.bodySmall,
            color = Color(getReadableTextColor(userRole.color)),
            textAlign = TextAlign.Center)
      }
}
