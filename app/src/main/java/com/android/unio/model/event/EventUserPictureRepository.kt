package com.android.unio.model.event

interface EventUserPictureRepository {

    fun addEventUserPicture(eventUserPicture: EventUserPicture, onSuccess: () -> Unit, onFailure:(Exception)->Unit)
    fun getNewUid(): String
}