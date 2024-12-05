package com.android.unio.model.event

import com.android.unio.model.firestore.FirestorePaths.EVENT_USER_PICTURES_PATH
import com.android.unio.model.firestore.performFirestoreOperation
import com.android.unio.model.firestore.transform.serialize
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class EventUserPictureRepositoryFirestore @Inject constructor(private val db: FirebaseFirestore) :
    EventUserPictureRepository {
    override fun getNewUid(): String {
        return db.collection(EVENT_USER_PICTURES_PATH).document().id
    }

    override fun addEventUserPicture(
        eventUserPicture: EventUserPicture,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (eventUserPicture.uid.isBlank()) {
            onFailure(IllegalArgumentException("No event picture id was provided"))
        } else {
            db.collection(EVENT_USER_PICTURES_PATH).document(eventUserPicture.uid)
                .set(serialize(eventUserPicture)).performFirestoreOperation(
                    onSuccess = { onSuccess() }, onFailure = { exception -> onFailure(exception) })
        }

    }

    // Note: the following line is needed to add external methods to the companion object
    companion object
}