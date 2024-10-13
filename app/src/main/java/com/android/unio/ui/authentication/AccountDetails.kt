package com.android.unio.ui.accountCreation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTheme
import com.android.unio.ui.theme.AppTypography

@Composable
fun AccountDetails(navigationAction: NavigationAction) {
  var firstName: String by remember { mutableStateOf("") }
  var lastName: String by remember { mutableStateOf("") }
  var bio: String by remember { mutableStateOf("") }


  val scrollState = rememberScrollState()
  Column(
    modifier = Modifier
      .padding(vertical = 20.dp, horizontal = 40.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    Text(
      text = "Tell us about yourself",
      fontSize = AppTypography.headlineSmall.fontSize
    )

    OutlinedTextField(
      modifier = Modifier
        .padding(4.dp)
        .fillMaxWidth(),
      label = { Text("First name") },
      onValueChange = { firstName = it },
      value = firstName
    )
    OutlinedTextField(
      modifier = Modifier
        .padding(4.dp)
        .fillMaxWidth(),
      label = { Text("Last name") },
      onValueChange = { lastName = it },
      value = lastName
    )
    OutlinedTextField(
      modifier = Modifier
        .padding(4.dp)
        .fillMaxWidth()
        .height(200.dp),
      label = { Text("Bio") },
      onValueChange = { bio = it },
      value = bio
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Maybe add a profile picture?",
        modifier = Modifier.widthIn(max = 140.dp),
        fontSize = AppTypography.headlineSmall.fontSize
      )
      Button(
        onClick = {
          /* TODO Handle adding profile picture */
        }
      ) {
        Box(
          modifier = Modifier.clip(CircleShape).size(100.dp).background(Color.Gray)
        ) {
          Text("Add")
        }
      }
    }
    OutlinedButton(
      modifier = Modifier.fillMaxWidth(),
      onClick = {
        /* TODO Handle adding centers of interest */
      }
    ) {
      Icon(Icons.Default.Add, contentDescription = "Add")
      Text("Add centers of interest")
    }
    Row() {
      /* TODO row containing dynamic list of interests */
    }
    OutlinedButton(
      modifier = Modifier.fillMaxWidth(),
      onClick = {
        /* TODO Handle adding interests */
      }
    ) {
      Icon(Icons.Default.Add, contentDescription = "Add")
      Text("Add links to other social media")
    }
    Row() {
      /* TODO row containing dynamic list of social media links */
    }
    Button(
      onClick = {
        /* TODO Handle account creation */
        navigationAction.navigateTo(Screen.HOME)
      }) {
      Text("Continue")
    }
  }
}




class ComposableTestActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
        val navController = rememberNavController()
        val navigationActions = NavigationAction(navController)
        setContent { Surface(modifier = Modifier.fillMaxSize()) { AppTheme { AccountDetails(navigationActions) } } }
    }
  }
}


