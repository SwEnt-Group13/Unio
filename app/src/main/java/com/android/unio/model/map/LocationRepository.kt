package com.android.unio.model.map

import kotlinx.coroutines.flow.Flow

interface LocationRepository {
  fun search(query: String): Flow<List<Location>>
}
