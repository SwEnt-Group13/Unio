package com.android.unio.ui.association

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.unio.model.association.Association
import com.android.unio.ui.theme.AppTypography

@Composable
fun AssociationSmall(association: Association, onClick: () -> Unit) {
  TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()) {
          Text(text = association.name, style = AppTypography.bodyMedium)
          Icon(
              modifier = Modifier.size(18.dp),
              imageVector = Icons.Default.ArrowForwardIos,
              contentDescription = null,
          )
        }
  }
}
