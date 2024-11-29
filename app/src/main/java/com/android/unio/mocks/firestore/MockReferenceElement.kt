package com.android.unio.mocks.firestore

import com.android.unio.model.firestore.ReferenceElement
import com.android.unio.model.firestore.UniquelyIdentifiable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A mock implementation of [ReferenceElement] for testing purposes.
 */
class MockReferenceElement<T : UniquelyIdentifiable>(initialElement: T? = null) :
    ReferenceElement<T> {

    private val _element = MutableStateFlow(initialElement)
    override val element: StateFlow<T?> = _element

    private var _uid: String = initialElement?.uid ?: ""
    override val uid: String
        get() = _uid

    override fun set(uid: String) {
        _uid = uid
    }

    override fun fetch(onSuccess: () -> Unit, lazy: Boolean) {
        onSuccess()
    }
}
