package com.android.unio.model.search

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.unio.model.association.Association
import com.android.unio.model.association.AssociationRepository
import com.android.unio.model.event.Event
import kotlinx.coroutines.launch

/** ViewModel for searching associations and events. It uses a [SearchRepository] to create the
 * AppSearch database and exposes the results through a [LiveData] containing a list of respectively
 * [Association] and [Event] */
@RequiresApi(Build.VERSION_CODES.S)
class SearchViewModel(private val repository: SearchRepository) : ViewModel() {
  private val _associations = MutableLiveData<List<Association>>()
  val associations: LiveData<List<Association>>
    get() = _associations

  private val _events = MutableLiveData<List<Event>>()
  val events: LiveData<List<Event>>
    get() = _events

  /** Initializes the ViewModel by creating the search database and connecting it to the session. */
  init {
    viewModelScope.launch { repository.init() }
  }

  /**
   * Searches the associations in the search database using the given query and
   * updates the internal [MutableLiveData].
   *
   * @param query The query to search for.
   */
  fun searchAssociations(query: String) {
    viewModelScope.launch {
      val results = repository.searchAssociations(query)
      _associations.postValue(results)
    }
  }

  // TODO make events functional
  //  fun searchEvents(query: String) {
  //    viewModelScope.launch {
  //      val results = repository.searchEvents(query)
  //      _events.postValue(results)
  //    }
  //  }

  // TODO check if this is needed
  override fun onCleared() {
    super.onCleared()
    repository.closeSession()
  }

  /**
   * Factory for creating a [SearchViewModel] with a constructor that takes a [SearchRepository].
   */
  companion object {
    fun provideFactory(
        context: Context,
        associationRepository: AssociationRepository
    ): ViewModelProvider.Factory {
      val appContext = context.applicationContext
      return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            val repository = SearchRepository(appContext, associationRepository)
            return SearchViewModel(repository) as T
          }
          throw IllegalArgumentException("Unknown ViewModel class")
        }
      }
    }
  }

  // Result is to be used as such
  //    searchViewModel.associations.observe(viewLifecycleOwner, Observer { associations ->
  //        // Update UI with associations
  //    })
}
