// File: UserClaimAssociationPresidentialRightsScreenTest.kt

package com.android.unio.components.user

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.user.UserDeletionRepositoryFirestore
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.user.UserClaimAssociationPresidentialRightsScreenScaffold
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
  @MockK private lateinit var userRepository: UserRepositoryFirestore
  @MockK private lateinit var searchRepository: SearchRepository
  @MockK private lateinit var userDeletionRepository: UserDeletionRepositoryFirestore
  @MockK
  private lateinit var concurrentAssociationUserRepositoryFirestore:
      ConcurrentAssociationUserRepositoryFirestore
  @MockK private lateinit var navHostController: NavHostController

  private lateinit var associationViewModel: AssociationViewModel
  @MockK private lateinit var navigationAction: NavigationAction

  private lateinit var searchViewModel: SearchViewModel

  private lateinit var userViewModel: UserViewModel

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

    userViewModel = UserViewModel(userRepository, imageRepository, userDeletionRepository)

    searchViewModel = SearchViewModel(searchRepository)
  }

  @Test
  fun testBackButtonNavigatesBack() {
    composeTestRule.setContent {
      UserClaimAssociationPresidentialRightsScreenScaffold(
          navigationAction = navigationAction,
          associationViewModel = associationViewModel,
          user = MockUser.createMockUser(uid = "1"),
          searchViewModel = searchViewModel)
    }

    // click the back button
    composeTestRule.onNodeWithTag("goBackButton").performClick()
  }
}
