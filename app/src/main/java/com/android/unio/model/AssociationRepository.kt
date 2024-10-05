package com.android.unio.model

interface AssociationRepository {
  fun getAssociations(onSuccess: (List<Association>) -> Unit, onFailure: (Exception) -> Unit)
}
