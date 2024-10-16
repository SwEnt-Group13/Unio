package com.android.unio.model.association

import com.android.unio.model.firestore.ReferenceList
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
    val members: ReferenceList<User>
) {
  companion object
}

enum class AssociationCategory(val displayName: String) {
  EPFL_BODIES("EPFL bodies"),
  REPRESENTATION("Representation"),
  PROJECTS("Interdisciplinary projects"),
  EPFL_STUDENTS("EPFL Students"),
  COUNTRIES("Students by country"),
  SUSTAINABILITY("Sustainability"),
  SCIENCE_TECH("Science and technology"),
  CULTURE_SOCIETY("Culture and society"),
  ARTS("Arts"),
  ENTERTAINMENT("Entertainment"),
  SPORTS("Sports"),
  GUIDANCE("Vocational guidance"),
  UNKNOWN("Unknown")
}
