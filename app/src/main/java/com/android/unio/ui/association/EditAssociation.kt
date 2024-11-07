package com.android.unio.ui.association

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.unio.R
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.image.ImageRepository
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun EditAssociationScreen(
    associationId: String,
    associationViewModel: AssociationViewModel,
    imageRepository: ImageRepository,
    navigationAction: NavigationAction,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val associations by associationViewModel.associations.collectAsState()
    val association = associations.find { it.uid == associationId }

    if (association == null) {
        Text(
            text = context.getString(R.string.association_not_found),
            color = Color.Red,
            modifier = Modifier.padding(16.dp)
        )
    } else {
        var url by remember { mutableStateOf(TextFieldValue(association.url)) }
        var name by remember { mutableStateOf(TextFieldValue(association.name)) }
        var fullName by remember { mutableStateOf(TextFieldValue(association.fullName)) }
        var description by remember { mutableStateOf(TextFieldValue(association.description)) }
        var image by remember { mutableStateOf(TextFieldValue(association.image)) }

        var expanded by remember { mutableStateOf(false) }
        var category by remember { mutableStateOf(association.category) }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "Edit Association", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = category.displayName)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false } // Close menu if dismissed
            ) {
                AssociationCategory.values().forEach { categoryOption ->
                    DropdownMenuItem(
                        text = {
                            Text(text = categoryOption.displayName)
                        },
                        onClick = {
                            category = categoryOption
                            expanded = false // Close dropdown on selection
                        }
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = image,
                onValueChange = { image = it },
                label = { Text("Image URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = { navigationAction.navigateTo(Screen.withParams(Screen.ASSOCIATION_PROFILE, association.uid)) }) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            associationViewModel.saveAssociation(
                                association.copy(
                                    name = name.text,
                                    description = description.text,
                                    image = image.text
                                ),
                                null,
                                onSuccess = {
                                    Log.d("EditAssociationScreen", "Association saved successfully.")
                                    navigationAction.navigateTo(Screen.withParams(Screen.ASSOCIATION_PROFILE, association.uid))
                                },
                                onFailure = {
                                    Log.e("EditAssociationScreen", "Failed to save association.")
                                    Toast.makeText(context, "Failed to save association", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                ) {
                    Text("Save")
                }

            }
        }
    }
}
