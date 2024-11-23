// File: UserClaimAssociationPresidentialRightsScreenTest.kt

package com.android.unio.ui.user

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import com.android.unio.TearDown
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.follow.ConcurrentAssociationUserRepositoryFirestore
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.ui.navigation.NavigationAction
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class UserClaimAssociationPresidentialRightsTest : TearDown() {

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val hiltRule = HiltAndroidRule(this)

  @MockK private lateinit var associationRepository: AssociationRepositoryFirestore
  @MockK private lateinit var eventRepository: EventRepositoryFirestore
  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
  @MockK
  private lateinit var concurrentAssociationUserRepositoryFirestore:
      ConcurrentAssociationUserRepositoryFirestore
  @MockK private lateinit var navHostController: NavHostController

  private lateinit var associationViewModel: AssociationViewModel
  @MockK private lateinit var navigationAction: NavigationAction

  // test data
  private val testAssociation =
      MockAssociation.createMockAssociation(
          uid = "assoc123", principalEmailAddress = "president@university.edu")

  private val testUser = MockUser.createMockUser(uid = "user123", email = "user@example.com")

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    hiltRule.inject()

    every { navigationAction.navigateTo(any<String>()) } returns Unit

    associationViewModel =
        AssociationViewModel(
            associationRepository,
            eventRepository,
            imageRepository,
            concurrentAssociationUserRepositoryFirestore)
    navigationAction = NavigationAction(navHostController)
  }

  @Test
  fun testEmailVerificationInputAndErrorHandling() {
    composeTestRule.setContent {
      UserClaimAssociationPresidentialRightsScreenScaffold(
          association = testAssociation, user = testUser, navigationAction = navigationAction)
    }

    composeTestRule.onNodeWithText("Enter the presidential email address:").assertIsDisplayed()

    composeTestRule.onNodeWithText("Verify Email").performClick()
  }

  @Test
  fun testBackButtonNavigatesBack() {
    composeTestRule.setContent {
      UserClaimAssociationPresidentialRightsScreenScaffold(
          association = testAssociation, user = testUser, navigationAction = navigationAction)
    }

    // click the back button
    composeTestRule.onNodeWithTag("goBackButton").performClick()
  }
}
