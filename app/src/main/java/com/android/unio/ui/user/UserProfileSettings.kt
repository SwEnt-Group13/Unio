package com.android.unio.ui.user

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.android.unio.R
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.strings.test_tags.AccountDetailsTestTags
import com.android.unio.model.user.AccountDetailsError
import com.android.unio.model.user.User
import com.android.unio.ui.components.ProfilePictureWithRemoveIcon
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.AppTheme
import me.zhanghai.compose.preference.ProvidePreferenceLocals

@Composable
fun UserProfileSettingsScreen(){

}


@Preview
@Composable
fun PreviewAccountSettings(

){
    val navController = rememberNavController()
    val navigationAction = NavigationAction(navController)


    val mockUser = MockUser.createMockUser()

    ProvidePreferenceLocals { AppTheme { UserProfileSettingsScreenContent(mockUser ,navigationAction) } }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileSettingsScreenContent(
    user: User,
    navigationAction: NavigationAction

){
    var firstName : String by remember { mutableStateOf(user.firstName) }
    var lastName : String by remember { mutableStateOf(user.lastName) }
    var bio : String by remember { mutableStateOf(user.biography) }

    var isErrors by remember { mutableStateOf(mutableSetOf<AccountDetailsError>()) }


    val profilePictureUri = remember { mutableStateOf<Uri>(Uri.parse(user.profilePicture)) }


    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            TopAppBar(
                title = { Text("Discard Changes")/*TODO*/ },
                navigationIcon = {
                    IconButton(
                        onClick = {},
                    ){
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back arrow"
                        )
                    }
                })
        },
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            ProfilePictureWithRemoveIcon(
                profilePictureUri.value,
                {profilePictureUri.value = Uri.EMPTY})

            EditUserTextFields(
                isErrors = isErrors,
                firstName = firstName,
                lastName = lastName,
                bio = bio,
                onFirstNameChange = {firstName = it},
                onLastNameChange = {lastName = it},
                onBioChange = {bio = it},
            )



        }
    }

}

@Composable
private fun EditUserTextFields(
    isErrors: MutableSet<AccountDetailsError>,
    firstName: String,
    lastName: String,
    bio: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit
) {
    val context = LocalContext.current
    val isFirstNameError = isErrors.contains(AccountDetailsError.EMPTY_FIRST_NAME)
    val isLastNameError = isErrors.contains(AccountDetailsError.EMPTY_LAST_NAME)

    OutlinedTextField(
        modifier =
        Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .testTag(AccountDetailsTestTags.FIRST_NAME_TEXT_FIELD),
        label = {
            Text(
                context.getString(R.string.account_details_first_name),
                modifier = Modifier.testTag(AccountDetailsTestTags.FIRST_NAME_TEXT))
        },
        isError = (isFirstNameError),
        supportingText = {
            if (isFirstNameError) {
                Text(
                    context.getString(AccountDetailsError.EMPTY_FIRST_NAME.errorMessage),
                    modifier = Modifier.testTag(AccountDetailsTestTags.FIRST_NAME_ERROR_TEXT))
            }
        },
        onValueChange = onFirstNameChange,
        value = firstName)

    OutlinedTextField(
        modifier =
        Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .testTag(AccountDetailsTestTags.LAST_NAME_TEXT_FIELD),
        label = {
            Text(
                context.getString(R.string.account_details_last_name),
                modifier = Modifier.testTag(AccountDetailsTestTags.LAST_NAME_TEXT))
        },
        isError = (isLastNameError),
        supportingText = {
            if (isLastNameError) {
                Text(
                    context.getString(AccountDetailsError.EMPTY_LAST_NAME.errorMessage),
                    modifier = Modifier.testTag(AccountDetailsTestTags.LAST_NAME_ERROR_TEXT))
            }
        },
        onValueChange = onLastNameChange,
        value = lastName)

    OutlinedTextField(
        modifier =
        Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(200.dp)
            .testTag(AccountDetailsTestTags.BIOGRAPHY_TEXT_FIELD),
        label = {
            Text(
                context.getString(R.string.account_details_bio),
                modifier = Modifier.testTag(AccountDetailsTestTags.BIOGRAPHY_TEXT))
        },
        onValueChange = onBioChange,
        value = bio)
}