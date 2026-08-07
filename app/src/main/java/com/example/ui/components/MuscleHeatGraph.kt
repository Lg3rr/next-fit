package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WorkoutSessionEntity

@Composable
fun MuscleHeatGraph(
    completedSessions: List<WorkoutSessionEntity>,
    weightUnit: String,
    modifier: Modifier = Modifier
) {
    val muscleGroups = listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Core")

    // Calculate volume or session frequency per muscle group
    val muscleVolumeMap = mutableMapOf<String, Double>()
    muscleGroups.forEach { muscleVolumeMap[it] = 0.0 }

    // Aggregate total volume per muscle group across completed sessions
    completedSessions.forEach { session ->
        val group = when {
            session.title.contains("Chest", ignoreCase = true) || session.title.contains("Push", ignoreCase = true) -> "Chest"
            session.title.contains("Back", ignoreCase = true) || session.title.contains("Pull", ignoreCase = true) -> "Back"
            session.title.contains("Leg", ignoreCase = true) || session.title.contains("Squat", ignoreCase = true) -> "Legs"
            session.title.contains("Shoulder", ignoreCase = true) -> "Shoulders"
            session.title.contains("Arm", ignoreCase = true) || session.title.contains("Bicep", ignoreCase = true) -> "Arms"
            session.title.contains("Core", ignoreCase = true) || session.title.contains("Abs", ignoreCase = true) -> "Core"
            else -> "Chest"
        }
        muscleVolumeMap[group] = (muscleVolumeMap[group] ?: 0.0) + session.totalVolumeKg
    }

    val maxVolume = (muscleVolumeMap.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    val totalAllVolume = (muscleVolumeMap.values.sum()).coerceAtLeast(1.0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("muscle_heat_graph_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MUSCLE GROUP HEAT GRAPH",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Training intensity & volume breakdown",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${completedSessions.size} Sessions",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Muscle Heat Bars
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                muscleGroups.forEach { muscle ->
                    val volume = muscleVolumeMap[muscle] ?: 0.0
                    val ratio = (volume / maxVolume).toFloat().coerceIn(0.05f, 1f)
                    val percentage = ((volume / totalAllVolume) * 100).toInt()

                    val heatColor = when {
                        ratio >= 0.75f -> Color(0xFFFF5722) // High Heat - Burning Orange
                        ratio >= 0.45f -> Color(0xFFFFB300) // Medium Heat - Amber Gold
                        ratio >= 0.20f -> MaterialTheme.colorScheme.primary // Moderate Heat - Cyan/Green
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) // Low Heat
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(heatColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = muscle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${volume.toInt()} $weightUnit ($percentage%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(ratio)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(heatColor)
                            )
                        }
                    }
                }
            }
        }
    }
}
