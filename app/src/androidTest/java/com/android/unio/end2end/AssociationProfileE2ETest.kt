package com.android.unio.end2end

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.unio.MainActivity
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AssociationProfileE2ETest: EndToEndTest() {

    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testAssociationProfileCorrectBehavior(){
        signInWithUser(composeTestRule, User1.EMAIL, User1.PASSWORD)
        

    }
}