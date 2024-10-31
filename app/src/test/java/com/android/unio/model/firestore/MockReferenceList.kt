package com.android.unio.model.firestore

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockReferenceList<T>(elements: List<T> = emptyList()) : ReferenceList<T> {
  private val _list = MutableStateFlow(elements)
  override val list: StateFlow<List<T>> = _list

  override fun add(uid: String) {}

  override fun addAll(uids: List<String>) {}

  override fun requestAll(onSuccess: () -> Unit) {
    onSuccess()
  }
}
