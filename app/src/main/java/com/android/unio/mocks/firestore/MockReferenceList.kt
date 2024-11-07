package com.android.unio.mocks.firestore

import com.android.unio.model.firestore.ReferenceList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockReferenceList<T>(elements: List<T> = emptyList()) : ReferenceList<T> {
  private val _list = MutableStateFlow(elements)
  override val list: StateFlow<List<T>> = _list
  override val uids: List<String> = emptyList()

  override fun add(uid: String) {}

  override fun addAll(uids: List<String>) {}

  override fun remove(uid: String) {}

  override fun requestAll(onSuccess: () -> Unit) {
    onSuccess()
  }

  override fun contains(uid: String): Boolean {
    return false
  }
}