package com.android.unio.ui.event

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.strings.test_tags.EventCreationTestTags
import com.android.unio.ui.navigation.NavigationAction
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.internal.zzac
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ScreenDisplayingTest {
  val user = MockUser.createMockUser(uid = "1")
  @MockK lateinit var navigationAction: NavigationAction
  @MockK private lateinit var firebaseAuth: FirebaseAuth

  // This is the implementation of the abstract method getUid() from FirebaseUser.
  // Because it is impossible to mock abstract method, this is the only way to mock it.
  @MockK private lateinit var mockFirebaseUser: zzac

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val hiltRule = HiltAndroidRule(this)

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    hiltRule.inject()

    mockkStatic(FirebaseAuth::class)
    every { Firebase.auth } returns firebaseAuth
    every { firebaseAuth.currentUser } returns mockFirebaseUser
  }

  @Test
  fun testEventCreationTagsDisplayed() {
    composeTestRule.setContent { EventCreationScreen(navigationAction) }

    composeTestRule.waitForIdle()

    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.TITLE))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_IMAGE))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.EVENT_TITLE))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(EventCreationTestTags.SHORT_DESCRIPTION))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.COAUTHORS))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(EventCreationTestTags.TAGGED_ASSOCIATIONS))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.DESCRIPTION))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.LOCATION))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.SAVE_BUTTON))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.END_TIME))
    assertDisplayComponentInScroll(composeTestRule.onNodeWithTag(EventCreationTestTags.START_TIME))
  }
}

/**
 * This function is a copy of the function with the same name from EditAssociationTest.kt. It should
 * be extracted to a common file in a future PR.
 */
private fun assertDisplayComponentInScroll(compose: SemanticsNodeInteraction) {
  if (compose.isNotDisplayed()) {
    compose.performScrollTo()
  }
  compose.assertIsDisplayed()
}
