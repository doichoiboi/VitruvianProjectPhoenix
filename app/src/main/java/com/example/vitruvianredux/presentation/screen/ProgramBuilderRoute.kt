package com.example.vitruvianredux.presentation.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.example.vitruvianredux.data.local.ProgramDayEntity
import com.example.vitruvianredux.data.local.WeeklyProgramEntity
import com.example.vitruvianredux.data.local.WeeklyProgramWithDays
import com.example.vitruvianredux.domain.model.Routine
import com.example.vitruvianredux.presentation.chrome.LocalAppChrome
import com.example.vitruvianredux.presentation.chrome.TopBarAction
import com.example.vitruvianredux.presentation.viewmodel.MainViewModel
import com.example.vitruvianredux.ui.theme.ThemeMode
import java.time.DayOfWeek
import java.util.UUID

@Composable
fun ProgramBuilderRoute(
    navController: NavController,
    viewModel: MainViewModel,
    programId: String,
    themeMode: ThemeMode
) {
    val routines by viewModel.routines.collectAsState()
    val programs by viewModel.weeklyPrograms.collectAsState()
    val appChrome = LocalAppChrome.current
    val chromeOwner = remember(programId) { "ProgramBuilderScreen:$programId" }

    var programName by remember { mutableStateOf("New Program") }
    var dailyRoutines by remember {
        mutableStateOf<Map<DayOfWeek, Routine?>>(
            DayOfWeek.entries.associateWith { null }
        )
    }

    LaunchedEffect(programId, programs, routines) {
        if (programId != "new") {
            val existingProgram = programs.find { it.program.id == programId }
            existingProgram?.let { program ->
                programName = program.program.title

                val routineMap = DayOfWeek.entries.associateWith { day ->
                    val programDay = program.days.find { it.dayOfWeek == day.value }
                    programDay?.let { dayEntity ->
                        routines.find { it.id == dayEntity.routineId }
                    }
                }

                dailyRoutines = routineMap
            }
        }
    }

    LaunchedEffect(appChrome, chromeOwner, programId) {
        appChrome.setDynamicTitle(chromeOwner, if (programId == "new") "New Program" else "Edit Program")
    }

    LaunchedEffect(appChrome, chromeOwner, programId, programName, dailyRoutines) {
        appChrome.setTopBarActions(
            chromeOwner,
            listOf(
                TopBarAction(
                    icon = Icons.Default.Done,
                    description = "Save Program",
                    onClick = {
                        val programEntity = WeeklyProgramEntity(
                            id = if (programId == "new") UUID.randomUUID().toString() else programId,
                            title = programName,
                            notes = null,
                            isActive = false,
                            createdAt = System.currentTimeMillis()
                        )

                        val programDays = dailyRoutines.entries
                            .filter { (_, routine) -> routine != null }
                            .map { (day, routine) ->
                                ProgramDayEntity(
                                    programId = programEntity.id,
                                    dayOfWeek = day.value,
                                    routineId = routine!!.id
                                )
                            }

                        viewModel.saveProgram(
                            WeeklyProgramWithDays(
                                program = programEntity,
                                days = programDays
                            )
                        )
                        navController.navigateUp()
                    }
                )
            )
        )
    }

    DisposableEffect(appChrome, chromeOwner) {
        onDispose {
            appChrome.clearChrome(chromeOwner)
        }
    }

    ProgramBuilderScreen(
        programName = programName,
        onProgramNameChange = { programName = it },
        routines = routines,
        dailyRoutines = dailyRoutines,
        onDailyRoutinesChange = { dailyRoutines = it },
        themeMode = themeMode
    )
}
