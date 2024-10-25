package com.android.unio.ui.authentication.overlay

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.unio.model.user.Social
import com.android.unio.model.user.UserSocial
import com.android.unio.model.user.UserSocialError
import com.android.unio.model.user.checkSocialContent
import com.android.unio.model.user.getPlaceHolderText
import com.android.unio.ui.theme.AppTypography
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SocialOverlay(
    onDismiss: () -> Unit,
    onSave: (MutableList<UserSocial>) -> Unit,
    userSocials: MutableList<UserSocial>
) {
  val scrollState = rememberScrollState()

  val copiedUserSocialsFlow = remember { MutableStateFlow(userSocials.map { it }.toMutableList()) }
  val copiedUserSocials by copiedUserSocialsFlow.collectAsState()

  var showAddSocialPrompt by remember { mutableStateOf(false) }

  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(20.dp).testTag("SocialsOverlayCard")) {
              Column(
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(15.dp)
                          .sizeIn(maxHeight = 400.dp)
                          .testTag("SocialsOverlayColumn"),
                  verticalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Your social media",
                        style = AppTypography.headlineSmall,
                        modifier = Modifier.testTag("SocialOverlayTitle"))
                    Text(
                        text =
                            "These will be displayed on your profile to allow other students to contact you",
                        style = AppTypography.bodyMedium,
                        modifier = Modifier.padding(bottom = 5.dp).testTag("SocialOverlaySubtitle"))
                    Surface(
                        modifier = Modifier.sizeIn(maxHeight = 250.dp), color = Color.Transparent) {
                          Column(
                              modifier = Modifier.verticalScroll(scrollState).fillMaxWidth(),
                              verticalArrangement = Arrangement.Center,
                              horizontalAlignment = Alignment.CenterHorizontally) {
                                copiedUserSocials.forEachIndexed { index, userSocial ->
                                  Row(
                                      horizontalArrangement = Arrangement.SpaceBetween,
                                      verticalAlignment = Alignment.CenterVertically,
                                      modifier =
                                          Modifier.padding(5.dp)
                                              .fillMaxWidth()
                                              .testTag(
                                                  "SocialOverlayClickableRow: ${userSocial.social.title}")) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                          Image(
                                              modifier = Modifier.size(16.dp).wrapContentSize(),
                                              painter = painterResource(userSocial.social.icon),
                                              contentDescription = userSocial.social.title,
                                              contentScale = ContentScale.Fit)
                                          Text(userSocial.social.title)
                                        }
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Close",
                                            modifier =
                                                Modifier.clickable {
                                                      copiedUserSocialsFlow.value =
                                                          copiedUserSocialsFlow.value
                                                              .toMutableList()
                                                              .apply { removeAt(index) }
                                                    }
                                                    .testTag(
                                                        "SocialOverlayCloseIcon: ${userSocial.social.title}"))
                                      }
                                  if (index != copiedUserSocials.size - 1) {
                                    Divider(
                                        modifier = Modifier.testTag("SocialOverlayDivider: $index"))
                                  }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End) {
                                      OutlinedButton(
                                          onClick = { showAddSocialPrompt = true },
                                          modifier =
                                              Modifier.padding(8.dp)
                                                  .testTag("SocialOverlayAddButton"),
                                          shape = RoundedCornerShape(16.dp)) {
                                            Icon(Icons.Default.Add, contentDescription = "Add")
                                            Text("Add Social")
                                          }
                                      Button(
                                          onClick = { onSave(copiedUserSocials) },
                                          modifier =
                                              Modifier.padding(8.dp)
                                                  .testTag("SocialOverlaySaveButton"),
                                          shape = RoundedCornerShape(16.dp)) {
                                            Text("Save")
                                          }
                                    }
                              }
                        }
                  }
            }
      }

  if (showAddSocialPrompt) {
    SocialPrompt(
        { showAddSocialPrompt = false },
        {
          copiedUserSocials.add(it)
          showAddSocialPrompt = false
        })
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SocialPrompt(
    onDismiss: () -> Unit,
    onSave: (UserSocial) -> Unit,
) {
  val socialsList = Social.entries.toList()

  var isExpanded by remember { mutableStateOf(false) }

  var selectedSocial by remember { mutableStateOf(socialsList[0]) }

  var socialURL by remember { mutableStateOf("") }

  var isError by remember { mutableStateOf(UserSocialError.NONE) }

  Dialog(onDismissRequest = {}, properties = DialogProperties(usePlatformDefaultWidth = false)) {
    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(20.dp).testTag("SocialPromptCard")) {
          Column(
              modifier = Modifier.fillMaxWidth().padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.SpaceBetween) {
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded },
                    modifier = Modifier.padding(10.dp).testTag("SocialPromptDropdownMenuBox")) {
                      TextField(
                          value = selectedSocial.title,
                          onValueChange = {},
                          readOnly = true,
                          trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                          })
                      ExposedDropdownMenu(
                          expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                            socialsList.forEachIndexed { index, social ->
                              DropdownMenuItem(
                                  modifier =
                                      Modifier.testTag(
                                          "SocialPromptDropdownMenuItem: ${social.title}"),
                                  text = { Text(social.title) },
                                  onClick = {
                                    selectedSocial = socialsList[index]
                                    isExpanded = false
                                  })
                            }
                          }
                    }
                OutlinedTextField(
                    value = socialURL,
                    onValueChange = { socialURL = it },
                    placeholder = {
                      if (selectedSocial != Social.WEBSITE) {
                        Text(getPlaceHolderText(selectedSocial), fontWeight = FontWeight.Bold)
                      } else {
                        Text(getPlaceHolderText(selectedSocial))
                      }
                    },
                    prefix = { Text(selectedSocial.URLshort) },
                    isError = (isError != UserSocialError.NONE),
                    keyboardOptions =
                        KeyboardOptions.Default.copy(
                            capitalization =
                                KeyboardCapitalization.None // Disable automatic capitalization
                            ),
                    supportingText = {
                      if (isError != UserSocialError.NONE) {
                        Text(
                            isError.errorMessage,
                            modifier = Modifier.testTag("SocialPromptErrorText"))
                      }
                    },
                    singleLine = true,
                    modifier = Modifier.padding(10.dp).testTag("SocialPromptTextField"))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                      OutlinedButton(
                          modifier = Modifier.testTag("SocialPromptCancelButton"),
                          onClick = onDismiss,
                          shape = RoundedCornerShape(16.dp)) {
                            Text("Cancel")
                          }
                      Button(
                          modifier = Modifier.testTag("SocialPromptSaveButton"),
                          onClick = {
                            val newUserSocial = UserSocial(selectedSocial, socialURL)
                            isError = checkSocialContent(newUserSocial)
                            if (isError == UserSocialError.NONE) {
                              onSave(newUserSocial)
                            }
                          },
                      ) {
                        Text("Save changes")
                      }
                    }
              }
        }
  }
}
