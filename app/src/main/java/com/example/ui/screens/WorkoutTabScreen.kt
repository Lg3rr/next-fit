package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.graphics.drawable.BitmapDrawable
import com.example.R
import com.example.data.CustomRoutineEntity
import com.example.data.ExerciseEntity
import com.example.data.WorkoutSessionEntity
import com.example.ui.components.CreateRoutineDialog

@Composable
fun WorkoutTabScreen(
    activeSession: WorkoutSessionEntity?,
    completedSessionsCount: Int,
    totalVolumeKg: Double,
    weightUnit: String,
    customRoutines: List<CustomRoutineEntity>,
    isWelcomeBackTriggered: Boolean,
    daysSinceLastWorkout: Int?,
    isWorkoutLoggedToday: Boolean = false,
    allExercises: List<ExerciseEntity>,
    onStartWorkout: (String) -> Unit,
    onResumeWorkout: () -> Unit,
    onStartRoutine: (String, List<String>) -> Unit,
    onStartRampUpRoutine: (String, List<String>) -> Unit,
    onDismissWelcomeBack: () -> Unit,
    onCreateCustomRoutine: (title: String, description: String, exercises: List<String>) -> Unit,
    onDeleteCustomRoutine: (CustomRoutineEntity) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartDialog by remember { mutableStateOf(false) }
    var showCreateRoutineDialog by remember { mutableStateOf(false) }
    var customWorkoutTitle by remember { mutableStateOf("") }

    val context = LocalContext.current
    val logoBitmap = remember(context) {
        runCatching {
            val drawable = ContextCompat.getDrawable(context, R.drawable.logo)
            if (drawable is BitmapDrawable) {
                drawable.bitmap.asImageBitmap()
            } else null
        }.getOrNull()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // --- Header Banner ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "NEXT FIT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "Ready to Train",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onOpenSettings,
                                modifier = Modifier.testTag("open_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                if (logoBitmap != null) {
                                    Image(
                                        bitmap = logoBitmap,
                                        contentDescription = "Next Fit Logo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = "Next Fit Logo",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("WORKOUTS", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "$completedSessionsCount",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Divider(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.outline
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOTAL VOLUME", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${totalVolumeKg.toInt()} $weightUnit",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // --- Today's Workout Reminder Card ---
        if (!isWorkoutLoggedToday && activeSession == null && !isWelcomeBackTriggered) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("today_reminder_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Haven't trained today yet?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Log a session today or pick a routine below!",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                customWorkoutTitle = "Daily Workout"
                                showStartDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Quick Log", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Inactivity Protocol Banner ("Welcome Back Flow") ---
        if (isWelcomeBackTriggered && daysSinceLastWorkout != null) {
            item {
                val weeks = maxOf(1, daysSinceLastWorkout / 7)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("welcome_back_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WavingHand,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "WELCOME BACK!",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    letterSpacing = 1.sp
                                )
                            }
                            IconButton(onClick = onDismissWelcomeBack) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "It's been $weeks ${if (weeks == 1) "week" else "weeks"} ($daysSinceLastWorkout days) since your last session. Let's get back into the rhythm smoothly!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Dynamic Ramp-Up Routine Suggested",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "70–80% target weights suggested for your comeback to avoid injury while preserving past PR stats.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                onStartRampUpRoutine(
                                    "Comeback Ramp-Up Full Body",
                                    listOf("Barbell Squat", "Barbell Bench Press", "Lat Pulldown", "Standard Push-Up")
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("start_ramp_up_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Comeback Session (Ramp-Up)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // --- Active Session Banner (if running) ---
        if (activeSession != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onResumeWorkout() }
                        .testTag("active_session_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ACTIVE WORKOUT IN PROGRESS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeSession.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = onResumeWorkout,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = androidx.compose.ui.graphics.Color.White
                            ),
                            modifier = Modifier.testTag("resume_workout_button")
                        ) {
                            Text("Resume", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Quick Start Empty Workout ---
        item {
            Button(
                onClick = { showStartDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_quick_workout_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Empty Workout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        // --- Routine Templates Header ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WORKOUT ROUTINES",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                TextButton(
                    onClick = { showCreateRoutineDialog = true },
                    modifier = Modifier.testTag("create_routine_button")
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create Routine", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // --- Custom User Routines ---
        if (customRoutines.isNotEmpty()) {
            items(customRoutines) { customRoutine ->
                val exercisesList = customRoutine.exerciseNamesJson.split(",").map { it.trim() }.filter { it.isNotBlank() }
                CustomRoutineCard(
                    routine = customRoutine,
                    exerciseCount = exercisesList.size,
                    onStart = {
                        onStartRoutine(customRoutine.title, exercisesList)
                    },
                    onDelete = {
                        onDeleteCustomRoutine(customRoutine)
                    }
                )
            }
        }

        // Preset routine items
        val routines = listOf(
            RoutineTemplate(
                title = "Push Power Day",
                description = "Chest, Shoulders & Triceps focus",
                icon = Icons.Default.FitnessCenter,
                exercises = listOf("Barbell Bench Press", "Incline Dumbbell Press", "Overhead Barbell Press", "Triceps Rope Pushdown")
            ),
            RoutineTemplate(
                title = "Pull & Back Shred",
                description = "Lats, Upper Back & Biceps focus",
                icon = Icons.Default.FormatListBulleted,
                exercises = listOf("Pull-Ups", "Barbell Bent Over Row", "Lat Pulldown", "Barbell Bicep Curl")
            ),
            RoutineTemplate(
                title = "Leg Destroyer",
                description = "Quadriceps, Hamstrings & Calves",
                icon = Icons.Default.DirectionsRun,
                exercises = listOf("Barbell Squat", "Leg Press", "Romanian Deadlift", "Standing Calf Raise")
            ),
            RoutineTemplate(
                title = "Full Body Strength",
                description = "All major compound movements",
                icon = Icons.Default.Bolt,
                exercises = listOf("Barbell Squat", "Barbell Bench Press", "Barbell Deadlift", "Overhead Barbell Press")
            )
        )

        items(routines) { routine ->
            RoutineCard(
                routine = routine,
                onStart = {
                    onStartRoutine(routine.title, routine.exercises)
                }
            )
        }
    }

    // --- Start Workout Title Dialog ---
    if (showStartDialog) {
        AlertDialog(
            onDismissRequest = { showStartDialog = false },
            title = { Text("Start Workout", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = customWorkoutTitle,
                    onValueChange = { customWorkoutTitle = it },
                    label = { Text("Workout Name (optional)") },
                    placeholder = { Text("e.g. Chest & Arms") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workout_title_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStartDialog = false
                        val title = customWorkoutTitle.ifBlank { "Workout Session" }
                        customWorkoutTitle = ""
                        onStartWorkout(title)
                    },
                    modifier = Modifier.testTag("confirm_start_workout_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = androidx.compose.ui.graphics.Color.White)
                ) {
                    Text("Start Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // --- Create Routine Dialog Launcher ---
    if (showCreateRoutineDialog) {
        CreateRoutineDialog(
            allExercises = allExercises,
            onCreateRoutine = { title, desc, exercises ->
                onCreateCustomRoutine(title, desc, exercises)
                showCreateRoutineDialog = false
            },
            onDismiss = { showCreateRoutineDialog = false }
        )
    }
}

data class RoutineTemplate(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val exercises: List<String>
)

@Composable
fun RoutineCard(
    routine: RoutineTemplate,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("routine_card_${routine.title}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = routine.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = routine.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = routine.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("start_routine_${routine.title}")
            ) {
                Text("Start", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun CustomRoutineCard(
    routine: CustomRoutineEntity,
    exerciseCount: Int,
    onStart: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_routine_card_${routine.title}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                "MY ROUTINE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = routine.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = if (routine.description.isNotBlank()) "${routine.description} • $exerciseCount exercises" else "$exerciseCount exercises",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Routine",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("start_custom_routine_${routine.title}")
                ) {
                    Text("Start", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

