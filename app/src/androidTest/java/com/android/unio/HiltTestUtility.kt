package com.android.unio

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
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
}

/**
 * This object contains the Hilt modules that will be used to replace the modules in the app for
 * testing. This is useful when we want to replace the actual implementation of a module
 *
 * !!! The objects contained are applied for all tests in the androidTest source set !!!
 */
object HiltModuleAndroidTest {}
