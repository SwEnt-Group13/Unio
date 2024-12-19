//package com.android.unio.components.user
//
//import androidx.compose.ui.test.assertIsDisplayed
//import androidx.compose.ui.test.assertTextEquals
//import androidx.compose.ui.test.junit4.createComposeRule
//import androidx.compose.ui.test.onNodeWithTag
//import androidx.navigation.NavHostController
//import com.android.unio.TearDown
//import com.android.unio.mocks.user.MockUser
//import com.android.unio.model.search.SearchRepository
//import com.android.unio.model.search.SearchViewModel
//import com.android.unio.model.strings.test_tags.user.UserProfileTestTags
//import com.android.unio.ui.navigation.NavigationAction
//import com.android.unio.ui.user.UserProfileBottomSheet
//import com.android.unio.ui.user.UserProfileScreenScaffold
//import dagger.hilt.android.testing.HiltAndroidRule
//import dagger.hilt.android.testing.HiltAndroidTest
//import io.mockk.MockKAnnotations
//import io.mockk.every
//import io.mockk.impl.annotations.MockK
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//
//@HiltAndroidTest
//class UserProfileTest : TearDown() {
//
//  @MockK private lateinit var navHostController: NavHostController
//  @MockK private lateinit var navigationAction: NavigationAction
//
//  @get:Rule val composeTestRule = createComposeRule()
//  @get:Rule val hiltRule = HiltAndroidRule(this)
//
//  private val user = MockUser.createMockUser()
//
//  private lateinit var searchViewModel: SearchViewModel
//  @MockK private lateinit var searchRepository: SearchRepository
//
//  @Before
//  fun setUp() {
//    MockKAnnotations.init(this, relaxed = true)
//    hiltRule.inject()
//
//    every { navigationAction.navigateTo(any<String>()) } returns Unit
//
//    searchViewModel = SearchViewModel(searchRepository)
//
//    navigationAction = NavigationAction(navHostController)
//  }
//
//  @Test
//  fun testEverythingIsDisplayed() {
//    composeTestRule.setContent { UserProfileScreenScaffold(user, navigationAction, false, {}, {}) }
//
//    composeTestRule.onNodeWithTag(UserProfileTestTags.PROFILE_PICTURE).assertExists()
//
//    composeTestRule.onNodeWithTag(UserProfileTestTags.NAME).assertExists()
//    composeTestRule
//        .onNodeWithTag(UserProfileTestTags.NAME)
//        .assertTextEquals("${user.firstName} ${user.lastName}")
//
//    composeTestRule.onNodeWithTag(UserProfileTestTags.BIOGRAPHY).assertExists()
//    composeTestRule.onNodeWithTag(UserProfileTestTags.BIOGRAPHY).assertTextEquals(user.biography)
//
//    user.socials.forEach { social ->
//      composeTestRule
//          .onNodeWithTag(UserProfileTestTags.SOCIAL_BUTTON + social.social.title)
//          .assertExists()
//    }
//
//    user.interests.forEach { interest ->
//      composeTestRule
//          .onNodeWithTag(UserProfileTestTags.INTEREST_CHIP + interest.name)
//          .assertExists()
//    }
//  }
//
//  @Test
//  fun testBottomSheet() {
//
//    composeTestRule.setContent { UserProfileBottomSheet(true, navigationAction) {} }
//
//    composeTestRule.onNodeWithTag(UserProfileTestTags.BOTTOM_SHEET).assertIsDisplayed()
//  }
//}
