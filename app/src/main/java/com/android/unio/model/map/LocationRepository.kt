package com.android.unio.model.map

import kotlinx.coroutines.flow.Flow

/** Repository for searching locations. */
interface LocationRepository {

  /**
   * Searches for locations.
   *
   * @param query The search query.
   * @return A flow of suggested locations.
   */
  fun search(query: String): Flow<List<Location>>
}
