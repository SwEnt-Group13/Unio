package com.android.unio.model.firestore

import com.android.unio.R
import com.android.unio.resources.ResourceManager

object FirestorePaths {
  val ASSOCIATION_PATH: String
    get() = ResourceManager.getString(R.string.firestore_path_association)

  val USER_PATH: String
    get() = ResourceManager.getString(R.string.firestore_path_user)

  val EVENT_PATH: String
    get() = ResourceManager.getString(R.string.firestore_path_event)
}
