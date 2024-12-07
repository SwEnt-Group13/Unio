package com.android.unio.ui.utils

import android.content.Context
import android.widget.Toast
import io.mockk.*
import org.junit.Test

class ToastUtilsMockTest {

    @Test
    fun testShowToastCancelsPreviousToast() {
        mockkStatic(Toast::class)
        val mockToast = mockk<Toast>(relaxed = true)

        every { Toast.makeText(any(), any<String>(), any()) } returns mockToast

        val mockContext = mockk<Context>()

        ToastUtils.showToast(mockContext, "First Toast")
        verify { mockToast.show() }

        ToastUtils.showToast(mockContext, "Second Toast")
        verify { mockToast.cancel() }
        verify(exactly = 2) { mockToast.show() }

        unmockkAll()
    }
}
