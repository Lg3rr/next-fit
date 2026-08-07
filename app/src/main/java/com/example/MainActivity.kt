package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.WorkoutViewModel
import com.example.ui.components.AppSettingsDialog
import com.example.ui.components.BottomTab
import com.example.ui.components.NextBottomNavigationBar
import com.example.ui.components.PrCelebrationDialog
import com.example.ui.components.RestTimerBar
import com.example.ui.screens.ActiveWorkoutSessionScreen
import com.example.ui.screens.ExerciseLibraryScreen
import com.example.ui.screens.HistoryAnalyticsScreen
import com.example.ui.screens.WorkoutTabScreen
import com.example.ui.theme.NextFitnessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WorkoutViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            NextFitnessTheme(themeMode = themeMode) {
                NextFitnessApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun NextFitnessApp(
    viewModel: WorkoutViewModel = viewModel()
) {
    var currentTab by remember { mutableStateOf(BottomTab.WORKOUT) }
    var isViewingActiveSessionScreen by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // State Collection
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val activeSets by viewModel.activeSets.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val completedSessions by viewModel.completedSessions.collectAsStateWithLifecycle()
    val personalRecords by viewModel.personalRecords.collectAsStateWithLifecycle()
    val bodyMeasurements by viewModel.allBodyMeasurements.collectAsStateWithLifecycle()
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle()

    val customRoutines by viewModel.customRoutines.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val inactivityThreshold by viewModel.inactivityThresholdDays.collectAsStateWithLifecycle()
    val isWelcomeBackTriggered by viewModel.isWelcomeBackTriggered.collectAsStateWithLifecycle()
    val daysSinceLastWorkout by viewModel.daysSinceLastWorkout.collectAsStateWithLifecycle()

    val dailyReminderEnabled by viewModel.dailyReminderEnabled.collectAsStateWithLifecycle()
    val isWorkoutLoggedToday by viewModel.isWorkoutLoggedToday.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.setDailyReminderEnabled(true, context)
            }
        }
    )

    val selectedMuscleGroup by viewModel.selectedMuscleGroup.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    // Timer State
    val restSeconds by viewModel.restTimerSeconds.collectAsStateWithLifecycle()
    val initialRestSec by viewModel.initialRestSeconds.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()

    // PR Event
    val latestPR by viewModel.latestPREvent.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isViewingActiveSessionScreen) {
                NextBottomNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        currentTab = tab
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Content Area
            if (isViewingActiveSessionScreen && activeSession != null) {
                ActiveWorkoutSessionScreen(
                    session = activeSession!!,
                    sets = activeSets,
                    weightUnit = weightUnit,
                    allExercises = exercises,
                    onToggleSetCompleted = { setItem, defaultRest ->
                        viewModel.toggleSetCompleted(setItem, defaultRest)
                    },
                    onUpdateSetValues = { setItem, w, r, rpe ->
                        viewModel.updateSetValues(setItem, w, r, rpe)
                    },
                    onAddSetToExercise = { exId, exName, muscle, restSec ->
                        viewModel.addSetToExerciseInSession(exId, exName, muscle, restSec)
                    },
                    onAddExerciseToWorkout = { ex ->
                        viewModel.addExerciseToActiveWorkout(ex)
                    },
                    onDeleteSet = { setItem ->
                        viewModel.deleteSet(setItem)
                    },
                    onFinishWorkout = { notes ->
                        viewModel.finishCurrentWorkout(notes)
                        isViewingActiveSessionScreen = false
                    },
                    onDiscardWorkout = {
                        viewModel.discardCurrentWorkout()
                        isViewingActiveSessionScreen = false
                    },
                    onBackClick = {
                        isViewingActiveSessionScreen = false
                    }
                )
            } else {
                AnimatedContent(
                    targetState = currentTab,
                    label = "tab_transition"
                ) { targetTab ->
                    when (targetTab) {
                        BottomTab.WORKOUT -> {
                            WorkoutTabScreen(
                                activeSession = activeSession,
                                completedSessionsCount = completedSessions.size,
                                totalVolumeKg = completedSessions.fold(0.0) { acc, s -> acc + s.totalVolumeKg },
                                weightUnit = weightUnit,
                                customRoutines = customRoutines,
                                isWelcomeBackTriggered = isWelcomeBackTriggered,
                                daysSinceLastWorkout = daysSinceLastWorkout,
                                isWorkoutLoggedToday = isWorkoutLoggedToday,
                                allExercises = exercises,
                                onStartWorkout = { title ->
                                    viewModel.startWorkout(title)
                                    isViewingActiveSessionScreen = true
                                },
                                onResumeWorkout = {
                                    isViewingActiveSessionScreen = true
                                },
                                onStartRoutine = { routineTitle, exerciseNames ->
                                    viewModel.startWorkout(routineTitle)
                                    exerciseNames.forEach { exName ->
                                        exercises.find { it.name.equals(exName, ignoreCase = true) }?.let { ex ->
                                            viewModel.addExerciseToActiveWorkout(ex)
                                        }
                                    }
                                    isViewingActiveSessionScreen = true
                                },
                                onStartRampUpRoutine = { routineTitle, exerciseNames ->
                                    viewModel.startWorkout(routineTitle, phase = "Comeback", isRampUp = true)
                                    exerciseNames.forEach { exName ->
                                        exercises.find { it.name.equals(exName, ignoreCase = true) }?.let { ex ->
                                            viewModel.addExerciseToActiveWorkout(ex)
                                        }
                                    }
                                    viewModel.dismissWelcomeBack()
                                    isViewingActiveSessionScreen = true
                                },
                                onDismissWelcomeBack = {
                                    viewModel.dismissWelcomeBack()
                                },
                                onCreateCustomRoutine = { title, desc, selectedExercises ->
                                    viewModel.createCustomRoutine(title, desc, selectedExercises)
                                },
                                onDeleteCustomRoutine = { routine ->
                                    viewModel.deleteCustomRoutine(routine)
                                },
                                onOpenSettings = {
                                    showSettingsDialog = true
                                }
                            )
                        }

                        BottomTab.EXERCISES -> {
                            ExerciseLibraryScreen(
                                exercises = exercises,
                                personalRecords = personalRecords,
                                selectedMuscleGroup = selectedMuscleGroup,
                                searchQuery = searchQuery,
                                weightUnit = weightUnit,
                                isWorkoutActive = activeSession != null,
                                onMuscleGroupSelected = { group ->
                                    viewModel.setMuscleFilter(group)
                                },
                                onSearchQueryChanged = { query ->
                                    viewModel.setSearchQuery(query)
                                },
                                onAddExerciseToActiveWorkout = { ex ->
                                    viewModel.addExerciseToActiveWorkout(ex)
                                    isViewingActiveSessionScreen = true
                                },
                                onCreateCustomExercise = { name, muscle, restSec ->
                                    viewModel.addCustomExercise(name, muscle, restSec)
                                }
                            )
                        }

                        BottomTab.ANALYTICS -> {
                            HistoryAnalyticsScreen(
                                completedSessions = completedSessions,
                                personalRecords = personalRecords,
                                bodyMeasurements = bodyMeasurements,
                                weightUnit = weightUnit,
                                onToggleWeightUnit = {
                                    viewModel.toggleWeightUnit()
                                },
                                onDeleteCompletedSession = { session ->
                                    viewModel.deleteCompletedWorkout(session)
                                },
                                onSaveBodyMeasurement = { weight, chest, biceps, waist, thighs, shoulders, bodyFat, notes ->
                                    viewModel.addBodyMeasurement(
                                        weightKg = weight,
                                        chestCm = chest,
                                        bicepsCm = biceps,
                                        waistCm = waist,
                                        thighsCm = thighs,
                                        shouldersCm = shoulders,
                                        bodyFatPercentage = bodyFat,
                                        notes = notes
                                    )
                                },
                                onDeleteBodyMeasurement = { measurement ->
                                    viewModel.deleteBodyMeasurement(measurement)
                                },
                                onExportBackup = {
                                    viewModel.exportBackupJson()
                                },
                                onImportBackup = { json ->
                                    viewModel.importBackupJson(json)
                                }
                            )
                        }
                    }
                }
            }

            // Global Floating Rest Timer Bar across all tabs!
            RestTimerBar(
                remainingSeconds = restSeconds,
                initialSeconds = initialRestSec,
                isRunning = isTimerRunning,
                onPauseResume = {
                    if (isTimerRunning) viewModel.pauseRestTimer() else viewModel.resumeRestTimer()
                },
                onAddSeconds = { sec ->
                    viewModel.adjustTimerSeconds(sec)
                },
                onClose = {
                    viewModel.stopRestTimer()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isViewingActiveSessionScreen) 16.dp else 70.dp)
            )

            // PR Celebration Dialog overlay
            latestPR?.let { pr ->
                PrCelebrationDialog(
                    prRecord = pr,
                    weightUnit = weightUnit,
                    onDismiss = {
                        viewModel.dismissPREvent()
                    }
                )
            }

            // Settings Preferences Dialog
            if (showSettingsDialog) {
                AppSettingsDialog(
                    currentThemeMode = themeMode,
                    currentWeightUnit = weightUnit,
                    currentInactivityThreshold = inactivityThreshold,
                    isDailyReminderEnabled = dailyReminderEnabled,
                    onThemeModeSelected = { mode ->
                        viewModel.setThemeMode(mode)
                    },
                    onWeightUnitToggled = {
                        viewModel.toggleWeightUnit()
                    },
                    onInactivityThresholdSelected = { days ->
                        viewModel.setInactivityThreshold(days)
                    },
                    onDailyReminderToggled = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.setDailyReminderEnabled(enabled, context)
                        }
                    },
                    onTriggerTestNotification = {
                        viewModel.triggerTestNotification(context)
                    },
                    onExportBackup = {
                        viewModel.exportBackupJson()
                    },
                    onImportBackup = { json ->
                        viewModel.importBackupJson(json)
                    },
                    onDismiss = {
                        showSettingsDialog = false
                    }
                )
            }
        }
    }
}

