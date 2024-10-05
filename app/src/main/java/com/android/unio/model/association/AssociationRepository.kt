package com.android.unio.model.association

interface AssociationRepository {
  fun getAssociations(onSuccess: (List<Association>) -> Unit, onFailure: (Exception) -> Unit)
}
