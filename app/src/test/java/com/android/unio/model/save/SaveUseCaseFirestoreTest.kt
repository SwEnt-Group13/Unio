package com.android.unio.model.save

import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.event.EventRepository
import com.android.unio.model.usecase.SaveUseCaseFirestore
import com.android.unio.model.user.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SaveUseCaseFirestoreTest {

    @MockK private lateinit var db: FirebaseFirestore
    @MockK private lateinit var userRepository: UserRepository
    @MockK private lateinit var eventRepository: EventRepository

    private lateinit var concurrentEventUserRepositoryFirestore: SaveUseCaseFirestore

    private val user = MockUser.createMockUser(uid = "1")
    private val event = MockEvent.createMockEvent(uid = "11")

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        concurrentEventUserRepositoryFirestore =
            SaveUseCaseFirestore(db, userRepository, eventRepository)
    }

    @Test
    fun testUpdateSave() {
        // Not very thorough testing but complicated to test more
        concurrentEventUserRepositoryFirestore.updateSave(user, event, {}, {})
        verify { db.runBatch(any()) }
    }
}