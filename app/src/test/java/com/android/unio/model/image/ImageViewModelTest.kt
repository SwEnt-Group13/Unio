package com.android.unio.model.image

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import java.io.InputStream
import org.junit.Before
import org.junit.Test

class ImageViewModelTest {
  @MockK private lateinit var imageRepositoryFirebaseStorage: ImageRepositoryFirebaseStorage
  @MockK private lateinit var inputStream: InputStream

  private lateinit var imageViewModel: ImageViewModel

  @Before
  fun setUp() {
    MockKAnnotations.init(this)

    imageViewModel = ImageViewModel(imageRepositoryFirebaseStorage)
  }

  @Test
  fun testUploadSucceeds() {
    every { imageRepositoryFirebaseStorage.uploadImage(any(), any(), any(), any()) } answers
        {
          (args[2] as (String) -> Unit)("url")
        }

    imageViewModel.uploadImage(
        inputStream, "path", { url -> assert(url == "url") }, { assert(false) })

    verify { imageRepositoryFirebaseStorage.uploadImage(inputStream, "path", any(), any()) }
  }

  @Test
  fun testUploadFails() {
    every { imageRepositoryFirebaseStorage.uploadImage(any(), any(), any(), any()) } answers
        {
          (args[3] as (Exception) -> Unit)(Exception())
        }

    imageViewModel.uploadImage(inputStream, "path", { url -> assert(false) }, { assert(true) })

    verify { imageRepositoryFirebaseStorage.uploadImage(inputStream, "path", any(), any()) }
  }
}
