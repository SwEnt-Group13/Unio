package com.android.unio.model.user

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import java.security.cert.CertificateExpiredException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

class AuthTest {
  @Mock private lateinit var auth: FirebaseAuth
  @Mock private lateinit var authResult: AuthResult
  @Mock private lateinit var signInTask: Task<AuthResult>
  @Mock private lateinit var signUpTask: Task<AuthResult>
  @Mock private lateinit var user: FirebaseUser
  @Mock
  private lateinit var firebaseAuthInvalidCredentialsException:
      FirebaseAuthInvalidCredentialsException
  @Mock private lateinit var certificateExpiredException: CertificateExpiredException

  private val email = "john.doe@epfl.ch"
  private val pwd = "1234"

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)

    // Setup user
    `when`(authResult.user).thenReturn(user)
    `when`(user.email).thenReturn(email)

    // Setup method calls
    `when`(auth.signInWithEmailAndPassword(any(), any())).thenReturn(signInTask)
    `when`(auth.createUserWithEmailAndPassword(any(), any())).thenReturn(signUpTask)
  }

  @Test
  fun testSuccessSignIn() {
    // Immediately invoke success listener
    `when`(signInTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val callback = invocation.arguments[0] as OnSuccessListener<AuthResult>
      callback.onSuccess(authResult)
      signInTask
    }

    signInOrCreateAccount(email, pwd, auth) {
      assertEquals(SignInState.SUCCESS_SIGN_IN, it.state)
      assertNotNull(it.user)
      assertEquals(email, it.user!!.email)
    }
  }

  @Test
  fun testSuccessCreateAccount() {
    // Sign in fails, so account creation should automatically start
    `when`(signInTask.addOnSuccessListener(any())).thenReturn(signInTask)
    `when`(signInTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val callback = invocation.arguments[0] as OnFailureListener
      callback.onFailure(firebaseAuthInvalidCredentialsException)
      signInTask
    }

    // Immediately invoke account creation success listener
    `when`(signUpTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val callback = invocation.arguments[0] as OnSuccessListener<AuthResult>
      callback.onSuccess(authResult)
      signUpTask
    }

    signInOrCreateAccount(email, pwd, auth) {
      assertEquals(SignInState.SUCCESS_CREATE_ACCOUNT, it.state)
      assertNotNull(it.user)
      assertEquals(email, it.user!!.email)
    }
  }

  @Test
  fun testInvalidCredentials() {
    // Sign in fails, and the reason is not a FirebaseAuthInvalidCredentialsException
    // so that account creation does not start
    `when`(signInTask.addOnSuccessListener(any())).thenReturn(signInTask)
    `when`(signInTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val callback = invocation.arguments[0] as OnFailureListener
      callback.onFailure(certificateExpiredException)
      signInTask
    }

    signInOrCreateAccount(email, "invalid", auth) {
      assertEquals(SignInState.INVALID_CREDENTIALS, it.state)
      assertNull(it.user)
    }
  }

  @Test
  fun testInvalidEmailFormat() {
    signInOrCreateAccount("invalid", pwd, auth) {
      assertEquals(SignInState.INVALID_EMAIL_FORMAT, it.state)
      assertNull(it.user)
    }
  }

  @Test
  fun testEmailValidator() {
    assertEquals(true, isValidEmail("john.doe@abcd.com"))
    assertEquals(true, isValidEmail("john@abcd.com"))
    assertEquals(false, isValidEmail("john@abcd."))
    assertEquals(false, isValidEmail("john@.abcd"))
    assertEquals(false, isValidEmail("john@abcd"))
    assertEquals(false, isValidEmail("@abcd"))
    assertEquals(false, isValidEmail("abcd"))
  }

  @Test
  fun testPasswordValidator() {
    assertEquals(true, isValidPassword("ab6def"))
    assertEquals(false, isValidPassword("123"))
    assertEquals(false, isValidPassword("abc"))
    assertEquals(false, isValidPassword("abcdef"))
  }
}
