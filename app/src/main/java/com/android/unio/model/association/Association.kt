package com.android.unio.model.association

import androidx.appsearch.annotation.Document
import androidx.appsearch.annotation.Document.Id
import androidx.appsearch.annotation.Document.Namespace
import androidx.appsearch.annotation.Document.StringProperty
import androidx.appsearch.app.AppSearchSchema.StringPropertyConfig
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

@Document
data class AssociationDocument(
    @Namespace val namespace: String = "",
    @Id val uid: String,
    @StringProperty(indexingType = StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val url: String = "",
    @StringProperty(indexingType = StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val name: String = "",
    @StringProperty(indexingType = StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val fullName: String = "",
    @StringProperty(indexingType = StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val description: String = ""
    // TODO add members
)

fun Association.toAssociationDocument(): AssociationDocument {
  return AssociationDocument(
      uid = this.uid,
      url = this.url,
      name = this.name,
      fullName = this.fullName,
      description = this.description)
}
