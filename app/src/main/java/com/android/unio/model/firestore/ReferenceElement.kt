package com.android.unio.model.firestore

import kotlinx.coroutines.flow.StateFlow

interface ReferenceElement<T> {
  val element: StateFlow<T?>
  val uid: String

  fun set(uid: String)

  fun fetch(onSuccess: () -> Unit = {}, lazy: Boolean = false)
}
