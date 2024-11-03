package com.android.unio.model.event

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockkStatic
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class EventRepositoryInitTest {

  private lateinit var repository: EventRepositoryFirestore

  @Mock private lateinit var db: FirebaseFirestore
  @Mock private lateinit var auth: FirebaseAuth
  @Mock private lateinit var firebaseUser: FirebaseUser
  @Captor
  private lateinit var authStateListenerCaptor: ArgumentCaptor<FirebaseAuth.AuthStateListener>

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    mockkStatic(FirebaseAuth::class)
    every { FirebaseAuth.getInstance() } returns auth

    repository = EventRepositoryFirestore(db)
  }

  @Test
  fun initCallsOnSuccessWhenUserIsAuthenticated() {
    `when`(auth.currentUser).thenReturn(firebaseUser)
    var onSuccessCalled = false
    val onSuccess = { onSuccessCalled = true }

    repository.init(onSuccess)

    // Capture listener and trigger it
    verify(auth).addAuthStateListener(authStateListenerCaptor.capture())
    authStateListenerCaptor.value.onAuthStateChanged(auth)

    shadowOf(Looper.getMainLooper()).idle()

    // Assert that onSuccess was called
    assertTrue(onSuccessCalled)
  }

  @Test
  fun initDoesNotCallOnSuccessWhenUserIsNotAuthenticated() {
    `when`(auth.currentUser).thenReturn(null)
    var onSuccessCalled = false
    val onSuccess = { onSuccessCalled = true }

    repository.init(onSuccess)

    verify(auth).addAuthStateListener(authStateListenerCaptor.capture())
    authStateListenerCaptor.value.onAuthStateChanged(auth)

    shadowOf(Looper.getMainLooper()).idle()

    // Assert that onSuccess was not called
    assertFalse(onSuccessCalled)
  }
}
