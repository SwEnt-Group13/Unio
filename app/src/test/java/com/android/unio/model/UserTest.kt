package com.android.unio.model

import com.android.unio.model.association.Association
import com.android.unio.model.user.User
import junit.framework.TestCase.assertEquals
import org.junit.Test

class UserTest {
  @Test
  fun testUser() {
    val user = User("1", "John", "john@example.com", emptyList())
    assertEquals("1", user.id)
    assertEquals("John", user.name)
    assertEquals("john@example.com", user.email)
    assertEquals(emptyList<Association>(), user.followingAssociations)
  }
}
