package com.android.unio.model.association

interface AssociationRepository {
  fun init(onSuccess: () -> Unit)

  fun getAssociations(onSuccess: (List<Association>) -> Unit, onFailure: (Exception) -> Unit)

  fun getAssociationWithId(
      id: String,
      onSuccess: (Association) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun addAssociation(
      association: Association,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun updateAssociation(
      association: Association,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun deleteAssociationById(
      associationId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )
}
