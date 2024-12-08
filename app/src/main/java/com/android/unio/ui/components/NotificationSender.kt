package com.android.unio.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.android.unio.R
import com.android.unio.model.notification.NotificationType
import com.android.unio.model.notification.broadcastMessage
import com.android.unio.model.strings.test_tags.NotificationSenderTestTags
import com.android.unio.ui.theme.AppTypography

/**
 * A composable that shows a dialog for sending a notification to a topic.
 *
 * @param dialogTitle The title of the dialog.
 * @param notificationType The target of the notification.
 * @param topic The topic to send the notification to.
 * @param notificationContent A function that returns the notification content given the written
 *   message.
 * @param showNotificationDialog Whether to show the dialog.
 * @param onClose The function to call when the dialog is closed.
 */
@Composable
fun NotificationSender(
    dialogTitle: String,
    notificationType: NotificationType,
    topic: String,
    notificationContent: (String) -> Map<String, String>,
    showNotificationDialog: Boolean,
    onClose: () -> Unit
) {

  var message by remember { mutableStateOf("") }
  val maxNotificationLength = 100
  val context = LocalContext.current

  if (showNotificationDialog) {
    Dialog(onDismissRequest = onClose) {
      Card(
          elevation = CardDefaults.cardElevation(8.dp),
          shape = RoundedCornerShape(16.dp),
          modifier =
              Modifier.fillMaxWidth().padding(20.dp).testTag(NotificationSenderTestTags.CARD)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)) {
                  Text(
                      dialogTitle,
                      style = AppTypography.bodyLarge,
                      modifier = Modifier.testTag(NotificationSenderTestTags.TITLE))

                  OutlinedTextField(
                      modifier = Modifier.testTag(NotificationSenderTestTags.MESSAGE_FIELD),
                      value = message,
                      onValueChange = {
                        if (it.length <= maxNotificationLength) {
                          message = it
                        }
                      },
                      label = {
                        if (message.isEmpty()) {
                          Text(context.getString(R.string.notification_broadcast_message))
                        } else {
                          Text(
                              "${context.getString(R.string.notification_broadcast_message)} (${message.length}/${maxNotificationLength})")
                        }
                      })

                  Row(
                      horizontalArrangement = Arrangement.End,
                      modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                        OutlinedButton(
                            onClick = {
                              message = ""
                              onClose()
                            },
                        ) {
                          Text(context.getString(R.string.cancel_button_text))
                        }
                        Button(
                            modifier = Modifier.testTag(NotificationSenderTestTags.SEND_BUTTON),
                            onClick = {
                              if (message.isEmpty()) {
                                Toast.makeText(
                                        context,
                                        context.getString(R.string.notification_empty_message),
                                        Toast.LENGTH_SHORT)
                                    .show()
                                return@Button
                              }

                              broadcastMessage(
                                  type = notificationType,
                                  topic = topic,
                                  payload = notificationContent(message),
                                  onSuccess = {
                                    Toast.makeText(
                                            context,
                                            context.getString(
                                                R.string.notification_broadcast_success),
                                            Toast.LENGTH_SHORT)
                                        .show()
                                  },
                                  {
                                    Toast.makeText(
                                            context,
                                            context.getString(
                                                R.string.notification_broadcast_failure),
                                            Toast.LENGTH_SHORT)
                                        .show()
                                  })
                              message = ""
                              onClose()
                            },
                        ) {
                          Text("Send")
                        }
                      }
                }
          }
    }
  }
}
