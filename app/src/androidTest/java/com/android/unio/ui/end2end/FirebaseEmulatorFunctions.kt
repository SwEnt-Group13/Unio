package com.android.unio.ui.end2end

import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.user.User
import org.junit.Before

interface FirebaseEmulatorFunctions {

    /*
    * This method makes sure that the emulators are running
    * and sets the tests to use them
     */
    @Before
    fun setUp()

    /*
    * Verify that the local Firebase emulator is running.
    *
    * @throws Exception if the emulator is not running
    */
    fun verifyEmulatorsAreRunning()

    /*
    * Connects Firebase to the local emulators
    */
    fun useEmulators()

    /*
    * Delete all users in the Firebase Authentication emulator
    */
    fun flushAuthenticatedUsers()

    /*
    * Delete all documents in the Firestore emulator
    */
    fun flushFirestoreDatabase()

    fun signIn()


}