package com.android.unio.ui

/**
 * This object contains the Hilt modules that will be used to replace the modules in the app for
 * testing. This is useful when we want to replace the actual implementation of a module
 *
 * !!! The objects contained are applied for all tests in the androidTest source set !!!
 */
object HiltModuleAndroidTest {

  //  @Module
  //  @TestInstallIn(components = [SingletonComponent::class], replaces = [EventModule::class])
  //  abstract class EventModuleTest() {
  //
  //    @Binds
  //    abstract fun bindEventRepository(eventRepositoryMock: EventRepositoryMock): EventRepository
  //  }
  //  @Module
  //  @TestInstallIn(components = [SingletonComponent::class], replaces = [UserModule::class])
  //  object TestUserModule {
  //
  //    @Provides
  //    fun provideUserRepository(): UserRepository {
  //      val userRepository = mockk<UserRepositoryFirestore>(relaxed = true)
  //      every { userRepository.init(any()) } answers
  //          {
  //            println("UserRepository init Hilt")
  //            firstArg<() -> Unit>().invoke()
  //          }
  //      return userRepository
  //    }
  //  }
  //
  //  @Module
  //  @TestInstallIn(components = [SingletonComponent::class], replaces = [EventModule::class])
  //  object TestEventModule {
  //
  //    @Provides
  //    fun provideEventRepository(): EventRepository {
  //      val eventRepository = mockk<EventRepositoryFirestore>(relaxed = true)
  //      every { eventRepository.init(any()) } answers { firstArg<() -> Unit>().invoke() }
  //      return eventRepository
  //    }
  //  }
  //
  //  @Module
  //  @TestInstallIn(components = [SingletonComponent::class], replaces =
  // [AssociationModule::class])
  //  object TestAssociationModule {
  //
  //    @Provides
  //    fun provideAssociationRepository(): AssociationRepository {
  //      val associationRepository = mockk<AssociationRepositoryFirestore>(relaxed = true)
  //      every { associationRepository.init(any()) } answers { firstArg<() -> Unit>().invoke() }
  //      return associationRepository
  //    }
  //  }
  //
  //  @Module
  //  @TestInstallIn(components = [SingletonComponent::class], replaces = [ImageModule::class])
  //  object TestImageModule {
  //
  //    @Provides
  //    fun provideImageRepository(): ImageRepository {
  //      return mockk<ImageRepositoryFirebaseStorage>(relaxed = true)
  //    }
  //  }
}
