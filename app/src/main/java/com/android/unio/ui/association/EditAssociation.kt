package com.android.unio.ui.association

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun EditAssociationScreen(
    associationViewModel: AssociationViewModel,
    navigationAction: NavigationAction,
) {
  val context = LocalContext.current

  val association by associationViewModel.selectedAssociation.collectAsState()

  if (association == null) {
    Log.e("EditAssociationScreen", "Association not found.")
    Toast.makeText(context, "An error occurred.", Toast.LENGTH_SHORT).show()
    return
  }

  EditAssociationScaffold(
      association = association!!,
      onCancel = {
        associationViewModel.selectAssociation(association!!.uid)
        navigationAction.navigateTo(Screen.ASSOCIATION_PROFILE, Screen.EDIT_ASSOCIATION)
      },
      onSave = { newAssociation ->
        associationViewModel.saveAssociation(
            newAssociation,
            null,
            onSuccess = {
              Log.d("EditAssociationScreen", "Association saved successfully.")
              associationViewModel.selectAssociation(newAssociation.uid)
              navigationAction.navigateTo(Screen.ASSOCIATION_PROFILE, Screen.EDIT_ASSOCIATION)
            },
            onFailure = {
              Log.e("EditAssociationScreen", "Failed to save association.")
              Toast.makeText(
                      context, context.getString(R.string.save_failed_message), Toast.LENGTH_SHORT)
                  .show()
            })
      })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAssociationScaffold(
    association: Association,
    onCancel: () -> Unit,
    onSave: (Association) -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var url by remember { mutableStateOf(association.url) }
  var name by remember { mutableStateOf(association.name) }
  var fullName by remember { mutableStateOf(association.fullName) }
  var description by remember { mutableStateOf(association.description) }

  var expanded by remember { mutableStateOf(false) }
  var category by remember { mutableStateOf(association.category) }

  Scaffold(
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
                  text = context.getString(R.string.edit_association_title),
                  style = MaterialTheme.typography.headlineMedium,
                  modifier = Modifier.testTag("EditAssociationTitle"))
            })
      }) { padding ->
        Column(
            modifier =
                Modifier.padding(padding)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())) {

              // Explanation for "Name"
              Text(
                  text = context.getString(R.string.name_explanation),
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.testTag("NameExplanationText"))

              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                  value = name,
                  onValueChange = { name = it },
                  label = { Text("Name") },
                  modifier = Modifier.fillMaxWidth().testTag("NameTextField"))

              Spacer(modifier = Modifier.height(16.dp))

              // Explanation for "Full Name"
              Text(
                  text = context.getString(R.string.full_name_explanation),
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.testTag("FullNameExplanationText"))

              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                  value = fullName,
                  onValueChange = { fullName = it },
                  label = { Text("Full Name") },
                  modifier = Modifier.fillMaxWidth().testTag("FullNameTextField"))

              Spacer(modifier = Modifier.height(16.dp))

              // Explanation for "Category"
              Text(
                  text = context.getString(R.string.category_explanation),
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.testTag("CategoryExplanationText"))

              Spacer(modifier = Modifier.height(8.dp))

              // Category Button
              Button(
                  onClick = { expanded = true },
                  modifier = Modifier.fillMaxWidth().testTag("CategoryButton")) {
                    Text(text = category.displayName)
                  }

              DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                AssociationCategory.entries.forEach { categoryOption ->
                  DropdownMenuItem(
                      text = { Text(text = categoryOption.displayName) },
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
                  modifier = Modifier.testTag("DescriptionExplanationText"))

              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                  value = description,
                  onValueChange = { description = it },
                  label = { Text("Description") },
                  modifier = Modifier.fillMaxWidth().testTag("DescriptionTextField"))

              Spacer(modifier = Modifier.height(16.dp))

              // Explanation for "URL"
              Text(
                  text = context.getString(R.string.url_explanation),
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.testTag("UrlExplanationText"))

              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                  value = url,
                  onValueChange = { url = it },
                  label = { Text("URL") },
                  modifier = Modifier.fillMaxWidth().testTag("UrlTextField"))

              Spacer(modifier = Modifier.height(24.dp))

              Row(
                  horizontalArrangement = Arrangement.End,
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onCancel, modifier = Modifier.testTag("CancelButton")) {
                      Text(context.getString(R.string.cancel_button_text))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                          scope.launch {
                            onSave(
                                association.copy(
                                    name = name,
                                    fullName = fullName,
                                    description = description,
                                    category = category,
                                    url = url))
                          }
                        },
                        modifier = Modifier.testTag("saveButton")) {
                          Text(context.getString(R.string.save_button_text))
                        }
                  }
            }
      }
}
