package com.android.unio.model.firestore

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ReferenceList<T> {
  val list: StateFlow<List<T>>

  fun add(uid: String)

  fun addAll(uids: List<String>)

  fun remove(uid: String)

  fun requestAll(onSuccess: () -> Unit)

  fun contains(uid: String): Boolean
}