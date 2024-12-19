//package com.android.unio.components.association
//
//import androidx.compose.ui.test.assertHasClickAction
//import androidx.compose.ui.test.junit4.createComposeRule
//import androidx.compose.ui.test.onNodeWithTag
//import androidx.compose.ui.test.performClick
//import com.android.unio.TearDown
//import com.android.unio.model.strings.test_tags.association.AssociationProfileTestTags
//import com.android.unio.ui.association.AssociationProfileBottomSheet
//import org.junit.Rule
//import org.junit.Test
//
//class AssociationProfileBottomSheetTest : TearDown() {
//  @get:Rule val composeTestRule = createComposeRule()
//
//  @Test
//  fun testEverythingIsDisplayed() {
//    composeTestRule.setContent { AssociationProfileBottomSheet(true, {}, {}, {}) }
//
//    composeTestRule.onNodeWithTag(AssociationProfileTestTags.BOTTOM_SHEET).assertExists()
//
//    composeTestRule.onNodeWithTag(AssociationProfileTestTags.BOTTOM_SHEET_EDIT).assertExists()
//    composeTestRule
//        .onNodeWithTag(AssociationProfileTestTags.BOTTOM_SHEET_NOTIFICATION)
//        .assertExists()
//
//    composeTestRule
//        .onNodeWithTag(AssociationProfileTestTags.BOTTOM_SHEET_EDIT)
//        .assertHasClickAction()
//    composeTestRule
//        .onNodeWithTag(AssociationProfileTestTags.BOTTOM_SHEET_NOTIFICATION)
//        .assertHasClickAction()
//  }
//
//  @Test
//  fun testButtonActions() {
//    var editClicked = false
//    var notificationClicked = false
//    var closed = false
//
//    composeTestRule.setContent {
//      AssociationProfileBottomSheet(
//          true, { editClicked = true }, { notificationClicked = true }, { closed = true })
//    }
//
//    composeTestRule.onNodeWithTag(AssociationProfileTestTags.BOTTOM_SHEET_EDIT).performClick()
//    composeTestRule
//        .onNodeWithTag(AssociationProfileTestTags.BOTTOM_SHEET_NOTIFICATION)
//        .performClick()
//
//    // Check that the buttons were clicked
//    assert(editClicked)
//    assert(notificationClicked)
//
//    // Check that the bottom sheet was closed
//    assert(closed)
//  }
//}
