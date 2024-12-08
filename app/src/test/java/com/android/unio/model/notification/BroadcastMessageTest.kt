package com.android.unio.model.notification

import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableReference
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.functions
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test

class BroadcastMessageTest {
  @MockK private lateinit var functions: FirebaseFunctions
  @MockK private lateinit var httpsCallableReference: HttpsCallableReference
  @MockK private lateinit var task: Task<HttpsCallableResult>

  @Before
  fun setUp() {
    MockKAnnotations.init(this)

    mockkStatic(FirebaseFunctions::class)
    every { Firebase.functions } returns functions

    every { functions.getHttpsCallable("broadcastMessage") } returns httpsCallableReference
    every { httpsCallableReference.call(any()) } returns task
    every { task.addOnFailureListener(any()) } returns task
  }

  @Test
  fun testInvalidParameters() {
    var onFailureCalled = false

    val payload = mapOf("title" to "title")
    broadcastMessage(
        NotificationTarget.EVENT_SAVERS,
        "topic",
        payload,
        { assert(false) { "onSuccess should not be called" } },
        { onFailureCalled = true })

    assert(onFailureCalled) { "onFailure should be called" }
  }

  @Test
  fun testValidParameters() {
    every { task.addOnSuccessListener(any()) } answers
        {
          val callback = it.invocation.args[0] as OnSuccessListener<HttpsCallableResult>
          callback.onSuccess(mockk())
          task
        }

    var onSuccessCalled = false

    val payload = mapOf("title" to "title", "body" to "body")
    broadcastMessage(
        NotificationTarget.EVENT_SAVERS,
        "topic",
        payload,
        { onSuccessCalled = true },
        { assert(false) { "onFailure should not be called" } })

    assert(onSuccessCalled) { "onSuccess should be called" }
  }
}
