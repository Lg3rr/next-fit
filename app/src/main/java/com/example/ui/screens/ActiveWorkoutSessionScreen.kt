package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExerciseEntity
import com.example.data.WorkoutSessionEntity
import com.example.data.WorkoutSetEntity
import com.example.ui.theme.*

@Composable
fun ActiveWorkoutSessionScreen(
    session: WorkoutSessionEntity,
    sets: List<WorkoutSetEntity>,
    weightUnit: String,
    allExercises: List<ExerciseEntity>,
    onToggleSetCompleted: (WorkoutSetEntity, Int) -> Unit,
    onUpdateSetValues: (WorkoutSetEntity, Double, Int, Double) -> Unit,
    onAddSetToExercise: (Long, String, String, Int) -> Unit,
    onAddExerciseToWorkout: (ExerciseEntity) -> Unit,
    onDeleteSet: (WorkoutSetEntity) -> Unit,
    onFinishWorkout: (String) -> Unit,
    onDiscardWorkout: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFinishDialog by remember { mutableStateOf(false) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var workoutNotes by remember { mutableStateOf("") }

    // Group sets by exercise
    val setsByExercise = remember(sets) {
        sets.groupBy { it.exerciseId to it.exerciseName }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NextDarkBackground)
    ) {
        // --- Active Session Top Bar ---
        Surface(
            color = NextDarkSurface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = NextTextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = session.title.uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NextEmeraldPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${sets.count { it.isCompleted }} sets done",
                        fontSize = 12.sp,
                        color = NextTextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDiscardWorkout,
                        modifier = Modifier.testTag("discard_workout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Discard Workout",
                            tint = NextErrorRed
                        )
                    }

                    Button(
                        onClick = { showFinishDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NextEmeraldPrimary,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        ),
                        modifier = Modifier.testTag("finish_workout_button")
                    ) {
                        Text("Finish", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Exercise & Sets List ---
        if (setsByExercise.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = NextTextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No exercises added yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NextTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap button below to select exercises",
                        fontSize = 13.sp,
                        color = NextTextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
            ) {
                items(setsByExercise.entries.toList()) { entry ->
                    val (exId, exName) = entry.key
                    val exerciseSets = entry.value
                    val exerciseEntity = allExercises.find { it.id == exId }
                    val muscleGroup = exerciseSets.firstOrNull()?.muscleGroup ?: "Other"
                    val defaultRest = exerciseEntity?.defaultRestSeconds ?: 90

                    ExerciseSetGroupCard(
                        exerciseName = exName,
                        muscleGroup = muscleGroup,
                        defaultRestSeconds = defaultRest,
                        sets = exerciseSets,
                        weightUnit = weightUnit,
                        onAddSet = {
                            onAddSetToExercise(exId, exName, muscleGroup, defaultRest)
                        },
                        onToggleSetCompleted = { set ->
                            onToggleSetCompleted(set, defaultRest)
                        },
                        onUpdateSetValues = onUpdateSetValues,
                        onDeleteSet = onDeleteSet
                    )
                }
            }
        }

        // --- Bottom Add Exercise Action ---
        Surface(
            color = NextDarkSurface,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = { showAddExerciseDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("add_exercise_to_workout_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NextEmeraldPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Exercise to Workout", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // --- Add Exercise Dialog Selector ---
    if (showAddExerciseDialog) {
        AddExerciseSelectorDialog(
            exercises = allExercises,
            onExerciseSelected = { ex ->
                showAddExerciseDialog = false
                onAddExerciseToWorkout(ex)
            },
            onDismiss = { showAddExerciseDialog = false }
        )
    }

    // --- Finish Workout Dialog ---
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Complete Workout", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Great job! Total completed sets: ${sets.count { it.isCompleted }}",
                        fontSize = 14.sp,
                        color = NextTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = workoutNotes,
                        onValueChange = { workoutNotes = it },
                        label = { Text("Session Notes (optional)") },
                        placeholder = { Text("Feeling strong, new PRs!") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("workout_notes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishDialog = false
                        onFinishWorkout(workoutNotes)
                    },
                    modifier = Modifier.testTag("confirm_finish_workout_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NextEmeraldPrimary, contentColor = androidx.compose.ui.graphics.Color.White)
                ) {
                    Text("Save & Finish", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Cancel", color = NextTextSecondary)
                }
            },
            containerColor = NextDarkSurface
        )
    }
}

@Composable
fun ExerciseSetGroupCard(
    exerciseName: String,
    muscleGroup: String,
    defaultRestSeconds: Int,
    sets: List<WorkoutSetEntity>,
    weightUnit: String,
    onAddSet: () -> Unit,
    onToggleSetCompleted: (WorkoutSetEntity) -> Unit,
    onUpdateSetValues: (WorkoutSetEntity, Double, Int, Double) -> Unit,
    onDeleteSet: (WorkoutSetEntity) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NextDarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exercise_group_$exerciseName")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = exerciseName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NextTextPrimary
                    )
                    Text(
                        text = "$muscleGroup • ${defaultRestSeconds}s rest",
                        fontSize = 12.sp,
                        color = NextEmeraldPrimary
                    )
                }

                TextButton(
                    onClick = onAddSet,
                    modifier = Modifier.testTag("add_set_$exerciseName")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = NextEmeraldPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Set", color = NextEmeraldPrimary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Table Column Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SET", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NextTextMuted, modifier = Modifier.weight(0.8f))
                Text("$weightUnit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NextTextMuted, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                Text("REPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NextTextMuted, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                Text("DONE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NextTextMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(28.dp))
            }

            Divider(color = NextDarkOutline, modifier = Modifier.padding(vertical = 4.dp))

            // Set Rows
            sets.forEach { setItem ->
                WorkoutSetRow(
                    set = setItem,
                    onToggleCompleted = { onToggleSetCompleted(setItem) },
                    onUpdateValues = { weight, reps, rpe ->
                        onUpdateSetValues(setItem, weight, reps, rpe)
                    },
                    onDelete = { onDeleteSet(setItem) }
                )
            }
        }
    }
}

@Composable
fun WorkoutSetRow(
    set: WorkoutSetEntity,
    onToggleCompleted: () -> Unit,
    onUpdateValues: (Double, Int, Double) -> Unit,
    onDelete: () -> Unit
) {
    var weightText by remember(set.weightKg) { mutableStateOf(if (set.weightKg % 1.0 == 0.0) set.weightKg.toInt().toString() else set.weightKg.toString()) }
    var repsText by remember(set.reps) { mutableStateOf(set.reps.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Set number
        Text(
            text = "${set.setNumber}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (set.isCompleted) NextEmeraldPrimary else NextTextPrimary,
            modifier = Modifier.weight(0.8f)
        )

        // Weight Input
        OutlinedTextField(
            value = weightText,
            onValueChange = { input ->
                weightText = input
                val newW = input.toDoubleOrNull() ?: set.weightKg
                val newR = repsText.toIntOrNull() ?: set.reps
                onUpdateValues(newW, newR, set.rpe)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .weight(1.5f)
                .height(48.dp)
                .padding(horizontal = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NextEmeraldPrimary,
                unfocusedBorderColor = NextDarkOutline
            )
        )

        // Reps Input
        OutlinedTextField(
            value = repsText,
            onValueChange = { input ->
                repsText = input
                val newW = weightText.toDoubleOrNull() ?: set.weightKg
                val newR = input.toIntOrNull() ?: set.reps
                onUpdateValues(newW, newR, set.rpe)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .weight(1.5f)
                .height(48.dp)
                .padding(horizontal = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NextEmeraldPrimary,
                unfocusedBorderColor = NextDarkOutline
            )
        )

        // Completed Checkbox
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onToggleCompleted,
                modifier = Modifier.testTag("set_checkbox_${set.exerciseName}_${set.setNumber}")
            ) {
                Icon(
                    imageVector = if (set.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Complete set",
                    tint = if (set.isCompleted) NextEmeraldPrimary else NextTextMuted,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Delete set button
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete set",
                tint = NextTextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun AddExerciseSelectorDialog(
    exercises: List<ExerciseEntity>,
    onExerciseSelected: (ExerciseEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = exercises.filter { it.name.contains(search, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Exercise", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.heightIn(max = 350.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Search exercise...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered) { ex ->
                        Card(
                            onClick = { onExerciseSelected(ex) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = NextDarkSurfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(ex.name, fontWeight = FontWeight.Bold, color = NextTextPrimary)
                                    Text(ex.muscleGroup, fontSize = 12.sp, color = NextEmeraldPrimary)
                                }
                                Icon(Icons.Default.Add, contentDescription = null, tint = NextEmeraldPrimary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NextTextSecondary)
            }
        },
        containerColor = NextDarkSurface
    )
}
