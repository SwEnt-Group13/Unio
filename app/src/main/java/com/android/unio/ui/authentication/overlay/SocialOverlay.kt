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
import com.android.unio.model.user.checkSocialContent
import com.android.unio.ui.theme.AppTypography
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SocialOverlay(
    onDismiss: () -> Unit,
    onSave: (MutableList<UserSocial>) -> Unit,
    userSocials: MutableList<UserSocial>
) {
    val scrollState = rememberScrollState()

    val copiedUserSocialsFlow = remember {
        MutableStateFlow(userSocials.map { it }.toMutableList())
    }
    val copiedUserSocials by copiedUserSocialsFlow.collectAsState()

    var showAddSocialPrompt by remember{ mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ){
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp),
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
                    Column(modifier = Modifier
                        .verticalScroll(scrollState)
                        .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally){
                        copiedUserSocials.forEachIndexed { index, userSocial ->
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier =
                                Modifier
                                    .padding(5.dp)
                                    .fillMaxWidth()
                                    .testTag("SocialsOverlayClickableRow: $index")){
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
                                        Image(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .wrapContentSize(),
                                            painter = painterResource(userSocial.social.icon),
                                            contentDescription = userSocial.social.title,
                                            contentScale = ContentScale.Fit)
                                        Text(userSocial.social.title)
                                    }
                                    Icon(
                                        Icons.Default.Close, contentDescription = "Close",
                                        modifier = Modifier.clickable {
                                            copiedUserSocialsFlow.value = copiedUserSocialsFlow.value.toMutableList().apply { removeAt(index) }
                                        })
                                }
                            }
                            if (index != copiedUserSocials.size - 1) {
                                Divider(
                                    modifier = Modifier.testTag("InterestOverlayDivider: $index")
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { showAddSocialPrompt = true },
                                modifier = Modifier
                                    .padding(8.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                                Text("Add Social")
                            }
                            Button(
                                onClick = {onSave(copiedUserSocials)},
                                modifier = Modifier.padding(8.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }
    }

    if(showAddSocialPrompt){
        SocialPrompt({showAddSocialPrompt = false}, {
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
){
    val socialsList = Social.entries.toList()

    var isExpanded by remember{ mutableStateOf(false) }

    var selectedSocial by remember {
        mutableStateOf(socialsList[0])
    }

    var socialURL by remember {
        mutableStateOf("")
    }

    var isError by remember {
        mutableStateOf(0)
    }

    var errorText by remember {
        mutableStateOf("")
    }

    Dialog(onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp),
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
                    placeholder = {
                        when(selectedSocial){
                            Social.FACEBOOK, Social.X,
                            Social.INSTAGRAM, Social.SNAPCHAT,
                            Social.TELEGRAM -> Text(text = "username", fontWeight = FontWeight.Bold)
                            Social.WHATSAPP -> Text(text = "41XXXXXXXXX", fontWeight = FontWeight.Bold)
                            Social.WEBSITE -> Text("https://www.mywebsite.com")
                        }},
                    prefix = {
                        when(selectedSocial){
                            Social.FACEBOOK -> Text(Social.FACEBOOK.URLshort)
                            Social.X -> Text(Social.X.URLshort)
                            Social.INSTAGRAM -> Text(Social.INSTAGRAM.URLshort)
                            Social.SNAPCHAT -> Text(Social.SNAPCHAT.URLshort)
                            Social.TELEGRAM -> Text(Social.TELEGRAM.URLshort)
                            Social.WHATSAPP -> Text(Social.WHATSAPP.URLshort)
                            Social.WEBSITE -> Text(Social.WEBSITE.URLshort)
                        }},
                    isError = (isError != 0),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.None // Disable automatic capitalization
                    ),
                    supportingText = {
                        if(isError != 0){
                            Text(errorText)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.padding(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ){
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val newUserSocial = UserSocial(selectedSocial, socialURL)
                            isError = checkSocialContent(newUserSocial)
                            if(isError != 0){
                                errorText = when(isError){
                                    1 -> "The text cannot be blank"
                                    2 -> "The phone number has incorrect format"
                                    3 -> "The website is not encoded with https://"
                                    else -> ""
                                }
                            }else{
                                onSave(newUserSocial)
                            }
                        },
                    ){
                        Text("Save changes")
                    }
                }
            }
        }
    }
}