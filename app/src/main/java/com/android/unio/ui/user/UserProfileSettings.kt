package com.android.unio.ui.user

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.testTag
import com.android.unio.R
import com.android.unio.model.strings.test_tags.UserProfileTestTags
import com.android.unio.model.user.User
import com.android.unio.ui.components.ProfilePictureWithRemoveIcon
import com.android.unio.ui.navigation.BottomNavigationMenu
import com.android.unio.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Route
import java.net.URI

@Composable
fun UserProfileSettingsScreen(){

}


@Composable
fun UserProfileSettingsScreenContent(
    user: User,
    navigationAction: NavigationAction

){
    val profilePictureUri = remember { mutableStateOf<Uri>(Uri.parse(user.profilePicture)) }


    Scaffold(
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
            modifier = Modifier.padding(padding)
        ){
            ProfilePictureWithRemoveIcon(
                profilePictureUri.value,
                {profilePictureUri.value = Uri.EMPTY})

        }
    }

}