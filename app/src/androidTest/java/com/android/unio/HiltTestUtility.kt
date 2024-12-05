package com.android.unio

import android.app.Application
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnitRunner
import com.android.unio.end2end.EndToEndTest
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Instead of using the default [AndroidJUnitRunner], we use a custom runner that extends from
 * AndroidJUnitRunner and uses [HiltTestApplication] as the application class. This class is used to
 * configure the test application for Hilt.
 */
class HiltApplication : AndroidJUnitRunner() {
  override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
    return super.newApplication(cl, HiltTestApplication::class.java.name, context)
  }

  override fun onStart() {
    // Ensure the test class is a subclass of TearDown or EndToEndTest.
    val testClassName = InstrumentationRegistry.getArguments().getString("class")
    testClassName?.let { className ->
      try {
        val testClass = Class.forName(className
          .replace(Regex("#[a-zA-Z0-9 ]*$"), "")) // Remove test method suffix.

        val extendsTearDown = TearDown::class.java.isAssignableFrom(testClass)
        val extendsEndToEndTest = EndToEndTest::class.java.isAssignableFrom(testClass)
        if (!extendsTearDown && !extendsEndToEndTest) {
          throw IllegalStateException("Test class $className must extend TearDown or EndToEndTest.")
        }
      } catch (e: ClassNotFoundException) {
        throw RuntimeException("Test class not found: $className", e)
      }
    }
    super.onStart()
  }
}
