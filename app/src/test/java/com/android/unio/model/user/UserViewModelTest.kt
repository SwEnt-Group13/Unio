package com.android.unio.model.user

import com.android.unio.model.firestore.MockReferenceList
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test

class UserViewModelTest {
  private val user =
      User(
          uid = "1",
          email = "1@gmail.com",
          firstName = "userFirst",
          lastName = "userLast",
          biography = "An example user",
          followedAssociations = MockReferenceList(listOf()),
          joinedAssociations = MockReferenceList(listOf()),
          interests = listOf(Interest.SPORTS, Interest.MUSIC),
          socials =
              listOf(
                  UserSocial(Social.INSTAGRAM, "Instagram"),
                  UserSocial(Social.WEBSITE, "example.com")),
          profilePicture = "https://www.example.com/image",
          savedEvents = MockReferenceList(listOf())
      )

  @MockK private lateinit var repository: UserRepository
  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {
    MockKAnnotations.init(this)

    every { repository.init(any()) } returns Unit

    userViewModel = UserViewModel(repository, false)
  }

  @Test
  fun testGetUserByUid() {
    every { repository.getUserWithId("123", any(), any()) } answers
        {
          val onSuccess = it.invocation.args[1] as (User) -> Unit
          onSuccess(user)
        }

    userViewModel.getUserByUid("123")

    verify { repository.getUserWithId("123", any(), any()) }

    // Check that refreshState is set to false
    assertEquals(false, userViewModel.refreshState.value)

    // Check that user is set to null
    assertEquals(user, userViewModel.user.value)
  }
}
