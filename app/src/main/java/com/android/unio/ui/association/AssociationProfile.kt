package com.android.unio.ui.association

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.resources.ResourceManager.getString
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssociationProfileScreen(navigationAction: NavigationAction) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Association Profile",
//                        style = AppTypography.headlineLarge,
                        modifier = Modifier.testTag("AssociationProfileTitle")
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navigationAction.goBack() },
                        modifier = Modifier.testTag("goBackButton")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        //TODO: Implement association sharing
                        Toast.makeText(context, "<DEBUG> Not implemented yet", Toast.LENGTH_SHORT)
                            .show()
                    }) {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = "Icon for sharing association"
                        )
                    }
                })
        },
        modifier = Modifier.testTag("AssociationScreen"),
        content = { padding -> AssociationProfileContent(padding) }
    )
}

@Composable
fun AssociationProfileContent(padding: PaddingValues) {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(padding)) {
        Row {
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.adec),
                    contentDescription = "placeholder",
                    modifier = Modifier.size(124.dp)
                )
            }
            Column {
                Text(
                    "xxx followers",
                    style = AppTypography.headlineSmall,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                Text(
                    "yyy members",
                    style = AppTypography.headlineSmall,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
                Button(
                    onClick = {
                        Toast.makeText(
                            context,
                            "<DEBUG> Not implemented yet",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    enabled = false
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Follow icon"
                    )
                    Spacer(Modifier.width(2.dp))
                    Text("Follow")
                }
            }
        }
        Text(getString(R.string.debug_lorem_ipsum),
            style = AppTypography.bodyMedium,
            modifier = Modifier.padding(horizontal = 24.dp))
    }
}
