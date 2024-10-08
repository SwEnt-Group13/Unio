package com.android.unio.ui

import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Route
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.navigation.TopLevelDestinations
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class NavigationActionTest {

    private lateinit var navigationDestination: NavDestination
    private lateinit var navHostController: NavHostController
    private lateinit var navigationAction: NavigationAction

    @Before
    fun setUp() {
        navigationDestination = mock { NavDestination::class.java }
        navHostController = mock { NavHostController::class.java }
        navigationAction = NavigationAction(navHostController)
    }

    @Test
    fun testNavigateTo() {
        navigationAction.navigateTo(TopLevelDestinations.HOME)
        verify(navHostController).navigate(eq(Route.HOME), any<NavOptionsBuilder.() -> Unit>())

        navigationAction.navigateTo(Screen.EXPLORE)
        verify(navHostController).navigate(Screen.EXPLORE)
    }

    @Test
    fun testGoBack() {
        navigationAction.goBack()
        verify(navHostController).popBackStack()
    }

    @Test
    fun testGetCurrentRoute() {
        `when`(navHostController.currentDestination).thenReturn(navigationDestination)
        `when`(navigationDestination.route).thenReturn(Route.HOME)

        assertThat(navigationAction.getCurrentRoute(), `is`(Route.HOME))
    }

}