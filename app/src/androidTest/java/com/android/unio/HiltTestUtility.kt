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
