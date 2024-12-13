package com.android.unio.mocks.firestore

import com.android.unio.model.firestore.ReferenceList
import com.android.unio.model.firestore.UniquelyIdentifiable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockReferenceList<T : UniquelyIdentifiable>(elements: List<T> = emptyList()) :
    ReferenceList<T> {
  private val _list = MutableStateFlow(elements)
  override val list: StateFlow<List<T>> = _list
  override val uids: List<String> = elements.map { it.uid }

  override fun add(uid: String) {}
  override fun add(element: T) {}

  override fun addAll(uids: List<String>) {}

  override fun remove(uid: String) {}

  override fun requestAll(onSuccess: () -> Unit, lazy: Boolean) {
    onSuccess()
  }

  override fun contains(uid: String): Boolean {
    return uids.contains(uid)
  }
}

class UniquelyIdentifiableString(override val uid: String) : UniquelyIdentifiable
