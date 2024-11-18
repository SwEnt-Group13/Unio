package com.android.unio.model.firestore

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class FirestoreUtilsTest {
  @MockK private lateinit var task: Task<Any>

  private var onSuccessCalled = false
  private var onFailureCalled = false

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    onSuccessCalled = false
    onFailureCalled = false
  }

  @Test
  fun testPerformFirestoreOperationNonNullSuccess() {
    val taskListenerSlot = slot<OnSuccessListener<Any>>()

    every { task.addOnSuccessListener(capture(taskListenerSlot)) } answers
        {
          taskListenerSlot.captured.onSuccess("Success")
          task
        }
    every { task.addOnFailureListener(any<OnFailureListener>()) } returns task

    task.performFirestoreOperation(
        onSuccess = { result ->
          onSuccessCalled = true
          assertTrue(result == "Success")
        },
        onFailure = { onFailureCalled = true })

    assertTrue(onSuccessCalled)
    assertFalse(onFailureCalled)
  }

  @Test
  fun testPerformFirestoreOperationNullSuccessThrowsError() {
    val taskListenerSlot = slot<OnSuccessListener<Any>>()

    every { task.addOnSuccessListener(capture(taskListenerSlot)) } answers
        {
          taskListenerSlot.captured.onSuccess(
              mockk<DocumentSnapshot>(relaxed = true) { every { exists() } returns false })
          task
        }
    every { task.addOnFailureListener(any<OnFailureListener>()) } returns task

    task.performFirestoreOperation(
        onSuccess = { onSuccessCalled = true },
        onFailure = { exception ->
          onFailureCalled = true
          assertTrue(exception is NullPointerException)
          assertTrue(exception.message == "Result is null")
        })

    assertFalse(onSuccessCalled)
    assertTrue(onFailureCalled)
  }

  @Test
  fun testPerformFirestoreOperationFailure() {
    val failureListenerSlot = slot<OnFailureListener>()

    every { task.addOnSuccessListener(any<OnSuccessListener<Any>>()) } returns task
    every { task.addOnFailureListener(capture(failureListenerSlot)) } answers
        {
          failureListenerSlot.captured.onFailure(Exception("Failure"))
          task
        }

    task.performFirestoreOperation(
        onSuccess = { onSuccessCalled = true },
        onFailure = { exception ->
          onFailureCalled = true
          assertTrue(exception.message == "Failure")
        })

    assertFalse(onSuccessCalled)
    assertTrue(onFailureCalled)
  }
}
