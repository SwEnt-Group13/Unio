package com.android.unio.model.map.nominatim

import java.net.HttpURLConnection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NominatimLocationRepositoryTest {

  private lateinit var server: MockWebServer
  private lateinit var apiService: NominatimApiService
  private lateinit var repository: NominatimLocationRepository
  private lateinit var mockResponseBody: String

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
  }

  @Test
  fun searchReturnsSuggestions() = runTest {
    val query = "Test Query"

    server.enqueue(
        MockResponse().setBody(mockResponseBody).setResponseCode(HttpURLConnection.HTTP_OK))

    val results = repository.search(query).first()

    assertEquals(1, results.size)
    assertEquals("Test Road, 123, 12345, Test City, Test State, Test Country", results[0].name)
    assertEquals(45.512331, results[0].latitude, 0.0001)
    assertEquals(7.559331, results[0].longitude, 0.0001)
  }

  @Test
  fun searchHandlesAPIError() = runTest {
    server.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR))

    val query = "Will fail!!"

    val result = repository.search(query).firstOrNull()
    assertEquals(null, result)
  }

  @Test
  fun searchIntroducesDelay() = runTest {
    val query = "Test Query"
    server.enqueue(
        MockResponse().setBody(mockResponseBody).setResponseCode(HttpURLConnection.HTTP_OK))

    runBlocking {
      val startTime = System.currentTimeMillis()
      repository.search(query).first()
      val endTime = System.currentTimeMillis()

      val elapsedTime = endTime - startTime
      assert(elapsedTime >= 1000)
    }
  }
}
