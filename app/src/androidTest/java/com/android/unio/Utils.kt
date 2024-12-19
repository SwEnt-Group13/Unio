//package com.android.unio
//
//import androidx.compose.ui.test.SemanticsNodeInteraction
//import androidx.compose.ui.test.assertIsDisplayed
//import androidx.compose.ui.test.isNotDisplayed
//import androidx.compose.ui.test.junit4.ComposeContentTestRule
//import androidx.compose.ui.test.onNodeWithTag
//import androidx.compose.ui.test.performClick
//import androidx.compose.ui.test.performScrollTo
//import androidx.compose.ui.test.performTextInput
//import com.android.unio.model.authentication.unregisterAllAuthStateListeners
//import com.android.unio.model.firestore.unregisterAllSnapshotListeners
//import com.android.unio.model.strings.test_tags.authentication.SocialsOverlayTestTags
//import com.google.firebase.Firebase
//import com.google.firebase.auth.auth
//import io.mockk.clearAllMocks
//import io.mockk.unmockkAll
//import org.junit.After
//
///*
// * Scrolls to a component if it's not displayed and asserts if it is displayed
// */
//fun SemanticsNodeInteraction.assertDisplayComponentInScroll() {
//  if (this.isNotDisplayed()) {
//    this.performScrollTo()
//  }
//  this.assertIsDisplayed()
//}
//
///*
// * Adds a new user social to the list of user socials
// */
//fun addNewUserSocial(composeTestRule: ComposeContentTestRule, username: String, platform: String) {
//  composeTestRule.onNodeWithTag(SocialsOverlayTestTags.ADD_BUTTON).performScrollTo().performClick()
//  composeTestRule.onNodeWithTag(SocialsOverlayTestTags.PROMPT_TEXT_FIELD).performTextInput(username)
//  composeTestRule.onNodeWithTag(SocialsOverlayTestTags.PROMPT_DROP_BOX).performClick()
//  composeTestRule
//      .onNodeWithTag(SocialsOverlayTestTags.PROMPT_DROP_BOX_ITEM + platform)
//      .performClick()
//  composeTestRule.onNodeWithTag(SocialsOverlayTestTags.PROMPT_SAVE_BUTTON).performClick()
//}
//
//fun clearTest() {
//  Firebase.auth.unregisterAllAuthStateListeners()
//  unregisterAllSnapshotListeners()
//  unmockkAll()
//  clearAllMocks()
//}
//
//open class TearDown {
//  @After open fun tearDown() = clearTest()
//}
