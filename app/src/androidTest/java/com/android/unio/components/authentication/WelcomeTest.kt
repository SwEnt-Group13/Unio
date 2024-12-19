package com.android.unio.components.authentication

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.android.unio.TearDown
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.authentication.AuthViewModel
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.strings.test_tags.authentication.WelcomeTestTags
import com.android.unio.model.usecase.UserDeletionUseCaseFirestore
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.authentication.WelcomeScreen
import com.android.unio.ui.navigation.NavigationAction
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class WelcomeTest : TearDown() {

    @get:Rule val composeTestRule = createComposeRule()

    val user = MockUser.createMockUser()

    private lateinit var userViewModel: UserViewModel
    private lateinit var authViewModel: AuthViewModel
    @MockK private lateinit var navigationAction: NavigationAction
    @MockK private lateinit var userRepository: UserRepositoryFirestore
    @MockK private lateinit var userDeletionRepository: UserDeletionUseCaseFirestore
    @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
    @MockK private lateinit var firebaseAuth: FirebaseAuth

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(FirebaseAuth::class)
        every { Firebase.auth } returns firebaseAuth
        every { firebaseAuth.addAuthStateListener(any()) } just runs
        every { firebaseAuth.removeAuthStateListener(any()) } just runs

        // Call first callback when init is called
        every { userRepository.init(any()) } answers { firstArg<() -> Unit>().invoke() }
        every { userRepository.getUserWithId(any(), any(), any()) } answers
                {
                    val onSuccess = args[1] as (User) -> Unit
                    onSuccess(user)
                }

        navigationAction = mock(NavigationAction::class.java)
        userViewModel = UserViewModel(userRepository, imageRepository, userDeletionRepository)
        authViewModel = AuthViewModel(firebaseAuth, userRepository)
    }

    @Test
    fun testWelcomeIsDisplayed() {
        composeTestRule.setContent { WelcomeScreen(navigationAction, authViewModel) }
        composeTestRule.onNodeWithTag(WelcomeTestTags.EMAIL).assertIsDisplayed()
        composeTestRule.onNodeWithTag(WelcomeTestTags.PASSWORD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(WelcomeTestTags.BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(WelcomeTestTags.BUTTON).assertHasClickAction()
        composeTestRule.onNodeWithTag(WelcomeTestTags.BUTTON).assertIsNotEnabled()
    }

    @Test
    fun testButtonEnables() {
        composeTestRule.setContent { WelcomeScreen(navigationAction, authViewModel) }
        composeTestRule.onNodeWithTag(WelcomeTestTags.BUTTON).assertIsNotEnabled()

        composeTestRule.onNodeWithTag(WelcomeTestTags.EMAIL).performTextInput("john.doe@epfl.ch")
        composeTestRule.onNodeWithTag(WelcomeTestTags.PASSWORD).performTextInput("123456")

        composeTestRule.onNodeWithTag(WelcomeTestTags.BUTTON).assertIsEnabled()
    }

    @After
    override fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }
}