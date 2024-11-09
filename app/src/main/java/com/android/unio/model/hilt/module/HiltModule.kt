package com.android.unio.model.hilt.module

import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.event.EventRepository
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.image.ImageRepository
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.user.UserRepository
import com.android.unio.model.user.UserRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class EventModule {

  @Binds
  abstract fun bindEventRepository(
      eventRepositoryFirestore: EventRepositoryFirestore
  ): EventRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

  @Provides fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseAuthModule {

  @Provides fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AssociationModule {

  @Binds
  abstract fun bindAssociationRepository(
      associationRepositoryFirestore: AssociationRepositoryFirestore
  ): AssociationRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ImageModule {

  @Binds
  abstract fun bindImageRepository(
      imageRepositoryFirestore: ImageRepositoryFirebaseStorage
  ): ImageRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UserModule {

  @Binds
  abstract fun bindUserRepository(userRepositoryFirestore: UserRepositoryFirestore): UserRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseStorageModule {

  @Provides fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}
