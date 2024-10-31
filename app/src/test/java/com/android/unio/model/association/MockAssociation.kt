package com.android.unio.model.association

import com.android.unio.model.firestore.MockReferenceList
import com.android.unio.model.user.MockUser
import com.android.unio.model.user.MockUser.Companion.createAllMockUsers
import com.android.unio.model.user.User
import kotlin.random.Random

/**
 * MockAssociation class provides sample instances of the Association data class for testing
 * purposes.
 */
class MockAssociation {
  companion object {

    /** Helper function to generate a random UID for an association. */
    private fun getRandomUid(): String = "assoc${Random.nextInt(1000, 9999)}"

    /** Helper function to generate a random URL for an association. */
    private fun getRandomUrl(): String = "https://association${Random.nextInt(1, 100)}.com"

    /** Helper function to generate a random acronym for an association. */
    private fun getRandomName(): String {
      val names = listOf("EPFL", "Rep", "Sust", "Sports", "Culture", "Arts", "Tech")
      return names.random()
    }

    /** Helper function to generate a random full name for an association. */
    private fun getRandomFullName(): String {
      val fullNames =
          listOf(
              "EPFL Bodies",
              "Representation Group",
              "Sustainability Initiative",
              "Sports Association",
              "Cultural Society",
              "Art Collective",
              "Tech Enthusiasts")
      return fullNames.random()
    }

    /** Helper function to generate a random description for an association. */
    private fun getRandomDescription(): String {
      val descriptions =
          listOf(
              "A vibrant community focused on learning and growth.",
              "Committed to bringing people together for various events and activities.",
              "An association dedicated to promoting sustainability.",
              "An enthusiastic group of sports lovers and fitness advocates.",
              "Encouraging cultural awareness and engagement in the arts.",
              "A space for tech enthusiasts to collaborate and innovate.")
      return descriptions.random()
    }

    /** Helper function to generate a random image URL for an association. */
    private fun getRandomImage(): String {
      val images =
          listOf(
              "https://example.com/image1.png",
              "https://example.com/image2.png",
              "https://example.com/image3.png",
              "https://example.com/image4.png",
              "https://example.com/image5.png")
      return images.random()
    }

    /** Helper function to generate a random category for an association. */
    private fun getRandomAssociationCategory(): AssociationCategory {
      return AssociationCategory.values().random()
    }

    /** Creates a mock Association with randomized properties. */
    fun createMockAssociation(
        userDependency: Boolean = false,
        uid: String = getRandomUid(),
        url: String = getRandomUrl(),
        name: String = getRandomName(),
        fullName: String = getRandomFullName(),
        category: AssociationCategory = getRandomAssociationCategory(),
        description: String = getRandomDescription(),
        members: List<User> =
            if (userDependency) {
              emptyList()
            } else {
              MockUser.createAllMockUsers(associationDependency = false)
            }, // avoid circular dependency
        image: String = getRandomImage()
    ): Association {
      return Association(
          uid = uid,
          url = url,
          name = name,
          fullName = fullName,
          category = category,
          description = description,
          members = MockReferenceList(members),
          image = image)
    }

    /** Creates a list of mock Associations with randomized properties for each. */
    fun createAllMockAssociations(
        size: Int = Random.nextInt(1, 11),
        userDependency: Boolean = false
    ): List<Association> {
      return List(size) { createMockAssociation(userDependency = userDependency) }
    }
  }
}
