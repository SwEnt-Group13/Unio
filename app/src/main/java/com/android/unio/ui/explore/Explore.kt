package com.android.unio.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.unio.model.association.Association

@Composable
fun ExploreScreen() {
    Scaffold(
        modifier = Modifier.testTag("exploreScreen"),
        /**
         * Here, we should have the bottom navigation bar.
         */
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .testTag("searchBar"),
                    placeholder = { Text("Search") },
                    trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") }
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AssociationType.entries.forEach { category ->
                        val filteredAssociations = mockAssociations
                            .filter { it.type == category }
                            .sortedBy { it.association.acronym }

                        if (filteredAssociations.isNotEmpty()) {
                            item {
                                Text(
                                    text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .testTag("categoryTitle")
                                )

                                // Horizontal scrollable list of associations
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(filteredAssociations.size) { index ->
                                        AssociationItem(filteredAssociations[index].association)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun AssociationItem(association: Association) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .width(80.dp)
        // Interaction (to see detailed screen about an association) can be defined here,
        // with the .clickable modifier
    ) {
        /**
         * Placeholder for the image later on when we find a way to get and store them,
         * for now just a gray box
         */
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = association.acronym,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )
    }
}

/**
 * Everything below is mock data. This file (and other classes)
 * should later be adapted to get data from the association repository.
 */
enum class AssociationType {
    MUSIC, FESTIVALS, INNOVATION, FACULTIES, SOCIAL, TECH, OTHER
}

data class MockAssociation(val association: Association, val type: AssociationType)

val mockAssociations = listOf(
    MockAssociation(
        Association(
            uid = "1",
            acronym = "Musical",
            fullName = "Musical Association",
            description = "AGEPoly Commission – stimulation of the practice of music on the campus",
            members = emptyList()
        ),
        AssociationType.MUSIC
    ),
    MockAssociation(
        Association(
            uid = "2",
            acronym = "Nuit De la Magistrale",
            fullName = "Nuit De la Magistrale Association",
            description = "AGEPoly Commission – party following the formal Magistrale Graduation Ceremony",
            members = emptyList()
        ),
        AssociationType.FESTIVALS
    ),
    MockAssociation(
        Association(
            uid = "3",
            acronym = "Balélec",
            fullName = "Festival Balélec",
            description = "Open-air unique en Suisse, organisée par des bénévoles étudiants.",
            members = emptyList()
        ),
        AssociationType.FESTIVALS
    ),
    MockAssociation(
        Association(
            uid = "4",
            acronym = "Artiphys",
            fullName = "Festival Artiphys",
            description = "Festival à l'EPFL",
            members = emptyList()
        ),
        AssociationType.FESTIVALS
    ),
    MockAssociation(
        Association(
            uid = "5",
            acronym = "Sysmic",
            fullName = "Festival Sysmic",
            description = "Festival à l'EPFL",
            members = emptyList()
        ),
        AssociationType.FESTIVALS
    ),
    MockAssociation(
        Association(
            uid = "6",
            acronym = "IFL",
            fullName = "Innovation Forum Lausanne",
            description = "Innovation Forum Lausanne",
            members = emptyList()
        ),
        AssociationType.INNOVATION
    ),
    MockAssociation(
        Association(
            uid = "7",
            acronym = "Clic",
            fullName = "Clic Association",
            description = "Association of EPFL Students of IC Faculty",
            members = emptyList()
        ),
        AssociationType.FACULTIES
    ),
)
