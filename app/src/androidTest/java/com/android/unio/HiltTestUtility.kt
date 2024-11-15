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
object HiltModuleAndroidTest {
  /*@Module
  @TestInstallIn(components = [SingletonComponent::class], replaces = [UserModule::class])
  object TestUserModule {

    @Provides
    fun provideUserRepository(): UserRepository {
      val userRepository = mockk<UserRepositoryFirestore>(relaxed = true)
      every { userRepository.init(any()) } returns Unit
      return userRepository
    }
  }

  @Module
  @TestInstallIn(components = [SingletonComponent::class], replaces = [EventModule::class])
  object TestEventModule {

    @Provides
    fun provideEventRepository(): EventRepository {
      val eventRepository = mockk<EventRepositoryFirestore>(relaxed = true)
      every { eventRepository.init(any()) } returns Unit
      return eventRepository
    }
  }

  @Module
  @TestInstallIn(components = [SingletonComponent::class], replaces = [AssociationModule::class])
  object TestAssociationModule {

    @Provides
    fun provideAssociationRepository(): AssociationRepository {
      val associationRepository = mockk<AssociationRepositoryFirestore>(relaxed = true)
      every { associationRepository.init(any()) } returns Unit
      return associationRepository
    }
  }

  @Module
  @TestInstallIn(components = [SingletonComponent::class], replaces = [ImageModule::class])
  object TestImageModule {

    @Provides
    fun provideImageRepository(): ImageRepository {
      return mockk<ImageRepositoryFirebaseStorage>(relaxed = true)
    }
  }*/
}
