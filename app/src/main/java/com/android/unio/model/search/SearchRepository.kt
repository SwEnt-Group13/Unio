package com.android.unio.model.search

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
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
import com.android.unio.model.event.Event
import com.android.unio.model.event.EventDocument
import com.android.unio.model.event.toEventDocument
import com.google.firebase.firestore.DocumentChange
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Repository for searching associations and events */
class SearchRepository(
    private val appContext: Context,
    private val associationRepository: AssociationRepository
) {
  private var session: AppSearchSession? = null

  /**
   * Creates the search database and connects it to the session [AppSearchSession]. Sets the
   * AppSearchSchema to be able to the search with the classes [AssociationDocument] and
   * [EventDocument], and listens for updates in the associations.
   */
  @RequiresApi(Build.VERSION_CODES.S)
  suspend fun init() {
    withContext(Dispatchers.IO) {
      val sessionFutures =
          LocalStorage.createSearchSessionAsync(
              LocalStorage.SearchContext.Builder(appContext, "unio").build())
      val setSchemaRequest =
          SetSchemaRequest.Builder()
              .addDocumentClasses(AssociationDocument::class.java, EventDocument::class.java)
              .build()

      session = sessionFutures.get()
      session?.setSchemaAsync(setSchemaRequest)

      listenForAssociationUpdates()
      // TODO listen for event updates
    }
  }

  /** Listens for updates in the associations collection in the Firestore
   * and reflects them to the search database. */
  private suspend fun listenForAssociationUpdates() {
    withContext(Dispatchers.IO) {
      associationRepository.addAssociationsListener { snapshots ->
        launch(Dispatchers.IO) {
          try {
            for (dc in snapshots.documentChanges) {
              when (dc.type) {
                DocumentChange.Type.ADDED,
                DocumentChange.Type.MODIFIED -> {
                  val association = dc.document.toObject(Association::class.java)
                  // TODO check if addAssociations should work with lists
                  addAssociations(listOf(association))
                }
                DocumentChange.Type.REMOVED -> {
                  val uid = dc.document.id
                  removeAssociation(uid)
                }
              }
            }
          } catch (e: Exception) {
            // TODO Handle exception
          }
        }
      }
    }
  }

    /**
     * Adds the given associations to the search database.
     *
     * @param associations the list of [Association] to add
     * @return true if the associations were added successfully, false otherwise
     */
  private suspend fun addAssociations(associations: List<Association>): Boolean {
    val associationDocuments = associations.map { it.toAssociationDocument() }
    return withContext(Dispatchers.IO) {
      session
          ?.putAsync(PutDocumentsRequest.Builder().addDocuments(associationDocuments).build())
          ?.get()
          ?.isSuccess == true
    }
  }

    /**
     * Removes the association with the given uid from the search database.
     *
     * @param uid the uid of the association to remove
     * @return true if the association was removed successfully, false otherwise
     */
  private suspend fun removeAssociation(uid: String): Boolean {
    return withContext(Dispatchers.IO) {
      session
          ?.removeAsync(
              // TODO check if this means I have to create a namespace
              RemoveByDocumentIdRequest.Builder("").addIds(uid).build())
          ?.get()
          ?.isSuccess == true
    }
  }

    /** TODO Adds the given events to the search database. */
  private suspend fun addEvents(events: List<Event>): Boolean {
    val eventDocuments = events.map { it.toEventDocument() }
    return withContext(Dispatchers.IO) {
      session
          ?.putAsync(PutDocumentsRequest.Builder().addDocuments(events).build())
          ?.get()
          ?.isSuccess == true
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

      //            val page = result.nextPageAsync.get()
      //
      //            // Do I need a page system?
      //            page.mapNotNull {
      //                if(it.genericDocument.schemaType ==
      // AssociationDocument::class.java.simpleName) {
      //                    it.getDocument(AssociationDocument::class.java)
      //                } else null
      //                //Return associations not association documents
      //            }

      val associations = mutableListOf<Association>()
        //TODO check if the pages are necessary
      var page = result.nextPageAsync.get()
      while (page.isNotEmpty()) {
        page.forEach {
          val doc = it.getDocument(AssociationDocument::class.java)
          associations.add(associationDocumentToAssociation(doc))
        }
        page = result.nextPageAsync.get()
      }
      associations.toList()
    }
  }

    /** TODO Searches the search database for events with the given query. */
  suspend fun searchEvents(query: String): List<EventDocument> {
    return withContext(Dispatchers.IO) {
      val searchSpec =
          SearchSpec.Builder()
              .setSnippetCount(10)
              .addFilterDocumentClasses(EventDocument::class.java)
              .setRankingStrategy(SearchSpec.RANKING_STRATEGY_NONE)
              .build()
      val result = session?.search(query, searchSpec) ?: return@withContext emptyList()

      val page = result.nextPageAsync.get()

      page.mapNotNull {
        if (it.genericDocument.schemaType == EventDocument::class.java.simpleName) {
          // And if yes how do I later get Association from AssociationDocument
          it.getDocument(EventDocument::class.java)
        } else null
      }
    }
  }

    /**
     * Converts the given [AssociationDocument] to an [Association].
     */
  private suspend fun associationDocumentToAssociation(
      associationDocument: AssociationDocument
  ): Association {
    return suspendCoroutine { continuation ->
      associationRepository.getAssociationWithId(
          id = associationDocument.uid,
          onSuccess = { association -> continuation.resume(association) },
          onFailure = { exception -> continuation.resumeWithException(exception) })
    }
  }

    /**
     * Closes the session and releases the resources.
     */
  fun closeSession() {
    session?.close()
    session = null
  }
}
