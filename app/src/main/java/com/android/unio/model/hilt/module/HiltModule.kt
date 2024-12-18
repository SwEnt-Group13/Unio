package com.android.unio.model.hilt.module

import android.content.Context
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.association.AssociationRepositoryFirestore
import com.android.unio.model.event.EventRepository
import com.android.unio.model.event.EventRepositoryFirestore
import com.android.unio.model.event.EventUserPictureRepository
import com.android.unio.model.event.EventUserPictureRepositoryFirestore
import com.android.unio.model.usecase.ConcurrentAssociationUserRepository
import com.android.unio.model.usecase.ConcurrentAssociationUserRepositoryFirestore
import com.android.unio.model.image.ImageRepository
import com.android.unio.model.image.ImageRepositoryFirebaseStorage
import com.android.unio.model.map.LocationRepository
import com.android.unio.model.map.nominatim.NominatimApiService
import com.android.unio.model.map.nominatim.NominatimLocationRepository
import com.android.unio.model.save.ConcurrentEventUserRepository
import com.android.unio.model.save.ConcurrentEventUserRepositoryFirestore
import com.android.unio.model.user.UserDeletionRepository
import com.android.unio.model.user.UserDeletionRepositoryFirestore
import com.android.unio.model.user.UserRepository
import com.android.unio.model.user.UserRepositoryFirestore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
abstract class EventUserPictureModule {

  @Binds
  abstract fun bindEventUserPictureRepository(
      eventUserPictureRepositoryFirestore: EventUserPictureRepositoryFirestore
  ): EventUserPictureRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

  @Provides fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseAuthModule {

  @Provides fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth
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
abstract class ConcurrentAssociationUserModule {

  @Binds
  abstract fun bindConcurrentAssociationUserRepository(
      concurrentAssociationUserRepositoryFirestore: ConcurrentAssociationUserRepositoryFirestore
  ): ConcurrentAssociationUserRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UserDeletionModule {

  @Binds
  abstract fun bindUserDeletionRepository(
      userDeletionRepository: UserDeletionRepositoryFirestore
  ): UserDeletionRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ConcurrentEventUserModule {

  @Binds
  abstract fun bindConcurrentEventUserRepository(
      concurrentEventUserRepositoryFirestore: ConcurrentEventUserRepositoryFirestore
  ): ConcurrentEventUserRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseStorageModule {

  @Provides fun provideFirebaseStorage(): FirebaseStorage = Firebase.storage
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseMessagingModule {

  @Provides fun provideFirebaseMessaging(): FirebaseMessaging = Firebase.messaging
}

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

  @Provides
  fun provideFusedLocationProviderClient(
      @ApplicationContext context: Context
  ): FusedLocationProviderClient {
    return LocationServices.getFusedLocationProviderClient(context)
  }
}

/**
 * A Dagger module that provides the Nominatim API service.
 *
 * This module is specific to the Nominatim API service. If we want to switch APIs, we simply need
 * to update this module, and add a new ApiService interface, and that's it.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

  companion object {

    /**
     * Provides the Nominatim API service by defining the client and the URL builder, with HTTP
     * logging.
     */
    @Provides
    @Singleton
    fun provideNominatimApiService(): NominatimApiService {
      val logging = HttpLoggingInterceptor()
      logging.setLevel(HttpLoggingInterceptor.Level.BODY)

      val client = OkHttpClient.Builder().addInterceptor(logging).build()

      return Retrofit.Builder()
          .baseUrl("https://nominatim.openstreetmap.org/")
          .client(client)
          .addConverterFactory(GsonConverterFactory.create())
          .build()
          .create(NominatimApiService::class.java)
    }
  }

  @Binds
  @Singleton
  abstract fun bindLocationRepository(
      nominatimLocationRepository: NominatimLocationRepository
  ): LocationRepository
}
