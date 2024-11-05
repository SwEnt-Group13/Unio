package com.android.unio.mocks.association

import com.android.unio.mocks.firestore.MockReferenceList
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.user.User

/**
 * MockAssociation class provides edge-case instances of the Association data class for testing
 * purposes.
 */
class MockAssociation {

  companion object {

    /** Enums for each edge-case category * */
    enum class EdgeCaseUid(val value: String) {
      EMPTY(""),
      SPECIAL_CHARACTERS("dusha2é"),
      LONG("assoc-very-long-id-1234567890123456789012345678901234567890"),
      TYPICAL("assocNormal123")
    }

    enum class EdgeCaseUrl(val value: String) {
      EMPTY(""),
      TYPICAL("https://example.com"),
      LONG("https://assoc-with-very-long-url.com/path/path/path/path/path/path/path"),
      INVALID("invalid-url")
    }

    enum class EdgeCaseName(val value: String) {
      EMPTY(""),
      SHORT("EPFL"),
      LONG_WITH_ACCENTS("LongAssociationNameWithSpecialChar-éñ"),
      SINGLE_CHAR("N"),
      TYPICAL("Mucial")
    }

    enum class EdgeCaseFullName(val value: String) {
      EMPTY(""),
      TYPICAL("EPFL Bodies"),
      LONG("Association Full Name With Quite A Lot Of Characters For Testing"),
      SPECIAL_CHARACTERS("AssociationWithSpecial#Characters!")
    }

    enum class EdgeCaseDescription(val value: String) {
      EMPTY(""),
      SHORT("A brief association description."),
      LONG(
          "This is a very long association description intended to test the handling of large amounts of text. "
              .repeat(10)),
      SPECIAL_CHARACTERS(
          "Description with special characters like #, @, $, %, and accents é, ü, ñ.")
    }

    enum class EdgeCaseImage(val value: String) {
      EMPTY(""),
      TYPICAL("https://example.com/image1.png"),
      LONG(
          "https://example.com/very/long/path/to/image/that/may/exceed/length/limits/image12345.png"),
      INVALID("invalid-url")
    }

    /** Edge cases list for category enums * */
    val edgeCaseCategories = AssociationCategory.values().toList()

    /** Creates a mock Association with specified properties for testing edge cases. */
    fun createMockAssociation(
        eventDependency: Boolean = false,
        userDependency: Boolean = false,
        uid: String = "uid1",
        url: String = "https://example.com",
        name: String = "Bob",
        fullName: String = "Bab",
        category: AssociationCategory = AssociationCategory.ENTERTAINMENT,
        description: String = "This is the best description",
        image: String = "image1.png",
        members: List<User> = emptyList()
    ): Association {
      val membersHelper =
          if (userDependency) {
            members
          } else {
            MockUser.createAllMockUsers(
                associationDependency = true, eventDependency = eventDependency)
          }
      return Association(
          uid = uid,
          url = url,
          name = name,
          fullName = fullName,
          category = category,
          description = description,
          members = MockReferenceList(membersHelper),
          image = image,
          followersCount = 2)
    }

    fun createAllMockAssociations(
        eventDependency: Boolean = false,
        userDependency: Boolean = false,
        size: Int = 5
    ): List<Association> {
      return List(size) {
        createMockAssociation(eventDependency = eventDependency, userDependency = userDependency)
      }
    }
  }
}
