package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExerciseEntity
import com.example.data.PersonalRecordEntity
import com.example.ui.theme.*

@Composable
fun ExerciseLibraryScreen(
    exercises: List<ExerciseEntity>,
    personalRecords: List<PersonalRecordEntity>,
    selectedMuscleGroup: String,
    searchQuery: String,
    weightUnit: String,
    isWorkoutActive: Boolean,
    onMuscleGroupSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onAddExerciseToActiveWorkout: (ExerciseEntity) -> Unit,
    onCreateCustomExercise: (String, String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    val muscleGroups = listOf("All", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core")

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title & Search
            Text(
                text = "EXERCISE LIBRARY",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = NextTextPrimary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Search exercise (e.g., Bench Press)") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NextTextMuted)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", tint = NextTextMuted)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exercise_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NextEmeraldPrimary,
                    unfocusedBorderColor = NextDarkOutline
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Muscle Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(muscleGroups) { group ->
                    val isSelected = group.equals(selectedMuscleGroup, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onMuscleGroupSelected(group) },
                        label = {
                            Text(
                                text = group,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NextEmeraldPrimary,
                            selectedLabelColor = NextDarkSurface,
                            containerColor = NextDarkSurface,
                            labelColor = NextTextSecondary
                        ),
                        modifier = Modifier.testTag("muscle_chip_$group")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Exercise Cards List
            if (exercises.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No exercises found",
                        color = NextTextMuted,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(exercises) { ex ->
                        val pr = personalRecords.find { it.exerciseId == ex.id }
                        ExerciseCardItem(
                            exercise = ex,
                            personalRecord = pr,
                            weightUnit = weightUnit,
                            isWorkoutActive = isWorkoutActive,
                            onAddToWorkout = { onAddExerciseToActiveWorkout(ex) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to create custom exercise
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = NextEmeraldPrimary,
            contentColor = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .testTag("create_custom_exercise_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Create Custom Exercise")
        }
    }

    // Custom Exercise Dialog
    if (showCreateDialog) {
        CreateCustomExerciseDialog(
            muscleGroups = muscleGroups.filter { it != "All" },
            onConfirm = { name, muscle, restSec ->
                showCreateDialog = false
                onCreateCustomExercise(name, muscle, restSec)
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
fun ExerciseCardItem(
    exercise: ExerciseEntity,
    personalRecord: PersonalRecordEntity?,
    weightUnit: String,
    isWorkoutActive: Boolean,
    onAddToWorkout: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NextDarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exercise_item_${exercise.name}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = NextTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = NextEmeraldPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = exercise.muscleGroup.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NextEmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${exercise.defaultRestSeconds}s rest",
                            fontSize = 12.sp,
                            color = NextTextSecondary
                        )
                    }
                }

                if (isWorkoutActive) {
                    IconButton(
                        onClick = onAddToWorkout,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(NextEmeraldPrimary)
                            .size(36.dp)
                            .testTag("add_to_active_${exercise.name}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add to workout",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // PR Highlight
            if (personalRecord != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = NextGoldAccent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = NextGoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PR: ${personalRecord.maxWeightKg} $weightUnit x ${personalRecord.maxReps} reps",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NextGoldAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateCustomExerciseDialog(
    muscleGroups: List<String>,
    onConfirm: (String, String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf(muscleGroups.firstOrNull() ?: "Chest") }
    var restSeconds by remember { mutableStateOf("90") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Exercise", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    placeholder = { Text("e.g. Cable Lateral Raise") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_exercise_name_input")
                )

                Text("Muscle Group:", fontSize = 12.sp, color = NextTextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(muscleGroups) { group ->
                        FilterChip(
                            selected = group == selectedMuscle,
                            onClick = { selectedMuscle = group },
                            label = { Text(group) }
                        )
                    }
                }

                OutlinedTextField(
                    value = restSeconds,
                    onValueChange = { restSeconds = it },
                    label = { Text("Default Rest Timer (seconds)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val restInt = restSeconds.toIntOrNull() ?: 90
                        onConfirm(name, selectedMuscle, restInt)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NextEmeraldPrimary, contentColor = androidx.compose.ui.graphics.Color.White),
                modifier = Modifier.testTag("confirm_create_custom_exercise_button")
            ) {
                Text("Create", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NextTextSecondary)
            }
        },
        containerColor = NextDarkSurface
    )
}
