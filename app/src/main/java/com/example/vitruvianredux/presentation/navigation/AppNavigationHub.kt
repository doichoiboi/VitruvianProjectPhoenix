package com.example.vitruvianredux.presentation.navigation

sealed class AppDestination(
    val route: String,
    val title: String,
    val analyticsName: String,
    val isWorkoutSection: Boolean = false,
    val isBottomBarDestination: Boolean = false,
    val isTopLevelDestination: Boolean = false,
    val usesDynamicTitle: Boolean = false
) {
    open fun matches(route: String): Boolean = this.route == route

    data object Home : AppDestination(
        route = NavigationRoutes.Home.route,
        title = "Choose Your Workout",
        analyticsName = "home",
        isWorkoutSection = true,
        isBottomBarDestination = true,
        isTopLevelDestination = true
    )

    data object JustLift : AppDestination(
        route = NavigationRoutes.JustLift.route,
        title = "Just Lift",
        analyticsName = "just_lift",
        isWorkoutSection = true
    )

    data object SingleExercise : AppDestination(
        route = NavigationRoutes.SingleExercise.route,
        title = "Single Exercise",
        analyticsName = "single_exercise",
        isWorkoutSection = true
    )

    data object DailyRoutines : AppDestination(
        route = NavigationRoutes.DailyRoutines.route,
        title = "Daily Routines",
        analyticsName = "daily_routines",
        isWorkoutSection = true,
        isBottomBarDestination = true
    )

    data object ActiveWorkout : AppDestination(
        route = NavigationRoutes.ActiveWorkout.route,
        title = "Active Workout",
        analyticsName = "active_workout",
        isWorkoutSection = true,
        usesDynamicTitle = true
    )

    data object WeeklyPrograms : AppDestination(
        route = NavigationRoutes.WeeklyPrograms.route,
        title = "Weekly Programs",
        analyticsName = "weekly_programs",
        isWorkoutSection = true,
        isBottomBarDestination = true
    )

    data object ProgramBuilder : AppDestination(
        route = NavigationRoutes.ProgramBuilder.route,
        title = "Program Builder",
        analyticsName = "program_builder",
        isWorkoutSection = true,
        usesDynamicTitle = true
    ) {
        private val routePrefix = NavigationRoutes.ProgramBuilder.route.substringBefore("/{")

        override fun matches(route: String): Boolean =
            route == this.route || route.startsWith("$routePrefix/")
    }

    data object Analytics : AppDestination(
        route = NavigationRoutes.Analytics.route,
        title = "Analytics",
        analyticsName = "analytics",
        isBottomBarDestination = true,
        isTopLevelDestination = true
    )

    data object Settings : AppDestination(
        route = NavigationRoutes.Settings.route,
        title = "Settings",
        analyticsName = "settings",
        isBottomBarDestination = true,
        isTopLevelDestination = true
    )

    data object ConnectionLogs : AppDestination(
        route = NavigationRoutes.ConnectionLogs.route,
        title = "Connection Logs",
        analyticsName = "connection_logs"
    )

    data object ProtocolTester : AppDestination(
        route = NavigationRoutes.ProtocolTester.route,
        title = "Protocol Tester",
        analyticsName = "protocol_tester"
    )
}

object AppNavigationHub {
    private val destinations = listOf(
        AppDestination.Home,
        AppDestination.JustLift,
        AppDestination.SingleExercise,
        AppDestination.DailyRoutines,
        AppDestination.ActiveWorkout,
        AppDestination.WeeklyPrograms,
        AppDestination.ProgramBuilder,
        AppDestination.Analytics,
        AppDestination.Settings,
        AppDestination.ConnectionLogs,
        AppDestination.ProtocolTester
    )

    fun destinationFor(route: String?): AppDestination {
        val normalizedRoute = route ?: NavigationRoutes.Home.route
        return destinations.firstOrNull { it.matches(normalizedRoute) } ?: AppDestination.Home
    }

    fun appBarTitle(route: String?, dynamicTitle: String?): String {
        val destination = destinationFor(route)
        return if (destination.usesDynamicTitle && !dynamicTitle.isNullOrBlank()) {
            dynamicTitle
        } else {
            destination.title
        }
    }

    fun isWorkoutSection(route: String?): Boolean =
        destinationFor(route).isWorkoutSection

    fun isBottomBarDestination(route: String?): Boolean =
        destinationFor(route).isBottomBarDestination

    fun showsBackButton(route: String?): Boolean =
        !destinationFor(route).isTopLevelDestination

    fun analyticsName(route: String?): String =
        destinationFor(route).analyticsName

}
