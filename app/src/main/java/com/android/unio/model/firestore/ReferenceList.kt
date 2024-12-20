package com.android.unio.model.firestore

import kotlinx.coroutines.flow.StateFlow

interface ReferenceList<T> {
  val list: StateFlow<List<T>>
  val uids: List<String>

  fun add(uid: String)

  fun add(element: T)

  fun addAll(uids: List<String>)

  fun update(element: T)

  fun remove(uid: String)

  fun requestAll(onSuccess: () -> Unit = {}, lazy: Boolean = false)

  fun contains(uid: String): Boolean
}
