package com.example.vitruvianredux.presentation.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AppNavigationHubTest {

    @Test
    fun `static routes use destination titles and ignore stale dynamic title`() {
        assertEquals(
            "Connection Logs",
            AppNavigationHub.appBarTitle(NavigationRoutes.ConnectionLogs.route, "Stale title")
        )
        assertEquals(
            "Choose Your Workout",
            AppNavigationHub.appBarTitle(NavigationRoutes.Home.route, "Just Lift")
        )
    }

    @Test
    fun `dynamic routes use provided title when available`() {
        assertEquals(
            "Upper Body A",
            AppNavigationHub.appBarTitle(NavigationRoutes.ActiveWorkout.route, "Upper Body A")
        )
        assertEquals(
            "Edit Program",
            AppNavigationHub.appBarTitle("program_builder/abc", "Edit Program")
        )
    }

    @Test
    fun `dynamic routes fall back to destination titles without override`() {
        assertEquals(
            "Active Workout",
            AppNavigationHub.appBarTitle(NavigationRoutes.ActiveWorkout.route, "")
        )
        assertEquals(
            "Program Builder",
            AppNavigationHub.appBarTitle("program_builder/new", null)
        )
    }

    @Test
    fun `workout section and bottom bar metadata match shell behavior`() {
        assertTrue(AppNavigationHub.isWorkoutSection(NavigationRoutes.JustLift.route))
        assertTrue(AppNavigationHub.isWorkoutSection("program_builder/new"))
        assertFalse(AppNavigationHub.isWorkoutSection(NavigationRoutes.Analytics.route))

        assertTrue(AppNavigationHub.isBottomBarDestination(NavigationRoutes.Home.route))
        assertTrue(AppNavigationHub.isBottomBarDestination(NavigationRoutes.DailyRoutines.route))
        assertTrue(AppNavigationHub.isBottomBarDestination(NavigationRoutes.WeeklyPrograms.route))
        assertFalse(AppNavigationHub.isBottomBarDestination(NavigationRoutes.JustLift.route))
    }

    @Test
    fun `top level destinations do not show the shell back button`() {
        assertFalse(AppNavigationHub.showsBackButton(NavigationRoutes.Home.route))
        assertFalse(AppNavigationHub.showsBackButton(NavigationRoutes.Analytics.route))
        assertFalse(AppNavigationHub.showsBackButton(NavigationRoutes.Settings.route))
        assertTrue(AppNavigationHub.showsBackButton(NavigationRoutes.ConnectionLogs.route))
    }

    @Test
    fun `analytics route names come from destination metadata`() {
        assertEquals("analytics", AppNavigationHub.analyticsName(NavigationRoutes.Analytics.route))
        assertEquals("program_builder", AppNavigationHub.analyticsName("program_builder/new"))
        assertEquals("home", AppNavigationHub.analyticsName("unknown"))
    }

    @Test
    fun `route lookup returns sealed destination objects`() {
        assertSame(AppDestination.Home, AppNavigationHub.destinationFor(NavigationRoutes.Home.route))
        assertSame(AppDestination.ProgramBuilder, AppNavigationHub.destinationFor("program_builder/abc"))
        assertSame(AppDestination.Home, AppNavigationHub.destinationFor(null))
    }
}
