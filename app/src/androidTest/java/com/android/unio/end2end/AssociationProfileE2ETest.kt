//package com.android.unio.end2end
//
//import androidx.compose.ui.test.assertIsDisplayed
//import androidx.compose.ui.test.isDisplayed
//import androidx.compose.ui.test.onNodeWithTag
//import androidx.compose.ui.test.onNodeWithText
//import androidx.compose.ui.test.performClick
//import androidx.test.filters.LargeTest
//import com.android.unio.assertDisplayComponentInScroll
//import com.android.unio.model.strings.test_tags.association.AssociationProfileTestTags
//import com.android.unio.model.strings.test_tags.explore.ExploreTestTags
//import com.android.unio.model.strings.test_tags.home.HomeTestTags
//import com.android.unio.model.strings.test_tags.navigation.BottomNavBarTestTags
//import com.android.unio.model.strings.test_tags.user.SomeoneElseUserProfileTestTags
//import dagger.hilt.android.testing.HiltAndroidTest
//import org.junit.Test
//
//@LargeTest
//@HiltAndroidTest
//class AssociationProfileE2ETest : EndToEndTest() {
//  @Test
//  fun testAssociationProfileCanGoToSomeoneElseUserProfile() {
//    signInWithUser(composeTestRule, JohnDoe.EMAIL, JohnDoe.PASSWORD)
//
//    composeTestRule.waitUntil(10000) {
//      composeTestRule.onNodeWithTag(HomeTestTags.SCREEN).isDisplayed()
//    }
//
//    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).assertIsDisplayed()
//    composeTestRule.onNodeWithTag(BottomNavBarTestTags.EXPLORE).performClick()
//
//    composeTestRule.waitUntil(10000) {
//      composeTestRule.onNodeWithTag(ExploreTestTags.EXPLORE_SCAFFOLD_TITLE).isDisplayed()
//    }
//
//    composeTestRule.onNodeWithText(ASSOCIATION_NAME).assertDisplayComponentInScroll()
//    composeTestRule.onNodeWithText(ASSOCIATION_NAME).performClick()
//
//    composeTestRule.waitUntil(10000) {
//      composeTestRule.onNodeWithTag(AssociationProfileTestTags.SCREEN).isDisplayed()
//    }
//    Thread.sleep(1000)
//    composeTestRule.onNodeWithText(ASSOCIATION_MEMBERS).assertDisplayComponentInScroll()
//    composeTestRule.onNodeWithText(ASSOCIATION_MEMBERS).performClick()
//
//    composeTestRule.waitUntil(10000) {
//      composeTestRule.onNodeWithTag(SomeoneElseUserProfileTestTags.SCREEN).isDisplayed()
//    }
//
//    composeTestRule.onNodeWithTag(SomeoneElseUserProfileTestTags.NAME).assertIsDisplayed()
//
//    composeTestRule.waitUntil(10000) {
//      composeTestRule.onNodeWithTag(SomeoneElseUserProfileTestTags.GO_BACK).isDisplayed()
//    }
//
//    composeTestRule.onNodeWithTag(SomeoneElseUserProfileTestTags.GO_BACK).performClick()
//
//    composeTestRule.waitUntil(10000) {
//      composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).isDisplayed()
//    }
//
//    composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).performClick()
//
//    // had to go back mutliple times in order to sign out (because we need to be inside of one of
//    // the
//    // principal screens to sign out)
//    signOutWithUser(composeTestRule)
//  }
//
//  private companion object AssociationTarget {
//    const val ASSOCIATION_NAME = "Ebou"
//    const val ASSOCIATION_MEMBERS = "Renata Mendoza Flores"
//  }
//}
