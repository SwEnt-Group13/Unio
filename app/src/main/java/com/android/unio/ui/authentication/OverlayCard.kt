package com.android.unio.ui.authentication

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Surface
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.unio.model.user.Interest
import com.android.unio.model.user.Social
import com.android.unio.model.user.UserSocial
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("InterestOverlayCard")) {
              Column(
                  modifier =
                  Modifier
                      .fillMaxWidth()
                      .padding(15.dp)
                      .sizeIn(maxHeight = 400.dp)
                      .testTag("InterestOverlayColumn"),
                  verticalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Your interests",
                        style = AppTypography.headlineSmall,
                        modifier = Modifier.testTag("InterestOverlayTitle"))
                    Text(
                        text = "Choose as many interests as you feel apply to you",
                        style = AppTypography.bodyMedium,
                        modifier =
                        Modifier
                            .padding(bottom = 5.dp)
                            .testTag("InterestOverlaySubtitle"))
                    Surface(
                        modifier = Modifier.sizeIn(maxHeight = 250.dp), color = Color.Transparent) {
                          Column(modifier = Modifier.verticalScroll(scrollState)) {
                            interestsState.forEachIndexed { index, pair ->
                              Row(
                                  horizontalArrangement = Arrangement.SpaceBetween,
                                  verticalAlignment = Alignment.CenterVertically,
                                  modifier =
                                  Modifier
                                      .padding(5.dp)
                                      .fillMaxWidth()
                                      .testTag("InterestOverlayClickableRow: $index")
                                      .clickable { pair.second.value = !pair.second.value }) {
                                    Text(
                                        text = pair.first.name,
                                        style = AppTypography.bodyMedium,
                                        modifier =
                                        Modifier
                                            .padding(start = 5.dp)
                                            .testTag("InterestOverlayText: ${pair.first.name}"))
                                    Checkbox(
                                        checked = pair.second.value,
                                        onCheckedChange = { pair.second.value = it },
                                        modifier =
                                            Modifier.testTag(
                                                "InterestOverlayCheckbox: ${pair.first.name}"))
                                  }
                              if (index != interestsState.size - 1) {
                                Divider(
                                    modifier = Modifier.testTag("InterestOverlayDivider: $index"))
                              }
                            }
                          }
                        }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Absolute.Right) {
                          Button(
                              onClick = onSave,
                              modifier =
                              Modifier
                                  .padding(5.dp)
                                  .testTag("InterestOverlaySaveButton")) {
                                Text(text = "Save")
                              }
                        }
                  }
            }
      }
}

@Composable
fun SocialsOverlay(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    userSocials: MutableStateFlow<MutableList<UserSocial>>
) {
    val userSocialState by userSocials.collectAsState()
    val scrollState = rememberScrollState()

    var showAddSocialPrompt by remember{ mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ){
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("SocialsOverlayCard")
        ){
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
                    .sizeIn(maxHeight = 400.dp)
                    .testTag("SocialsOverlayColumn"),
                verticalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Your social media",
                    style = AppTypography.headlineSmall,
                    modifier = Modifier.testTag("SocialsOverlayTitle"))
                Text(
                    text = "These will be displayed on your profile to allow other students to contact you",
                    style = AppTypography.bodyMedium,
                    modifier =
                    Modifier
                        .padding(bottom = 5.dp)
                        .testTag("SocialsOverlaySubtitle"))
                Surface(
                    modifier = Modifier.sizeIn(maxHeight = 250.dp), color = Color.Transparent) {
                    Column(modifier = Modifier.verticalScroll(scrollState)) {
                        userSocialState.forEachIndexed { index, userSocial ->
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier =
                                Modifier
                                    .padding(5.dp)
                                    .fillMaxWidth()
                                    .testTag("InterestOverlayClickableRow: $index")){
                                Row(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ){
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ){
                                        Icon(Icons.Default.AccountCircle, contentDescription = "AccountCircle")
                                        Text(userSocial.social.title)
                                    }
                                    Icon(Icons.Default.Close, contentDescription = "Close",
                                        modifier = Modifier.clickable { userSocialState.removeAt(index) })
                                }

                            }
                            if (index != userSocialState.size - 1) {
                                Divider(
                                    modifier = Modifier.testTag("InterestOverlayDivider: $index")
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        )
                        {
                            Icon(Icons.Default.AddCircleOutline,
                                contentDescription = "AddSocial",
                                modifier = Modifier
                                    .clickable { showAddSocialPrompt = true }
                                    .size(50.dp))
                            Text("Add Social")
                        }
                    }
                }
            }
        }
    }

    if(showAddSocialPrompt){
        SocialPrompt({showAddSocialPrompt = false}, userSocials)
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SocialPrompt(
    onDismiss: () -> Unit,
    userSocials: MutableStateFlow<MutableList<UserSocial>>
){
    val socialsList = Social.entries.toList()

    var isExpanded by remember{ mutableStateOf(false) }

    var selectedSocial by remember {
        mutableStateOf(socialsList[0])
    }

    var socialURL by remember {
        mutableStateOf("")
    }


    Log.e("9000", isExpanded.toString())
    Log.e("9001", selectedSocial.toString())

    Dialog(onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("SocialsOverlayCard")
        ){
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween) {
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = {isExpanded = !isExpanded},
                    modifier = Modifier.padding(10.dp)) {
                    TextField(
                        value = selectedSocial.title,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) })
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        socialsList.forEachIndexed { index, social ->
                            DropdownMenuItem(
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
                    onValueChange = {socialURL = it},
                    modifier = Modifier.padding(10.dp))
                Button(
                    onClick = {
                        val newUserSocial = UserSocial(selectedSocial, socialURL)
                        userSocials.value.add(newUserSocial)
                        onDismiss()
                    },
                ){
                    Text("Save changes")
                }
            }
        }
    }
}