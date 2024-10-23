package com.android.unio.model.association

import com.android.unio.model.firestore.ReferenceList
import com.android.unio.model.strings.AssociationStrings
import com.android.unio.model.user.User

/**
 * Association data class Make sure to update the hydration and serialization methods when changing
 * the data class
 *
 * @property uid association id
 * @property url association url
 * @property name association acronym
 * @property fullName association full name
 * @property description association description
 * @property members list of association members
 */
data class Association(
    val uid: String,
    val url: String,
    val name: String,
    val fullName: String,
    val category: AssociationCategory,
    val description: String,
    val members: ReferenceList<User>,
    var image: String
) {
  companion object
}

enum class AssociationCategory(val displayName: String) {
  EPFL_BODIES(AssociationStrings.EPFL_BODIES),
  REPRESENTATION(AssociationStrings.REPRESENTATION),
  PROJECTS(AssociationStrings.PROJECTS),
  EPFL_STUDENTS(AssociationStrings.EPFL_STUDENTS),
  COUNTRIES(AssociationStrings.COUNTRIES),
  SUSTAINABILITY(AssociationStrings.SUSTAINABILITY),
  SCIENCE_TECH(AssociationStrings.SCIENCE_TECH),
  CULTURE_SOCIETY(AssociationStrings.CULTURE_SOCIETY),
  ARTS(AssociationStrings.ARTS),
  ENTERTAINMENT(AssociationStrings.ENTERTAINMENT),
  SPORTS(AssociationStrings.SPORTS),
  GUIDANCE(AssociationStrings.GUIDANCE),
  UNKNOWN(AssociationStrings.UNKNOWN)
}
