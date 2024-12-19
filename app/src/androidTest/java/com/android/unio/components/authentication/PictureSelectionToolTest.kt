package com.android.unio.components.authentication

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.filters.LargeTest
import com.android.unio.TearDown
import com.android.unio.model.strings.test_tags.authentication.PictureSelectionToolTestTags
import com.android.unio.ui.components.PictureSelectionTool
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.internal.zzac
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
@HiltAndroidTest
class PictureSelectionToolTest : TearDown() {

    @MockK private lateinit var firebaseAuth: FirebaseAuth
    @MockK private lateinit var mockFirebaseUser: zzac

    @get:Rule val composeTestRule = createComposeRule()
    @get:Rule val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        // Mocking Firebase.auth and its behavior
        mockkStatic(FirebaseAuth::class)
        every { Firebase.auth } returns firebaseAuth
        every { firebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns "mocked-uid"
    }

    @Test
    fun testInitialUIState() {
        composeTestRule.setContent {
            PictureSelectionTool(
                maxPictures = 3,
                allowGallery = true,
                allowCamera = true,
                onValidate = {},
                onCancel = {},
                initialSelectedPictures = emptyList())
        }
        // Verify that initial UI elements are displayed
        composeTestRule.onNodeWithTag(PictureSelectionToolTestTags.GALLERY_ADD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(PictureSelectionToolTestTags.CAMERA_ADD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(PictureSelectionToolTestTags.CANCEL_BUTTON).assertIsDisplayed()
    }

    @Test
    fun testHavingPictures() {
        val mockUri1 = mockk<Uri>(relaxed = true)

        composeTestRule.setContent {
            PictureSelectionTool(
                maxPictures = 3,
                allowGallery = true,
                allowCamera = true,
                onValidate = {},
                onCancel = {},
                initialSelectedPictures = listOf(mockUri1))
        }
        // Verify that the selected pictures are displayed
        composeTestRule.onNodeWithTag(PictureSelectionToolTestTags.SELECTED_PICTURE).assertIsDisplayed()
        // Ensure that the Validate button is now visible
        composeTestRule.onNodeWithTag(PictureSelectionToolTestTags.VALIDATE_BUTTON).assertIsDisplayed()
    }
}