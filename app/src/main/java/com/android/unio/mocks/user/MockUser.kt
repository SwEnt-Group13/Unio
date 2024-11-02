package com.android.unio.mocks.user

import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.firestore.MockReferenceList
import com.android.unio.model.association.Association
import com.android.unio.model.user.Interest
import com.android.unio.model.user.Social
import com.android.unio.model.user.User
import com.android.unio.model.user.UserSocial
import kotlin.random.Random

/** MockUser class provides sample instances of the User data class for testing purposes. */
class MockUser {
  companion object {
    /**
     * Creates a mock User with customizable properties.
     *
     * @param uid User ID
     * @param email User email
     * @param firstName User's first name
     * @param lastName User's last name
     * @param biography User biography
     * @param followedAssociations List of associations the user follows
     * @param joinedAssociations List of associations the user has joined
     * @param interests User's interests
     * @param socials User's social accounts
     * @param profilePicture URL to user's profile picture
     */
    fun createMockUser(
        associationDependency: Boolean = false,
        uid: String = "user${Random.nextInt(1, 1000)}",
        email: String = "${getRandomName().toLowerCase()}@example.com",
        firstName: String = getRandomName(),
        lastName: String = getRandomName(),
        biography: String = "This is a random user biography for testing. ${Random.nextInt(100)}",
        followedAssociations: List<Association> =
            if (associationDependency) {
              emptyList()
            } else {
              MockAssociation.createAllMockAssociations(userDependency = true)
            }, // avoid circular dependency
        joinedAssociations: List<Association> =
            if (associationDependency) {
              emptyList()
            } else {
              MockAssociation.createAllMockAssociations(userDependency = true)
            }, // avoid circular dependency
        interests: List<Interest> =
            List(Random.nextInt(1, Interest.values().size + 1)) { getRandomInterest() },
        socials: List<UserSocial> =
            List(Random.nextInt(1, Social.values().size + 1)) { getRandomUserSocial() },
        profilePicture: String = "https://example.com/profile_${Random.nextInt(1, 100)}.png"
    ): User {
      return User(
          uid = uid,
          email = email,
          firstName = firstName,
          lastName = lastName,
          biography = biography,
          followedAssociations = MockReferenceList(followedAssociations),
          joinedAssociations = MockReferenceList(joinedAssociations),
          interests = interests,
          socials = socials,
          profilePicture = profilePicture)
    }

    /**
     * Creates a random UserSocial.
     *
     * @return Random UserSocial with random social platform and content.
     */
    private fun getRandomUserSocial(): UserSocial {
      val social = Social.values().random()
      val content =
          when (social) {
            Social.WHATSAPP -> Random.nextInt(100000000, 1000000000).toString()
            else -> "user${Random.nextInt(1, 10000)}"
          }
      return UserSocial(social, content)
    }

    /**
     * Gets a random Interest.
     *
     * @return Random Interest from the Interest enum.
     */
    private fun getRandomInterest(): Interest {
      return Interest.values().random()
    }

    /**
     * Gets a random name (for first and last names).
     *
     * @return Random string representing a name.
     */
    private fun getRandomName(): String {
      val names =
          listOf(
              "Alice", "Bob", "Charlie", "David", "Eva", "Frank", "Grace", "Hannah", "Ivy", "Jack")
      return names.random()
    }

    /** Creates a list of mock Users with different properties for testing purposes. */
    fun createAllMockUsers(
        size: Int = Random.nextInt(1, 11),
        associationDependency: Boolean = false
    ): List<User> {
      return List(size) { createMockUser(associationDependency = associationDependency) }
    }
  }
}
