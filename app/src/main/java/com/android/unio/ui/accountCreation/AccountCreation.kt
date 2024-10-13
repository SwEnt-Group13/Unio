package com.android.unio.ui.accountCreation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.onPrimaryLight
import com.android.unio.ui.theme.primaryLight

@Composable
fun AccountCreationScreen(navigationAction: NavigationAction) {
    var firstName : String by remember { mutableStateOf("") }
    var lastName : String by remember { mutableStateOf("") }
    var bio : String by remember { mutableStateOf("") }


    Column(
        modifier = Modifier.padding(16.dp).padding(vertical = 20.dp, horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(
                modifier = Modifier.padding(4.dp).fillMaxWidth(),
                label = { Text("First name") },
                onValueChange = { firstName = it },
                value = firstName)
            OutlinedTextField(
                modifier = Modifier.padding(4.dp).fillMaxWidth(),
                label = { Text("Last name") },
                onValueChange = { lastName = it },
                value = lastName)
            OutlinedTextField(
                modifier = Modifier.padding(4.dp).fillMaxWidth().height(200.dp),
                label = { Text("Bio") },
                onValueChange = { bio = it },
                value = bio)

            Row(){
                Text("Maybe Add a profile picture")
                Button(
                    onClick = {
                        /* TODO Handle adding profile picture */
                    }
                ){
                    Text("Add profile picture")
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick =  {
                    /* TODO Handle adding centers of interest */
                }
            ){
                Text("Add centers of interest")
            }
            Row(){
                /* TODO row containing dynamic list of interests */
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    /* TODO Handle adding interests */
                }
            ){
                Text("Add links to other social media")
            }
            Row(){
                /* TODO row containing dynamic list of social media links */
            }
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryLight,
                    contentColor = onPrimaryLight
                ),
                onClick = {
                    /* TODO Handle account creation */
                    navigationAction.navigateTo(Screen.HOME)
                }) {
                    Text("Continue")
            }
        }
}
