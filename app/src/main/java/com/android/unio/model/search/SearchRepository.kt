package com.android.unio.model.search

import android.content.Context
import android.util.Log
import androidx.appsearch.app.AppSearchSession
import androidx.appsearch.app.PutDocumentsRequest
import androidx.appsearch.app.RemoveByDocumentIdRequest
import androidx.appsearch.app.SearchSpec
import androidx.appsearch.app.SetSchemaRequest
import androidx.appsearch.localstorage.LocalStorage
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationDocument
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.association.toAssociationDocument
import com.android.unio.model.authentication.registerAuthStateListener
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventDocument
import com.android.unio.model.event.EventRepository
import com.android.unio.model.event.toEventDocument
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.withContext

/**
 * Repository for searching associations and events
 *
 * @property appContext the application context
 * @property associationRepository the repository for associations
 * @property eventRepository the repository for events
 */
class SearchRepository
@Inject
constructor(
    @ApplicationContext private val appContext: Context,
    private val associationRepository: AssociationRepository,
    private val eventRepository: EventRepository
) {
  // Should be private, but I need it public for tests
  var session: AppSearchSession? = null

  /**
   * Creates the search database and connects it to the session [AppSearchSession]. Sets the
   * AppSearchSchema to be able to the search with the classes [AssociationDocument] and
   * [EventDocument], and listens for updates in the associations.
   */
  suspend fun init() {
    withContext(Dispatchers.IO) {
      Firebase.auth.registerAuthStateListener {
        if (it.currentUser != null) {
          try {
            val sessionFutures =
                LocalStorage.createSearchSessionAsync(
                    LocalStorage.SearchContext.Builder(appContext, "unio").build())
            val setSchemaRequest =
                SetSchemaRequest.Builder()
                    .addDocumentClasses(AssociationDocument::class.java, EventDocument::class.java)
                    .build()
            session = sessionFutures.get()
            session?.setSchemaAsync(setSchemaRequest)

            fetchAssociations()
            fetchEvents()
          } catch (e: Exception) {
            Log.e("SearchRepository", "failed to initialize search database", e)
          }
        }
      }
    }
  }

  /**
   * Calls the [AssociationRepository] to fetch all associations and adds them to the search
   * database. If the call fails, logs the exception.
   */
  fun fetchAssociations() {
    associationRepository.getAssociations(
        onSuccess = { associations -> addAssociations(associations) },
        onFailure = { exception ->
          Log.e("SearchRepository", "failed to fetch associations", exception)
        })
  }

  /**
   * Calls the [EventRepository] to fetch all events and adds them to the search database. If the
   * call fails, logs the exception.
   */
  fun fetchEvents() {
    eventRepository.getEvents(
        onSuccess = { events -> addEvents(events) },
        onFailure = { exception -> Log.e("SearchRepository", "failed to fetch events", exception) })
  }

  /**
   * Adds the given associations to the search database.
   *
   * @param associations the list of [Association] to add
   */
  // Should be private, but I need it public for tests
  fun addAssociations(associations: List<Association>) {
    val associationDocuments = associations.map { it.toAssociationDocument() }
    try {
      session?.putAsync(PutDocumentsRequest.Builder().addDocuments(associationDocuments).build())
    } catch (e: Exception) {
      Log.e("SearchRepository", "failed to add associations to search database", e)
    }
  }

  /**
   * Adds the given events to the search database.
   *
   * @param events the list of [Event] to add
   */
  // Should be private, but I need it public for tests
  fun addEvents(events: List<Event>) {
    val eventDocuments = events.map { it.toEventDocument() }
    try {
      session?.putAsync(PutDocumentsRequest.Builder().addDocuments(eventDocuments).build())
    } catch (e: Exception) {
      Log.e("SearchRepository", "failed to add event to search database", e)
    }
  }

  /**
   * Removes the association or event with the given uid from the search database.
   *
   * @param uid the uid of the association or event to remove
   */
  fun remove(uid: String) {
    try {
      session?.removeAsync(RemoveByDocumentIdRequest.Builder("unio").addIds(uid).build())
    } catch (e: Exception) {
      Log.e("SearchRepository", "failed to remove association from search database", e)
    }
  }

  /**
   * Searches the search database for associations with the given query.
   *
   * @param query the query to search for
   * @return a list of [Association] that match the query
   */
  suspend fun searchAssociations(query: String): List<Association> {
    return withContext(Dispatchers.IO) {
      val searchSpec =
          SearchSpec.Builder()
              .setSnippetCount(10)
              .addFilterDocumentClasses(AssociationDocument::class.java)
              .setRankingStrategy(SearchSpec.RANKING_STRATEGY_NONE)
              .build()
      val result = session?.search(query, searchSpec) ?: return@withContext emptyList()

      val associations = mutableListOf<Association>()
      val page = result.nextPageAsync.await()

      page.forEach {
        val doc = it.getDocument(AssociationDocument::class.java)
        associations.add(associationDocumentToAssociation(doc))
      }
      return@withContext associations.toList()
    }
  }

  /**
   * Searches the search database for events with the given query.
   *
   * @param query the query to search for
   * @return a list of [Event] that match the query
   */
  suspend fun searchEvents(query: String): List<Event> {
    return withContext(Dispatchers.IO) {
      val searchSpec =
          SearchSpec.Builder()
              .setSnippetCount(10)
              .addFilterDocumentClasses(EventDocument::class.java)
              .setRankingStrategy(SearchSpec.RANKING_STRATEGY_NONE)
              .build()
      val result = session?.search(query, searchSpec) ?: return@withContext emptyList()

      val events = mutableListOf<Event>()
      val page = result.nextPageAsync.await()

      page.forEach {
        val doc = it.getDocument(EventDocument::class.java)
        events.add(eventDocumentToEvent(doc))
      }
      return@withContext events.toList()
    }
  }

  /**
   * Converts the given [AssociationDocument] to an [Association].
   *
   * @param associationDocument the [AssociationDocument] to convert
   * @return the [Association] converted from the [AssociationDocument]
   */
  private suspend fun associationDocumentToAssociation(
      associationDocument: AssociationDocument
  ): Association {
    return suspendCoroutine { continuation ->
      associationRepository.getAssociationWithId(
          id = associationDocument.uid,
          onSuccess = { association -> continuation.resume(association) },
          onFailure = { exception ->
            Log.e(
                "SearchRepository",
                "failed to convert associationDocumentation to association ",
                exception)
            continuation.resumeWithException(exception)
          })
    }
  }

  /**
   * Converts the given [EventDocument] to an [Event].
   *
   * @param eventDocument the [EventDocument] to convert
   * @return the [Event] converted from the [EventDocument]
   */
  private suspend fun eventDocumentToEvent(eventDocument: EventDocument): Event {
    return suspendCoroutine { continuation ->
      eventRepository.getEventWithId(
          id = eventDocument.uid,
          onSuccess = { association -> continuation.resume(association) },
          onFailure = { exception ->
            Log.e("SearchRepository", "failed to convert eventDocumentation to event ", exception)
            continuation.resumeWithException(exception)
          })
    }
  }

  /** Closes the session and releases the resources. */
  fun closeSession() {
    session?.close()
    session = null
  }
}
