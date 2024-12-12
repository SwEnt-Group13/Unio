package com.android.unio.ui.association

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.Event
import com.android.unio.model.firestore.FirestorePaths.ASSOCIATION_PATH
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.strings.test_tags.association.SaveAssociationTestTags
import com.android.unio.model.strings.test_tags.association.SaveAssociationTestTags.PICTURE_BUTTON
import com.android.unio.model.strings.test_tags.association.SaveAssociationTestTags.PRINCIPAL_EMAIL_ADDRESS_EXPLANATION_TEXT
import com.android.unio.model.strings.test_tags.association.SaveAssociationTestTags.PRINCIPAL_EMAIL_ADDRESS_TEXT
import com.android.unio.model.strings.test_tags.event.EventDetailsTestTags
import com.android.unio.ui.components.PictureSelectionTool
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.utils.ToastUtils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.InputStream
import kotlinx.coroutines.launch

/**
 * Screen for editing an association.
 *
 * @param associationViewModel [AssociationViewModel] that provides the association to edit.
 * @param navigationAction [NavigationAction] that handles navigation.
 */
@Composable
fun SaveAssociationScreen(
    associationViewModel: AssociationViewModel,
    navigationAction: NavigationAction,
    isNewAssociation: Boolean
) {
  val context = LocalContext.current

  val association by associationViewModel.selectedAssociation.collectAsState()

  if (association == null && !isNewAssociation) {
    Log.e("SaveAssociationScreen", "Association not found.")
    Toast.makeText(context, "An error occurred.", Toast.LENGTH_SHORT).show()
    return
  }

  (if (association == null) {
        Association(
            uid =
                Firebase.firestore
                    .collection(ASSOCIATION_PATH)
                    .document()
                    .id, // creates a new document reference and retrieve the randomly generated
            // Firestore UID
            url = "",
            name = "",
            fullName = "",
            category = AssociationCategory.UNKNOWN,
            description = "",
            followersCount = 0,
            members = emptyList(),
            roles = emptyList(),
            image = "",
            events = Event.emptyFirestoreReferenceList(),
            principalEmailAddress = "")
      } else {
        association
      })
      ?.let {
        SaveAssociationScaffold(
            association = it,
            onCancel = {
              if (isNewAssociation) {
                navigationAction.navigateTo(Screen.MY_PROFILE, Screen.SAVE_ASSOCIATION)
              } else {
                associationViewModel.selectAssociation(it.uid)
                navigationAction.navigateTo(Screen.ASSOCIATION_PROFILE, Screen.SAVE_ASSOCIATION)
              }
            },
            onSave = { newAssociation, imageStream ->
              associationViewModel.saveAssociation(
                  isNewAssociation = true,
                  newAssociation,
                  imageStream,
                  onSuccess = {
                    if (isNewAssociation) {
                      ToastUtils.showToast(
                          context, context.getString(R.string.save_association_success))
                      navigationAction.navigateTo(Screen.MY_PROFILE, Screen.SAVE_ASSOCIATION)
                    } else {
                      associationViewModel.selectAssociation(newAssociation.uid)
                      navigationAction.navigateTo(
                          Screen.ASSOCIATION_PROFILE, Screen.SAVE_ASSOCIATION)
                    }
                  },
                  onFailure = { error ->
                    Log.e("SaveAssociationScreen", context.getString(R.string.save_failed_message))
                    Log.e("SaveAssociationScreen", "Save failed: ${error.message}", error)
                    ToastUtils.showToast(context, context.getString(R.string.save_failed_message))
                  })
            },
            isNewAssociation = isNewAssociation)
      }
}

/**
 * Scaffold for editing an association.
 *
 * @param association The [Association] to edit.
 * @param onCancel Callback when the user cancels the edit.
 * @param onSave Callback when the user saves the edit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveAssociationScaffold(
    association: Association,
    onCancel: () -> Unit,
    onSave: (Association, InputStream?) -> Unit,
    isNewAssociation: Boolean
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var url by remember { mutableStateOf(association.url) }
  var name by remember { mutableStateOf(association.name) }
  var fullName by remember { mutableStateOf(association.fullName) }
  var description by remember { mutableStateOf(association.description) }
  var principalEmailAddress by remember { mutableStateOf(association.principalEmailAddress) }

  var selectedImageUri by remember {
    mutableStateOf<Uri?>(
        if (isNewAssociation) {
          null
        } else {
          association.image.toUri()
        })
  }

  var expanded by remember { mutableStateOf(false) }
  var category by remember { mutableStateOf(association.category) }

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var showSheet by remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.testTag(SaveAssociationTestTags.SCREEN),
      topBar = {
        TopAppBar(
            navigationIcon = {
              IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = context.getString(R.string.association_go_back))
              }
            },
            title = {
              Text(
                  text =
                      if (isNewAssociation) {
                        context.getString(R.string.create_association_title)
                      } else {
                        context.getString(R.string.edit_association_title)
                      },
                  style = MaterialTheme.typography.headlineMedium,
                  modifier = Modifier.testTag(SaveAssociationTestTags.TITLE))
            })
      }) { padding ->
        Column(
            modifier =
                Modifier.padding(padding)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())) {
              Text(
                  text = context.getString(R.string.name_explanation),
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.testTag(SaveAssociationTestTags.NAME_EXPLANATION_TEXT))

              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                  value = name,
                  onValueChange = { name = it },
                  label = { Text(context.getString(R.string.field_name)) },
                  modifier =
                      Modifier.fillMaxWidth().testTag(SaveAssociationTestTags.NAME_TEXT_FIELD))

              Spacer(modifier = Modifier.height(16.dp))

              // Explanation for "Full Name"
              Text(
                  text = context.getString(R.string.full_name_explanation),
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.testTag(SaveAssociationTestTags.FULL_NAME_EXPLANATION_TEXT))

              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                  value = fullName,
                  onValueChange = { fullName = it },
                  label = { Text(context.getString(R.string.field_full_name)) },
                  modifier =
                      Modifier.fillMaxWidth().testTag(SaveAssociationTestTags.FULL_NAME_TEXT_FIELD))

              Spacer(modifier = Modifier.height(16.dp))

              // Explanation for "Category"
              Text(
                  text = context.getString(R.string.category_explanation),
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.testTag(SaveAssociationTestTags.CATEGORY_EXPLANATION_TEXT))

              Spacer(modifier = Modifier.height(8.dp))

              // Category Button
              Button(
                  onClick = { expanded = true },
                  modifier =
                      Modifier.fillMaxWidth().testTag(SaveAssociationTestTags.CATEGORY_BUTTON)) {
                    Text(text = context.getString(category.displayNameId))
                  }

              DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                AssociationCategory.entries.forEach { categoryOption ->
                  DropdownMenuItem(
                      text = { Text(text = context.getString(categoryOption.displayNameId)) },
                      onClick = {
                        category = categoryOption
                        expanded = false
                      })
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Explanation for "Description"
              Text(
                  text = context.getString(R.string.description_explanation),
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.testTag(SaveAssociationTestTags.DESCRIPTION_EXPLANATION_TEXT))

              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                  value = description,
                  onValueChange = { description = it },
                  label = { Text(context.getString(R.string.field_description)) },
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag(SaveAssociationTestTags.DESCRIPTION_TEXT_FIELD))

              Spacer(modifier = Modifier.height(16.dp))

              // Image selection tool
              Text(
                  text = context.getString(R.string.association_image_explanation),
                  style = MaterialTheme.typography.bodySmall)

              Spacer(modifier = Modifier.height(8.dp))

              if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = context.getString(R.string.field_selected_image),
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier.size(100.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(MaterialTheme.shapes.medium))
                Spacer(modifier = Modifier.height(8.dp))
              }

              Button(
                  onClick = { showSheet = true },
                  modifier = Modifier.align(Alignment.CenterHorizontally).testTag(PICTURE_BUTTON)) {
                    Text(text = context.getString(R.string.field_association_image))
                  }

              if (showSheet) {
                ModalBottomSheet(
                    modifier = Modifier.testTag(EventDetailsTestTags.PICTURE_SELECTION_SHEET),
                    sheetState = sheetState,
                    onDismissRequest = {
                      scope.launch {
                        sheetState.hide()
                        showSheet = false
                      }
                    },
                    content = {
                      PictureSelectionTool(
                          maxPictures = 1,
                          allowGallery = true,
                          allowCamera = true,
                          onValidate = { uris ->
                            selectedImageUri = uris.firstOrNull()
                            scope.launch {
                              sheetState.hide()
                              showSheet = false
                            }
                          },
                          onCancel = {
                            scope.launch {
                              sheetState.hide()
                              showSheet = false
                            }
                          },
                          initialSelectedPictures = listOfNotNull(selectedImageUri))
                    })
              }

              // Explanation for "URL"
              Text(
                  text = context.getString(R.string.url_explanation),
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.testTag(SaveAssociationTestTags.URL_EXPLANATION_TEXT))

              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                  value = url,
                  onValueChange = { url = it },
                  label = { Text(context.getString(R.string.field_url)) },
                  modifier =
                      Modifier.fillMaxWidth().testTag(SaveAssociationTestTags.URL_TEXT_FIELD))

              Spacer(modifier = Modifier.height(16.dp))

              Text(
                  text = context.getString(R.string.principal_email_address_explanation),
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.testTag(PRINCIPAL_EMAIL_ADDRESS_EXPLANATION_TEXT))

              Spacer(modifier = Modifier.height(8.dp))

              OutlinedTextField(
                  value = principalEmailAddress,
                  onValueChange = { principalEmailAddress = it },
                  label = { Text(context.getString(R.string.field_principal_email_address)) },
                  modifier = Modifier.fillMaxWidth().testTag(PRINCIPAL_EMAIL_ADDRESS_TEXT))

              Spacer(modifier = Modifier.height(24.dp))

              Row(
                  horizontalArrangement = Arrangement.End,
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.testTag(SaveAssociationTestTags.CANCEL_BUTTON)) {
                          Text(context.getString(R.string.cancel_button_text))
                        }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                          scope.launch {
                            val inputStream =
                                selectedImageUri?.let { uri ->
                                  context.contentResolver.openInputStream(uri)
                                }
                            onSave(
                                association.copy(
                                    name = name,
                                    fullName = fullName,
                                    description = description,
                                    category = category,
                                    url = url,
                                    principalEmailAddress = principalEmailAddress),
                                inputStream)
                          }
                        },
                        modifier = Modifier.testTag(SaveAssociationTestTags.SAVE_BUTTON)) {
                          Text(context.getString(R.string.save_button_text))
                        }
                  }
            }
      }
}
