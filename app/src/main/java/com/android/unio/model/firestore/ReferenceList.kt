package com.android.unio.model.firestore

interface ReferenceList {
  fun add(uid: String)

  fun addAll(uids: List<String>)

  fun requestAll()
}
