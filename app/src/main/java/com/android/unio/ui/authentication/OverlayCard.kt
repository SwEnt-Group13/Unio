package com.android.unio.ui.authentication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.unio.model.user.Interest
import com.android.unio.ui.theme.AppTypography
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun InterestOverlay(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    interests: MutableStateFlow<MutableList<Pair<Interest, MutableState<Boolean>>>>
) {
  val scrollState = rememberScrollState()
  val interestsState by interests.collectAsState()

  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth().padding(20.dp)) {
              Column(
                  modifier = Modifier.fillMaxWidth().padding(15.dp).sizeIn(maxHeight = 400.dp).testTag("InterestOverlayColumn"),
                  verticalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Your interests", style = AppTypography.headlineSmall, modifier = Modifier.testTag("InterestOverlayTitle"))
                    Text(
                        text = "Choose as many interests as you feel apply to you",
                        style = AppTypography.bodyMedium,
                        modifier = Modifier.padding(bottom = 5.dp).testTag("InterestOverlaySubtitle"))
                    Surface(
                        modifier = Modifier.sizeIn(maxHeight = 250.dp), color = Color.Transparent) {
                          Column(modifier = Modifier.verticalScroll(scrollState)) {
                            interestsState.forEachIndexed { index, pair ->
                              Row(
                                  horizontalArrangement = Arrangement.SpaceBetween,
                                  verticalAlignment = Alignment.CenterVertically,
                                  modifier =
                                      Modifier.padding(5.dp).fillMaxWidth().testTag("InterestOverlayClickableRow : $index").clickable {
                                        pair.second.value = !pair.second.value
                                      }) {
                                    Text(
                                        text = pair.first.name,
                                        style = AppTypography.bodyMedium,
                                        modifier = Modifier.padding(start = 5.dp).testTag("InterestOverlayText: ${pair.first.name}"))
                                    Checkbox(
                                        checked = pair.second.value,
                                        onCheckedChange = { pair.second.value = it },
                                        modifier = Modifier.testTag("InterestOverlayCheckbox: ${pair.first.name}"))
                                  }
                              if (index != interestsState.size - 1) {
                                Divider(modifier = Modifier.testTag("InterestOverlayDivider: $index"))
                              }
                            }
                          }
                        }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Absolute.Right) {
                          Button(onClick = onSave, modifier = Modifier.padding(5.dp).testTag("InterestOverlaySaveButton")) {
                            Text(text = "Save")
                          }
                        }
                  }
            }
      }
}
