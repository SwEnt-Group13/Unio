package com.android.unio.ui.authentication.overlay

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.unio.R
import com.android.unio.model.strings.test_tags.authentication.InterestsOverlayTestTags
import com.android.unio.model.user.Interest
import com.android.unio.ui.theme.AppTypography

/**
 * A dialog that allows users to select their interests.
 *
 * @param onDismiss Callback when the dialog is dismissed.
 * @param onSave Callback when the changes are saved.
 * @param interests The list of pairs of [Interest] and their selected state.
 */
@Composable
fun InterestOverlay(
    onDismiss: () -> Unit,
    onSave: (List<Pair<Interest, MutableState<Boolean>>>) -> Unit,
    interests: List<Pair<Interest, MutableState<Boolean>>>
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  val copiedInterests = interests.toList().map { it.first to mutableStateOf(it.second.value) }

  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp),
            modifier =
                Modifier.fillMaxWidth().padding(20.dp).testTag(InterestsOverlayTestTags.CARD)) {
              Column(
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(15.dp)
                          .sizeIn(maxHeight = 400.dp)
                          .testTag(InterestsOverlayTestTags.COLUMN),
                  verticalArrangement = Arrangement.SpaceBetween) {

                    // Text fields for the title and description of the Interest Overlay
                    Text(
                        text = context.getString(R.string.interest_overlay_title),
                        style = AppTypography.headlineSmall,
                        modifier = Modifier.testTag(InterestsOverlayTestTags.TITLE_TEXT))
                    Text(
                        text = context.getString(R.string.interest_overlay_description),
                        style = AppTypography.bodyMedium,
                        modifier =
                            Modifier.padding(bottom = 5.dp)
                                .testTag(InterestsOverlayTestTags.SUBTITLE_TEXT))
                    Surface(
                        modifier = Modifier.sizeIn(maxHeight = 250.dp), color = Color.Transparent) {
                          Column(modifier = Modifier.verticalScroll(scrollState)) {
                            copiedInterests.forEachIndexed { index, pair ->

                              // The row for each interest
                              InterestsOverlayInterestRow(pair)

                              if (index != copiedInterests.size - 1) {
                                HorizontalDivider(
                                    modifier =
                                        Modifier.testTag(
                                            InterestsOverlayTestTags.DIVIDER + "$index"))
                              }
                            }
                          }
                        }

                    // Buttons for saving and cancelling the changes
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Absolute.Right) {
                          OutlinedButton(
                              shape = RoundedCornerShape(16.dp),
                              onClick = onDismiss,
                              modifier =
                                  Modifier.padding(5.dp)
                                      .testTag(InterestsOverlayTestTags.CANCEL_BUTTON)) {
                                Text(context.getString(R.string.overlay_cancel))
                              }

                          Button(
                              onClick = { onSave(copiedInterests) },
                              modifier =
                                  Modifier.padding(5.dp)
                                      .testTag(InterestsOverlayTestTags.SAVE_BUTTON)) {
                                Text(context.getString(R.string.overlay_save))
                              }
                        }
                  }
            }
      }
}

/**
 * A row that displays an interest and a checkbox to select it.
 *
 * @param pair The pair of [Interest] and its selected state.
 */
@Composable
private fun InterestsOverlayInterestRow(
    pair: Pair<Interest, MutableState<Boolean>>,
) {
  Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
      modifier =
          Modifier.padding(5.dp)
              .fillMaxWidth()
              .testTag(InterestsOverlayTestTags.CLICKABLE_ROW + pair.first.name)
              .clickable { pair.second.value = !pair.second.value }) {
        Text(
            text = pair.first.name,
            style = AppTypography.bodyMedium,
            modifier =
                Modifier.padding(start = 5.dp)
                    .testTag(InterestsOverlayTestTags.TEXT + pair.first.name))
        Checkbox(
            checked = pair.second.value,
            onCheckedChange = { pair.second.value = it },
            modifier = Modifier.testTag(InterestsOverlayTestTags.CHECKBOX + pair.first.name))
      }
}
