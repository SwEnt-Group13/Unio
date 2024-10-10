package com.android.unio.model.firestore

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ReferenceList<T> {
  val list: StateFlow<List<T>>

  fun add(uid: String)

  fun addAll(uids: List<String>)

  fun requestAll()
}

class MockReferenceList<T> : ReferenceList<T> {
  private val _uids = mutableListOf<String>()
  private val _list = MutableStateFlow<List<T>>(emptyList())
  override val list: StateFlow<List<T>> = _list

  override fun add(uid: String) {
    _uids.add(uid)
  }

  override fun addAll(uids: List<String>) {
    _uids.addAll(uids)
  }

  override fun requestAll() {
    _list.value = emptyList()
  }
}
