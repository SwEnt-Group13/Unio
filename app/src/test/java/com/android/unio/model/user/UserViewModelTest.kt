package com.android.unio.model.user

import com.android.unio.mocks.user.MockUser
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test

class UserViewModelTest {
  private val user = MockUser.createMockUser()

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

  @Test
  fun testUpdateUser() {
    val user = MockUser.createMockUser(uid = "1")
    every { repository.updateUser(any(), any(), any()) } answers
        {
          val onSuccess = it.invocation.args[1] as () -> Unit
          onSuccess()
        }
    every { repository.getUserWithId("1", any(), any()) } answers
        {
          val onSuccess = it.invocation.args[1] as (User) -> Unit
          onSuccess(user)
        }
    userViewModel.updateUser(user)

    verify { repository.updateUser(user, any(), any()) }
  }
}
