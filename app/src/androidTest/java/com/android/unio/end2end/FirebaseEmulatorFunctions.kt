//package com.android.unio.end2end
//
//import androidx.compose.ui.test.junit4.ComposeContentTestRule
//import org.junit.After
//import org.junit.Before
//
//interface FirebaseEmulatorFunctions {
//
//  /** This method makes sure that the emulators are running and sets the tests to use them */
//  @Before fun setUp()
//
//  /** This method clears the test data */
//  @After fun tearDown()
//
//  /**
//   * Verify that the local Firebase emulator is running.
//   *
//   * @throws Exception if the emulator is not running
//   */
//  fun verifyEmulatorsAreRunning()
//
//  /** Connects Firebase to the local emulators */
//  fun useEmulators()
//
//  /** Delete all users in the Firebase Authentication emulator */
//  fun flushAuthenticatedUsers()
//
//  /*
//   * Delete all documents in the Firestore emulator
//   */
//  fun flushFirestoreDatabase()
//
//  /**
//   * Signs in to firebase with given credentials
//   *
//   * @param composeTestRule the compose test rule
//   * @param email the email of the user
//   * @param password the password of the user
//   */
//  fun signInWithUser(composeTestRule: ComposeContentTestRule, email: String, password: String)
//
//  fun signOutWithUser(composeTestRule: ComposeContentTestRule)
//}
