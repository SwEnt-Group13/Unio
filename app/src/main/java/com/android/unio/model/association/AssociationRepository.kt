package com.android.unio.model.association

interface AssociationRepository {
  fun init(onSuccess: () -> Unit)

  fun getAssociations(onSuccess: (List<Association>) -> Unit, onFailure: (Exception) -> Unit)

  fun getAssociationsWithId(
      id: String,
      onSuccess: (Association) -> Unit,
      onFailure: (Exception) -> Unit
  )
}
