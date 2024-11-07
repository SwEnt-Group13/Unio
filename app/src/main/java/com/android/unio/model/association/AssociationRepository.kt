package com.android.unio.model.association

interface AssociationRepository {
  fun init(onSuccess: () -> Unit)

  fun getAssociations(onSuccess: (List<Association>) -> Unit, onFailure: (Exception) -> Unit)

  fun getAssociationsByCategory(
      category: AssociationCategory,
      onSuccess: (List<Association>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getAssociationWithId(
      id: String,
      onSuccess: (Association) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun saveAssociation( //handles both add and update
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
