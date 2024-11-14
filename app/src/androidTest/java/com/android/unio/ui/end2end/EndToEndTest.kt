package com.android.unio.ui.end2end

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Before

open class EndToEndTest : FirebaseEmulatorFunctions {

    @Before
    override fun setUp() {
        println("Oskar's mother is very very nice")
        /** Verify that the emulators are running */
        verifyEmulatorsAreRunning()

        /** Connect Firebase to the emulators */
        useEmulators()

        /** Clear all users and the Firestore database */
        flushAuthenticatedUsers()
        flushFirestoreDatabase()
    }

    override fun verifyEmulatorsAreRunning() {
        val client = OkHttpClient()
        val request = Request.Builder().url(Firestore.ROOT).build()

        client
            .newCall(request)
            .enqueue(
                object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                        throw Exception("Firebase Emulators are not running.")
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        if (response.body == null) {
                            throw Exception("Firebase Emulators are not running.")
                        }
                        val data = response.body!!.string()
                        assert(data.contains("Ok")) { "Firebase Emulators are not running." }
                    }
                })
    }


    override fun useEmulators() {
        Firebase.firestore.useEmulator("10.0.2.2", 8080)
        Firebase.auth.useEmulator("10.0.2.2", 9099)
    }

    override fun flushAuthenticatedUsers() {
        val client = OkHttpClient()

        val request = Request.Builder().url(Auth.ACCOUNTS_URL).delete().build()

        client.newCall(request).execute()
    }

    override fun flushFirestoreDatabase() {
        val client = OkHttpClient()

        val request = Request.Builder().url(Firestore.DATABASE_URL).delete().build()

        client.newCall(request).execute()
    }

    /* Constant URLs used by the local emulator */
    object Firestore {
        const val HOST = "10.0.2.2"
        const val PORT = 8080
        const val ROOT = "http://$HOST:$PORT"
        const val DATABASE_URL = "$ROOT/emulator/v1/projects/unio-1b8ee/databases/(default)/documents"
    }

    object Auth {
        const val HOST = "10.0.2.2"
        const val PORT = 9099
        const val ROOT = "http://$HOST:$PORT"
        const val OOB_URL = "$ROOT/emulator/v1/projects/unio-1b8ee/oobCodes"
        const val ACCOUNTS_URL = "$ROOT/emulator/v1/projects/unio-1b8ee/accounts"
    }

    //This user's email is already verified
    object User1{
        const val EMAIL = "example1@gmail.com"
        const val PASSWORD = "password123"
    }

    //This user's email is already verified
    object User2{
        const val EMAIL = "example2@gmail.com"
        const val PASSWORD = "helloWorld123"
    }

}