package com.android.unio.ui.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertEquals
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

    @Test
    fun testNavigateToAssociationProfileFromExplore() {
        navigationAction.navigateTo(TopLevelDestinations.EXPLORE)
        verify(navHostController).navigate(eq(Route.EXPLORE), any<NavOptionsBuilder.() -> Unit>())

        navigationAction.navigateTo(Screen.ASSOCIATION_PROFILE)
        verify(navHostController).navigate(Screen.ASSOCIATION_PROFILE)
    }

    @Test
    fun testScreenWithSingleParam() {
        val screen = "association/{uid}"
        val uid = "2024"
        val result = Screen.withParams(screen, uid)
        val expected = screen.replace("{uid}", uid)
        assertEquals(expected, result)
    }

    @Test
    fun testScreenWithMultipleParams() {
        val screen = "association/{uid}/{eid}"
        val uid = "2024"
        val eid = "2025"
        val result = Screen.withParams(screen, uid, eid)
        val expected = screen.replace("{uid}", uid).replace("{eid}", eid)
        assertEquals(expected, result)
    }
}