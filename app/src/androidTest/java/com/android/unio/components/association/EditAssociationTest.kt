package com.android.unio.components.association

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.NavHostController
import com.android.unio.TearDown
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.strings.test_tags.EditAssociationTestTags
import com.android.unio.ui.association.EditAssociationScaffold
import com.android.unio.ui.navigation.NavigationAction
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock

class EditAssociationTest : TearDown() {

  private lateinit var navHostController: NavHostController
  private lateinit var navigationAction: NavigationAction
  @Mock private lateinit var collectionReference: CollectionReference
  @Mock private lateinit var db: FirebaseFirestore
  @Mock private lateinit var associationRepository: AssociationRepository

  private lateinit var associations: List<Association>

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    associations =
        listOf(
            MockAssociation.createMockAssociation(uid = "1"),
            MockAssociation.createMockAssociation(uid = "2"))

    Mockito.`when`(db.collection(Mockito.anyString())).thenReturn(collectionReference)
    Mockito.`when`(associationRepository.getAssociations(any(), any())).thenAnswer { invocation ->
      val onSuccess: (List<Association>) -> Unit =
          invocation.arguments[0] as (List<Association>) -> Unit
      onSuccess(associations)
    }

    navHostController = mock()
    navigationAction = NavigationAction(navHostController)
  }

  @Test
  fun testEditAssociationScreenDisplaysCorrectly() {
    composeTestRule.setContent {
      EditAssociationScaffold(
          MockAssociation.createMockAssociation(uid = "1"), onCancel = {}, onSave = {})
    }

    composeTestRule.waitForIdle()

    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(EditAssociationTestTags.NAME_TEXT_FIELD))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(EditAssociationTestTags.FULL_NAME_TEXT_FIELD))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(EditAssociationTestTags.CATEGORY_BUTTON))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(EditAssociationTestTags.DESCRIPTION_TEXT_FIELD))
    assertDisplayComponentInScroll(
        composeTestRule.onNodeWithTag(EditAssociationTestTags.URL_TEXT_FIELD))
  }

  private fun assertDisplayComponentInScroll(compose: SemanticsNodeInteraction) {
    if (compose.isNotDisplayed()) {
      compose.performScrollTo()
    }
    compose.assertIsDisplayed()
  }
}
