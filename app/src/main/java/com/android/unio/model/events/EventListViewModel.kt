package com.android.unio.model.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EventListViewModel(private val repository: EventRepository) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events //cannot be modified by ui

    init {
        loadEvents()
    }

    private fun loadEvents() {
        _events.value = repository.getEvents()
    }

    // Factory for creating EventListViewModel with repository dependency
    companion object {
        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EventListViewModel(EventRepositoryMock()) as T
                }
            }
    }
}
