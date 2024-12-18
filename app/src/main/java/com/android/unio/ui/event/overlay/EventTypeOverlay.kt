package com.android.unio.ui.event.overlay

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
import com.android.unio.model.event.EventType
import com.android.unio.model.strings.test_tags.event.EventTypeOverlayTestTags
import com.android.unio.ui.theme.AppTypography


@Composable
fun EventTypeOverlay(
    onDismiss: () -> Unit,
    onSave: (List<Pair<EventType, MutableState<Boolean>>>) -> Unit,
    types: List<Pair<EventType, MutableState<Boolean>>>
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val copiedTypes = types.toList().map { it.first to mutableStateOf(it.second.value) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp),
            modifier =
            Modifier.fillMaxWidth().padding(20.dp).testTag(EventTypeOverlayTestTags.CARD)) {
            Column(
                modifier =
                Modifier.fillMaxWidth()
                    .padding(15.dp)
                    .sizeIn(maxHeight = 400.dp)
                    .testTag(EventTypeOverlayTestTags.COLUMN),
                verticalArrangement = Arrangement.SpaceBetween) {

                // Text fields for the title and description of the Interest Overlay
                Text(
                    text = context.getString(), //R.string.interest_overlay_title
                    style = AppTypography.headlineSmall,
                    modifier = Modifier.testTag(EventTypeOverlayTestTags.TITLE_TEXT))
                Text(
                    text = context.getString(), //R.string.interest_overlay_description
                    style = AppTypography.bodyMedium,
                    modifier =
                    Modifier.padding(bottom = 5.dp)
                        .testTag(EventTypeOverlayTestTags.SUBTITLE_TEXT))
                Surface(
                    modifier = Modifier.sizeIn(maxHeight = 250.dp), color = Color.Transparent) {
                    Column(modifier = Modifier.verticalScroll(scrollState)) {
                        copiedTypes.forEachIndexed { index, pair ->

                            // The row for each interest
                            EventTypeOverlayRow(pair)

                            if (index != copiedTypes.size - 1) {
                                HorizontalDivider(
                                    modifier =
                                    Modifier.testTag(EventTypeOverlayTestTags.DIVIDER + "$index"))
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
                            .testTag(EventTypeOverlayTestTags.CANCEL_BUTTON)) {
                        Text(context.getString()) //R.string.overlay_cancel
                    }

                    Button(
                        onClick = { onSave(copiedTypes) },
                        modifier =
                        Modifier.padding(5.dp)
                            .testTag(EventTypeOverlayTestTags.SAVE_BUTTON)) {
                        Text(context.getString()) //R.string.overlay_save
                    }
                }
            }
        }
    }
}

@Composable
private fun EventTypeOverlayRow(
    pair: Pair<EventType, MutableState<Boolean>>,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
        Modifier.padding(5.dp)
            .fillMaxWidth()
            .testTag(EventTypeOverlayTestTags.CLICKABLE_ROW + pair.first.name)
            .clickable { pair.second.value = !pair.second.value }) {
        Text(
            text = pair.first.name,
            style = AppTypography.bodyMedium,
            modifier =
            Modifier.padding(start = 5.dp)
                .testTag(EventTypeOverlayTestTags.TEXT + pair.first.name))
        Checkbox(
            checked = pair.second.value,
            onCheckedChange = { pair.second.value = it },
            modifier = Modifier.testTag(EventTypeOverlayTestTags.CHECK_BOX + pair.first.name))
    }
}
