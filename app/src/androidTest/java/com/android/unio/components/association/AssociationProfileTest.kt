package com.android.unio.components.association

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.test.core.app.ApplicationProvider
import com.android.unio.R
import com.android.unio.TearDown
import com.android.unio.assertDisplayComponentInScroll
import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.firestore.MockReferenceElement
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.association.Member
import com.android.unio.model.association.Role
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventUserPictureRepositoryFirestore
import com.android.unio.model.event.EventViewModel
import com.android.unio.model.firestore.emptyFirestoreReferenceList
import com.android.unio.model.firestore.firestoreReferenceListWith
import com.android.unio.model.hilt.module.FirebaseModule
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.search.SearchRepository
import com.android.unio.model.search.SearchViewModel
import com.android.unio.model.strings.test_tags.association.AssociationProfileActionsTestTags
import com.android.unio.model.strings.test_tags.association.AssociationProfileTestTags
import com.android.unio.model.usecase.FollowUseCaseFirestore
import com.android.unio.model.usecase.SaveUseCaseFirestore
import com.android.unio.model.usecase.UserDeletionUseCaseFirestore
import com.android.unio.model.user.User
import com.android.unio.model.user.UserRepositoryFirestore
import com.android.unio.model.user.UserViewModel
import com.android.unio.ui.association.AssociationProfileScaffold
import com.android.unio.ui.association.AssociationProfileScreen
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(FirebaseModule::class)
class AssociationProfileTest : TearDown() {

  private lateinit var associations: List<Association>
  private lateinit var events: List<Event>

  @MockK private lateinit var navigationAction: NavigationAction
  private lateinit var eventViewModel: EventViewModel
  private lateinit var userViewModel: UserViewModel
  private lateinit var associationViewModel: AssociationViewModel

  private lateinit var searchViewModel: SearchViewModel
  @MockK(relaxed = true) private lateinit var searchRepository: SearchRepository

  @MockK private lateinit var associationRepository: AssociationRepositoryFirestore

  @MockK private lateinit var eventRepository: EventRepositoryFirestore

  @MockK private lateinit var concurrentAssociationUserRepository: FollowUseCaseFirestore

  @MockK private lateinit var userRepository: UserRepositoryFirestore
  @MockK private lateinit var userDeletionRepository: UserDeletionUseCaseFirestore

  @MockK private lateinit var imageRepository: ImageRepositoryFirebaseStorage
  @MockK
  private lateinit var eventUserPictureRepositoryFirestore: EventUserPictureRepositoryFirestore
  @MockK private lateinit var concurrentEventUserRepositoryFirestore: SaveUseCaseFirestore

  @MockK private lateinit var connectivityManager: ConnectivityManager

  @MockK private lateinit var task: Task<QuerySnapshot>

  @MockK private lateinit var querySnapshot: QuerySnapshot

  @MockK private lateinit var documentSnapshotA: DocumentSnapshot

  @MockK private lateinit var documentSnapshotB: DocumentSnapshot

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val hiltRule = HiltAndroidRule(this)

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    hiltRule.inject()

    mockkStatic(FirebaseFirestore::class)
    mockkStatic(Network::class)
    mockkStatic(ContextCompat::class)
    val db = mockk<FirebaseFirestore>()
    val collection = mockk<CollectionReference>()
    val query = mockk<Query>()

    val eventA = MockEvent.createMockEvent(uid = "a")
    val eventB = MockEvent.createMockEvent(uid = "b")

    events = listOf(eventA, eventB)

    every { getSystemService(any(), ConnectivityManager::class.java) } returns connectivityManager
    every { Firebase.firestore } returns db
    every { db.collection(any()) } returns collection
    every { collection.whereIn(any(FieldPath::class), any()) } returns query
    every { query.get() } returns task
    every { task.addOnSuccessListener(any()) }
        .answers { call ->
          val callback = call.invocation.args[0] as OnSuccessListener<QuerySnapshot>
          callback.onSuccess(querySnapshot)
          task
        }
    every { querySnapshot.documents } returns listOf(documentSnapshotA, documentSnapshotB)
    every { documentSnapshotA.data } answers
        {
          mapOf(
              "uid" to eventA.uid,
              "title" to eventA.title,
              "description" to eventA.description,
              "location" to eventA.location,
              "image" to eventA.image)
        }
    every { documentSnapshotB.data } answers
        {
          mapOf(
              "uid" to eventB.uid,
              "title" to eventB.title,
              "description" to eventB.description,
              "location" to eventB.location,
              "image" to eventB.image)
        }

    every { (task.addOnFailureListener(any())) } returns (task)

    // Mock the navigation action to do nothing
    every { navigationAction.navigateTo(any<String>()) } returns Unit
    every { navigationAction.goBack() } returns Unit

    val user =
        User(
            uid = "1",
            email = "",
            firstName = "",
            lastName = "",
            biography = "",
            savedEvents = Event.emptyFirestoreReferenceList(),
            followedAssociations = Association.emptyFirestoreReferenceList(),
            joinedAssociations = Association.emptyFirestoreReferenceList(),
            interests = emptyList(),
            socials = emptyList(),
            profilePicture = "",
        )

    associations =
        listOf(
            MockAssociation.createMockAssociation(
                uid = "a1",
                userDependency = true,
                members =
                    listOf(
                        Member(
                            MockReferenceElement(
                                MockUser.createMockUser(uid = "1", associationDependency = true)),
                            Role.ADMIN.uid)),
                events = Event.Companion.firestoreReferenceListWith(events.map { it.uid })),
            MockAssociation.createMockAssociation(
                uid = "a2",
                userDependency = true,
                members =
                    listOf(
                        Member(
                            MockReferenceElement(
                                MockUser.createMockUser(uid = "1", associationDependency = true)),
                            Role.ADMIN.uid)),
                events = Event.Companion.firestoreReferenceListWith(events.map { it.uid })),
        )

    every { eventRepository.init(any()) } answers { (args[0] as () -> Unit).invoke() }

    every { eventRepository.getEvents(any(), any()) } answers
        {
          val onSuccess = args[0] as (List<Event>) -> Unit
          onSuccess(events)
        }
    eventViewModel =
        EventViewModel(
            eventRepository,
            imageRepository,
            associationRepository,
            eventUserPictureRepositoryFirestore,
            concurrentEventUserRepositoryFirestore)

    every { associationRepository.init(any()) } answers { firstArg<() -> Unit>().invoke() }
    every { associationRepository.getAssociations(any(), any()) } answers
        {
          val onSuccess = args[0] as (List<Association>) -> Unit
          onSuccess(associations)
        }

    every { userRepository.init(any()) } answers { (args[0] as () -> Unit).invoke() }

    every { concurrentAssociationUserRepository.updateFollow(any(), any(), any(), any()) } answers
        {
          val onSuccess = args[2] as () -> Unit
          onSuccess()
        }
    every { userRepository.getUserWithId(any(), any(), any()) } answers
        {
          val onSuccess = args[1] as (User) -> Unit
          onSuccess(user)
        }
    every { userRepository.updateUser(user, any(), any()) } answers
        {
          val onSuccess = args[1] as () -> Unit
          onSuccess()
        }

    userViewModel = UserViewModel(userRepository, imageRepository, userDeletionRepository)
    userViewModel.addUser(user, {})

    associationViewModel =
        AssociationViewModel(
            associationRepository,
            eventRepository,
            imageRepository,
            concurrentAssociationUserRepository)
    associationViewModel.getAssociations()
    associationViewModel.selectAssociation(associations.first().uid)

    searchViewModel = spyk(SearchViewModel(searchRepository))
  }

  @Test
  fun testAssociationProfileDisplayComponent() {
    every { connectivityManager.activeNetwork } returns mockk<Network>()

    composeTestRule.setContent {
      ProvidePreferenceLocals {
        AssociationProfileScaffold(
            navigationAction,
            userViewModel,
            eventViewModel,
            associationViewModel,
            searchViewModel) {}
      }
    }
    composeTestRule.waitForIdle()

    assert(associationViewModel.selectedAssociation.value!!.events.list.value.isNotEmpty())

    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.SCREEN)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.IMAGE_HEADER)
        .assertDisplayComponentInScroll()
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.TITLE).assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.MORE_BUTTON)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.HEADER_FOLLOWERS)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.HEADER_MEMBERS)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.FOLLOW_BUTTON)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.DESCRIPTION)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.EVENT_TITLE)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.CONTACT_MEMBERS_TITLE)
        .assertDisplayComponentInScroll()
  }

  @Test
  fun testSeeMoreLessButton() {
    every { connectivityManager.activeNetwork } returns mockk<Network>()
    var seeMore = ""
    var seeLess = ""
    composeTestRule.setContent {
      ProvidePreferenceLocals {
        val context = ApplicationProvider.getApplicationContext<Context>()
        seeMore = context.getString(R.string.association_see_more)

        seeLess = context.getString(R.string.association_see_less)
        AssociationProfileScaffold(
            navigationAction,
            userViewModel,
            eventViewModel,
            associationViewModel,
            searchViewModel) {}
      }
    }
    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.EVENT_CARD + "a")
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.EVENT_CARD + "b")
        .assertIsNotDisplayed()

    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.SEE_MORE_BUTTON)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.SEE_MORE_BUTTON)
        .assertTextContains(seeMore)
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.SEE_MORE_BUTTON).performClick()

    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.EVENT_CARD + "a")
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.EVENT_CARD + "b")
        .assertDisplayComponentInScroll()

    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.SEE_MORE_BUTTON)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.SEE_MORE_BUTTON)
        .assertTextContains(seeLess)
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.SEE_MORE_BUTTON).performClick()

    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.EVENT_CARD + "a")
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.EVENT_CARD + "b")
        .assertIsNotDisplayed()
  }

  @Test
  fun testFollowAssociation() {
    every { connectivityManager.activeNetwork } returns mockk<Network>()

    val context: Context = ApplicationProvider.getApplicationContext()
    composeTestRule.setContent {
      ProvidePreferenceLocals {
        AssociationProfileScaffold(
            navigationAction,
            userViewModel,
            eventViewModel,
            associationViewModel,
            searchViewModel) {}
      }
    }
    val currentCount = associationViewModel.selectedAssociation.value!!.followersCount

    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.FOLLOW_BUTTON)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithText(context.getString(R.string.association_follow))
        .assertIsDisplayed()

    // Follow operation
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.FOLLOW_BUTTON).performClick()
    assert(userViewModel.user.value?.followedAssociations!!.contains(associations.first().uid))
    assert(associationViewModel.selectedAssociation.value!!.followersCount == currentCount + 1)
    composeTestRule
        .onNodeWithText(context.getString(R.string.association_unfollow))
        .assertIsDisplayed()
    composeTestRule.waitForIdle()
    // Unfollow operation
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.FOLLOW_BUTTON).performClick()
    composeTestRule
        .onNodeWithText(context.getString(R.string.association_follow))
        .assertIsDisplayed()
    assert(!userViewModel.user.value?.followedAssociations!!.contains(associations.first().uid))
    assert(associationViewModel.selectedAssociation.value!!.followersCount == currentCount)
  }

  @Test
  fun testFollowOffline() {
    // Disable internet connection in the test
    val context: Context = ApplicationProvider.getApplicationContext()
    every { connectivityManager.activeNetwork } returns null
    composeTestRule.setContent {
      ProvidePreferenceLocals {
        AssociationProfileScaffold(
            navigationAction,
            userViewModel,
            eventViewModel,
            associationViewModel,
            searchViewModel) {}
      }
    }

    val currentCount = associationViewModel.selectedAssociation.value!!.followersCount

    composeTestRule
        .onNodeWithTag(AssociationProfileTestTags.FOLLOW_BUTTON)
        .assertDisplayComponentInScroll()
    composeTestRule
        .onNodeWithText(context.getString(R.string.association_follow))
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag(AssociationProfileTestTags.FOLLOW_BUTTON).performClick()
    assert(!userViewModel.user.value?.followedAssociations!!.contains(associations.first().uid))
    assert(associationViewModel.selectedAssociation.value!!.followersCount == currentCount)
  }

  @Test
  fun testGoBackButton() {
    every { connectivityManager.activeNetwork } returns mockk<Network>()

    composeTestRule.setContent {
      ProvidePreferenceLocals {
        AssociationProfileScaffold(
            navigationAction,
            userViewModel,
            eventViewModel,
            associationViewModel,
            searchViewModel) {}
      }
    }

    composeTestRule.onNodeWithTag(AssociationProfileTestTags.GO_BACK_BUTTON).performClick()

    verify { navigationAction.goBack() }
  }

  @Test
  fun testAssociationProfileGoodId() {
    every { connectivityManager.activeNetwork } returns mockk<Network>()

    composeTestRule.setContent {
      ProvidePreferenceLocals {
        AssociationProfileScaffold(
            navigationAction,
            userViewModel,
            eventViewModel,
            associationViewModel,
            searchViewModel) {}
      }
    }

    composeTestRule.onNodeWithTag(AssociationProfileTestTags.TITLE).assertDisplayComponentInScroll()
    composeTestRule.onNodeWithText(associations.first().name).assertDisplayComponentInScroll()
  }

  @Test
  fun testAssociationProfileNoId() {
    every { connectivityManager.activeNetwork } returns mockk<Network>()

    associationViewModel.selectAssociation("3")
    composeTestRule.setContent {
      ProvidePreferenceLocals {
        AssociationProfileScreen(
            navigationAction, associationViewModel, searchViewModel, userViewModel, eventViewModel)
      }
    }

    composeTestRule.onNodeWithTag(AssociationProfileTestTags.SCREEN).assertIsNotDisplayed()
  }

  @Test
  fun testAddEventButtonOnline() {
    every { connectivityManager.activeNetwork } returns mockk<Network>()

    composeTestRule.setContent {
      ProvidePreferenceLocals {
        AssociationProfileScaffold(
            navigationAction,
            userViewModel,
            eventViewModel,
            associationViewModel,
            searchViewModel) {}
      }
    }

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.ACTIONS_PAGE).isDisplayed()
    }
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.ACTIONS_PAGE).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule
          .onNodeWithTag(AssociationProfileActionsTestTags.ADD_EVENT_BUTTON)
          .isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(AssociationProfileActionsTestTags.ADD_EVENT_BUTTON)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(AssociationProfileActionsTestTags.ADD_EVENT_BUTTON)
        .performScrollTo()
        .performClick()

    verify { navigationAction.navigateTo(Screen.EVENT_CREATION) }
  }

  @Test
  fun testAddEventButtonOffline() {
    every { connectivityManager.activeNetwork } returns null

    composeTestRule.setContent {
      ProvidePreferenceLocals {
        AssociationProfileScaffold(
            navigationAction,
            userViewModel,
            eventViewModel,
            associationViewModel,
            searchViewModel) {}
      }
    }

    composeTestRule.waitUntil(10000) {
      composeTestRule.onNodeWithTag(AssociationProfileTestTags.ACTIONS_PAGE).isDisplayed()
    }
    composeTestRule.onNodeWithTag(AssociationProfileTestTags.ACTIONS_PAGE).performClick()

    composeTestRule.waitUntil(10000) {
      composeTestRule
          .onNodeWithTag(AssociationProfileActionsTestTags.ADD_EVENT_BUTTON)
          .isDisplayed()
    }

    composeTestRule
        .onNodeWithTag(AssociationProfileActionsTestTags.ADD_EVENT_BUTTON)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(AssociationProfileActionsTestTags.ADD_EVENT_BUTTON)
        .performScrollTo()
        .performClick()

    verify(exactly = 0) { navigationAction.navigateTo(Screen.EVENT_CREATION) }
  }

  @Module
  @InstallIn(SingletonComponent::class)
  object FirebaseTestModule {
    @Provides fun provideFirestore(): FirebaseFirestore = mockk()
  }
}
