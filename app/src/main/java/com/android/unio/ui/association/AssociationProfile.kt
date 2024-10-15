package com.android.unio.ui.association

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventCard
import com.android.unio.model.firestore.MockReferenceList
import com.android.unio.model.firestore.ReferenceList
import com.android.unio.resources.ResourceManager.getString
import com.android.unio.resources.ResourceManager.init
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssociationProfileScreen(navigationAction: NavigationAction) {
    val context = LocalContext.current
    init(context)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Association Profile",
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
                            contentDescription = getString(R.string.association_go_back)
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
        content = { padding -> AssociationProfileContent(padding, context) }
    )
}

@Composable
fun AssociationProfileContent(padding: PaddingValues, context: Context) {
    Column(
        modifier = Modifier
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        AssociationHeader(context)
        Spacer(modifier = Modifier.size(22.dp))
        AssociationDescription()
        Spacer(modifier = Modifier.size(15.dp))
        AssociationEventTitle()
        Spacer(modifier = Modifier.size(11.dp))
        AssociationProfileEvents(context)
        Spacer(modifier = Modifier.size(11.dp))
        UserCard(context)
        Spacer(modifier = Modifier.size(61.dp))
        AssociationRecruitment(context)
    }
}

@Composable
fun AssociationRecruitment(context: Context) {
    Text(
        text = "Join <Association> ?",
        style = AppTypography.headlineMedium,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
    Spacer(modifier = Modifier.size(13.dp))
    Text(
        text = "Here’s where you could help us. Click on a role to learn more",
        style = AppTypography.bodySmall,
        modifier = Modifier.padding(horizontal = 23.dp)
    )
    Spacer(modifier = Modifier.size(18.dp))
    Row(modifier = Modifier.padding(horizontal = 24.dp)) {
        OutlinedButton(
            onClick = {
                Toast.makeText(context, "<DEBUG> Not implemented yet", Toast.LENGTH_SHORT).show()
            },
            enabled = true
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add icon"
            )
            Spacer(Modifier.width(2.dp))
            Text("Graphic Designer")
        }
        Spacer(modifier = Modifier.width(10.dp))
        OutlinedButton(
            onClick = {
                Toast.makeText(context, "<DEBUG> Not implemented yet", Toast.LENGTH_SHORT).show()
            },
            enabled = true
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add icon"
            )
            Spacer(Modifier.width(2.dp))
            Text("Treasurer")
        }
    }
}

@Composable
fun UserCard(context: Context) {
    Text(
        getString(R.string.association_contact_members),
        style = AppTypography.headlineMedium,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
    Spacer(modifier = Modifier.size(4.dp))
    Box(
        modifier = Modifier
            .padding(horizontal = 23.dp)
            .width(366.dp)
            .height(40.dp)
            .background(Color.LightGray, RoundedCornerShape(12.dp))
            .padding(vertical = 2.dp, horizontal = 3.dp)
            .clickable {
                Toast
                    .makeText(context, "<DEBUG> Not implemented yet", Toast.LENGTH_SHORT)
                    .show()
            },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(115.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                //TODO: Replace with user's profile picture
                Icons.Filled.Person,
                contentDescription = "user's profile picture",
                Modifier.size(36.dp)
            )
            Text(
                text = "Casey Rue",
                style = AppTypography.headlineSmall
            )

        }
    }
}

@Composable
fun AssociationProfileEvents(context: Context) {
    Column(
        modifier = Modifier.padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EventCard(
            event = Event(
                organisers = MockReferenceList(),
                taggedAssociations = MockReferenceList()
            )
        ) { }
        Spacer(modifier = Modifier.size(11.dp))
        OutlinedButton(
            onClick = {
                Toast.makeText(context, "<DEBUG> Not implemented yet", Toast.LENGTH_SHORT)
                    .show()
            }, modifier = Modifier
                .padding(horizontal = 28.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "See more"
            )
            Spacer(Modifier.width(2.dp))
            Text(getString(R.string.association_see_more))
        }
    }
}

@Composable
fun AssociationEventTitle() {
    Text(
        getString(R.string.association_upcoming_events),
        modifier = Modifier.padding(horizontal = 20.dp),
        style = AppTypography.headlineMedium
    )
}

@Composable
fun AssociationDescription() {
    Text(
        getString(R.string.debug_lorem_ipsum),
        style = AppTypography.bodyMedium,
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

@Composable
fun AssociationHeader(context: Context) {
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
}
