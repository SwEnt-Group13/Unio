package com.android.unio.model.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel class that manages the event list data and provides it to the UI. It uses an
 * [EventRepository] to load the list of events and exposes them through a [StateFlow] to be
 * observed by the UI.
 *
 * @property repository The [EventRepository] that provides the events.
 */
class EventListViewModel(private val repository: EventRepository) : ViewModel() {

  /**
   * A private mutable state flow that holds the list of events. It is internal to the ViewModel and
   * cannot be modified from the outside.
   */
  private val _events = MutableStateFlow<List<Event>>(emptyList())

  /**
   * A public immutable [StateFlow] that exposes the list of events to the UI. This flow can only be
   * observed and not modified.
   */
  val events: StateFlow<List<Event>> = _events

  /** Initializes the ViewModel by loading the events from the repository. */
  init {
    loadEvents()
  }

  /**
   * Loads the list of events from the repository asynchronously using coroutines and updates the
   * internal [MutableStateFlow].
   */
  fun loadEvents() {
    // Launch a coroutine in the ViewModel scope to load events asynchronously
    viewModelScope.launch {
      val eventList = repository.getEvents() // this will be asynchronous if the repository is async
      _events.value = eventList
    }
  }

  /**
   * Companion object that provides a factory for creating instances of [EventListViewModel]. This
   * factory is used to create the ViewModel with the [EventRepositoryMock] dependency.
   */
  companion object {
    /** A factory for creating [EventListViewModel] instances with the [EventRepositoryMock]. */
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          /**
           * Creates an instance of the [EventListViewModel].
           *
           * @param modelClass The class of the ViewModel to create.
           * @return The created ViewModel instance.
           * @throws IllegalArgumentException if the [modelClass] does not match.
           */
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EventListViewModel(EventRepositoryMock()) as T
          }
        }
  }
}
