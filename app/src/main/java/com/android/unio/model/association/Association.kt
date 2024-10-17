package com.android.unio.model.association

import androidx.appsearch.annotation.Document
import androidx.appsearch.annotation.Document.Id
import androidx.appsearch.annotation.Document.Namespace
import androidx.appsearch.annotation.Document.StringProperty
import androidx.appsearch.app.AppSearchSchema.StringPropertyConfig
import com.android.unio.model.firestore.ReferenceList
import com.android.unio.model.user.User

data class Association(
    val uid: String,
    val url: String = "",
    val acronym: String = "",
    val fullName: String = "",
    val description: String = "",
    val members: ReferenceList<User>
)

@Document
data class AssociationDocument(
    @Namespace val namespace: String = "",
    @Id val uid: String,
    @StringProperty(indexingType = StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val url: String = "",
    @StringProperty(indexingType = StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val acronym: String = "",
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
      acronym = this.acronym,
      fullName = this.fullName,
      description = this.description)
}
