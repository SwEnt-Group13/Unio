package com.android.unio.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class AssociationRepositoryFirestore(private val db: FirebaseFirestore) : AssociationRepository {

  override fun getAssociations(
      onSuccess: (List<Association>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(ASSOCIATION_PATH)
        .get()
        .addOnSuccessListener { result ->
          val associations = mutableListOf<Association>()
          for (document in result) {
            val association = hydrate(document)

            associations.add(association)
          }
          onSuccess(associations)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  fun hydrate(doc: DocumentSnapshot): Association {
    return Association(
        uid = doc.id,
        acronym = doc.getString("acronym") ?: "",
        fullName = doc.getString("fullName") ?: "",
        description = doc.getString("description") ?: "",
        members = doc.get("members") as? List<String> ?: emptyList())
  }

  companion object {
    private const val ASSOCIATION_PATH = "associations"
    private const val USER_PATH = "users"
  }
}
