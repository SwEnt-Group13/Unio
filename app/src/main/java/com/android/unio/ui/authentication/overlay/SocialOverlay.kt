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
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.unio.R
import com.android.unio.model.strings.test_tags.SocialsOverlayTestTags
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

  val context = LocalContext.current

  val copiedUserSocialsFlow = remember { MutableStateFlow(userSocials.map { it }.toMutableList()) }
  val copiedUserSocials by copiedUserSocialsFlow.collectAsState()

  var showAddSocialPrompt by remember { mutableStateOf(false) }

  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp),
            modifier =
                Modifier.fillMaxWidth().padding(20.dp).testTag(SocialsOverlayTestTags.CARD)) {
              Column(
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(15.dp)
                          .sizeIn(maxHeight = 400.dp)
                          .testTag(SocialsOverlayTestTags.COLUMN),
                  verticalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = context.getString(R.string.social_overlay_title),
                        style = AppTypography.headlineSmall,
                        modifier = Modifier.testTag(SocialsOverlayTestTags.TITLE_TEXT))
                    Text(
                        text = context.getString(R.string.social_overlay_description),
                        style = AppTypography.bodyMedium,
                        modifier =
                            Modifier.padding(bottom = 5.dp)
                                .testTag(SocialsOverlayTestTags.DESCRIPTION_TEXT))
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
                                                  SocialsOverlayTestTags.CLICKABLE_ROW +
                                                      userSocial.social.title)) {
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
                                            contentDescription =
                                                context.getString(
                                                    R.string
                                                        .social_overlay_content_description_close),
                                            modifier =
                                                Modifier.clickable {
                                                      copiedUserSocialsFlow.value =
                                                          copiedUserSocialsFlow.value
                                                              .toMutableList()
                                                              .apply { removeAt(index) }
                                                    }
                                                    .testTag(
                                                        SocialsOverlayTestTags.ICON +
                                                            userSocial.social.title))
                                      }
                                  if (index != copiedUserSocials.size - 1) {
                                    HorizontalDivider(
                                        modifier =
                                            Modifier.testTag(
                                                SocialsOverlayTestTags.DIVIDER + "$index"))
                                  }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End) {
                                      OutlinedButton(
                                          onClick = { showAddSocialPrompt = true },
                                          modifier =
                                              Modifier.padding(8.dp)
                                                  .testTag(SocialsOverlayTestTags.ADD_BUTTON),
                                          shape = RoundedCornerShape(16.dp)) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription =
                                                    context.getString(
                                                        R.string
                                                            .social_overlay_content_description_add))
                                            Text(
                                                context.getString(
                                                    R.string.social_overlay_add_social))
                                          }
                                      Button(
                                          onClick = { onSave(copiedUserSocials) },
                                          modifier =
                                              Modifier.padding(8.dp)
                                                  .testTag(SocialsOverlayTestTags.SAVE_BUTTON),
                                          shape = RoundedCornerShape(16.dp)) {
                                            Text(context.getString(R.string.overlay_save))
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SocialPrompt(
    onDismiss: () -> Unit,
    onSave: (UserSocial) -> Unit,
) {
  val context = LocalContext.current

  val socialsList = Social.entries.toList()

  var isExpanded by remember { mutableStateOf(false) }

  var selectedSocial by remember { mutableStateOf(socialsList[0]) }

  var socialURL by remember { mutableStateOf("") }

  var isError by remember { mutableStateOf(UserSocialError.NONE) }

  Dialog(onDismissRequest = {}, properties = DialogProperties(usePlatformDefaultWidth = false)) {
    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp),
        modifier =
            Modifier.fillMaxWidth().padding(20.dp).testTag(SocialsOverlayTestTags.PROMPT_CARD)) {
          Column(
              modifier = Modifier.fillMaxWidth().padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.SpaceBetween) {
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded },
                    modifier =
                        Modifier.padding(10.dp).testTag(SocialsOverlayTestTags.PROMPT_DROP_BOX)) {
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
                                          SocialsOverlayTestTags.PROMPT_DROP_BOX_ITEM +
                                              social.title),
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
                    prefix = { Text(selectedSocial.url) },
                    isError = (isError != UserSocialError.NONE),
                    keyboardOptions =
                        KeyboardOptions.Default.copy(
                            capitalization =
                                KeyboardCapitalization.None // Disable automatic capitalization
                            ),
                    supportingText = {
                      if (isError != UserSocialError.NONE) {
                        Text(
                            context.getString(isError.errorMessage),
                            modifier = Modifier.testTag(SocialsOverlayTestTags.PROMPT_ERROR))
                      }
                    },
                    singleLine = true,
                    modifier =
                        Modifier.padding(10.dp).testTag(SocialsOverlayTestTags.PROMPT_TEXT_FIELD))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                      OutlinedButton(
                          modifier = Modifier.testTag(SocialsOverlayTestTags.PROMPT_CANCEL_BUTTON),
                          onClick = onDismiss,
                          shape = RoundedCornerShape(16.dp)) {
                            Text(context.getString(R.string.overlay_cancel))
                          }
                      Button(
                          modifier = Modifier.testTag(SocialsOverlayTestTags.PROMPT_SAVE_BUTTON),
                          onClick = {
                            val newUserSocial = UserSocial(selectedSocial, socialURL)
                            isError = checkSocialContent(newUserSocial)
                            if (isError == UserSocialError.NONE) {
                              onSave(newUserSocial)
                            }
                          },
                      ) {
                        Text(context.getString(R.string.social_overlay_save_changes))
                      }
                    }
              }
        }
  }
}
