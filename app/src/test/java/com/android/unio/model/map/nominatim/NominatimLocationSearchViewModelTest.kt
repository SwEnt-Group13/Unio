package com.android.unio.model.map.nominatim

import com.android.unio.model.map.Location
import kotlinx.coroutines.CoroutineDispatcher
import java.net.HttpURLConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class NominatimLocationSearchViewModelTest {
  private lateinit var server: MockWebServer
  private lateinit var apiService: NominatimApiService
  private lateinit var repository: NominatimLocationRepository
  private lateinit var mockResponseBody: String
  private lateinit var viewModel: NominatimLocationSearchViewModel
  private lateinit var testDispatcher: CoroutineDispatcher

  @Before
  fun setUp() {
    server = MockWebServer()
    server.start()

    apiService =
        Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApiService::class.java)

    repository = NominatimLocationRepository(apiService)

    testDispatcher = StandardTestDispatcher()
    Dispatchers.setMain(testDispatcher)

    viewModel = NominatimLocationSearchViewModel(repository, testDispatcher)

    // Mock Response Body
    mockResponseBody =
        """
            [
                {
                    "lat": "45.512331",
                    "lon": "7.559331",
                    "display_name": "Test Address, Test City, Test Country",
                    "address": {
                        "road": "Test Road",
                        "house_number": "123",
                        "postcode": "12345",
                        "city": "Test City",
                        "state": "Test State",
                        "country": "Test Country"
                    }
                }
            ]
        """
            .trimIndent()
  }

  @After
  fun tearDown() {
    server.shutdown()
    Dispatchers.resetMain()
  }

  @Test
  fun `initial state is empty`() = runTest {
    assertEquals("", viewModel.query.first())
    assertEquals(emptyList<Location>(), viewModel.locationSuggestions.first())
  }

  @Test
  fun `setQuery updates query state`() = runTest {
    val testQuery = "Test Query"
    viewModel.setQuery(testQuery)
    assertEquals(testQuery, viewModel.query.first())
  }

  @Test
  fun `query with results updates locationSuggestions`() = runTest {
    server.enqueue(
        MockResponse().setBody(mockResponseBody).setResponseCode(HttpURLConnection.HTTP_OK))

    val testQuery = "Test Query"
    viewModel.setQuery(testQuery)

    advanceTimeBy(1000)
    advanceUntilIdle()

    val suggestions = viewModel.locationSuggestions.first()
    assertEquals(1, suggestions.size)
    assertEquals("Test Road, 123, 12345 Test City, Test State, Test Country", suggestions[0].name)
    assertEquals(45.512331, suggestions[0].latitude, 0.0001)
    assertEquals(7.559331, suggestions[0].longitude, 0.0001)
  }

  @Test
  fun `query with empty results updates locationSuggestions to empty`() = runTest {
    server.enqueue(MockResponse().setBody("[]").setResponseCode(200))

    val testQuery = "No Results"
    viewModel.setQuery(testQuery)

    viewModel.query.first()

    val suggestions = viewModel.locationSuggestions.first()
    assertEquals(emptyList<Location>(), suggestions)
  }

  @Test
  fun `query with API error updates locationSuggestions to empty`() = runTest {
    server.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR))

    val testQuery = "Error Query"
    viewModel.setQuery(testQuery)

    viewModel.query.first()

    val suggestions = viewModel.locationSuggestions.first()
    assertEquals(emptyList<Location>(), suggestions)
  }

  @Test
  fun `query with empty string does not trigger search`() = runTest {
    val testQuery = ""
    viewModel.setQuery(testQuery)

    viewModel.query.first()

    val suggestions = viewModel.locationSuggestions.firstOrNull()
    assertEquals(emptyList<Location>(), suggestions)
  }
}
