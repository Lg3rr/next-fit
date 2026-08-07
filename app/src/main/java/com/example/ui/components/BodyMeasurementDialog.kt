package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMeasurementDialog(
    weightUnit: String,
    onSaveMeasurement: (weightKg: Double, chestCm: Double, bicepsCm: Double, waistCm: Double, thighsCm: Double, shouldersCm: Double, bodyFatPercentage: Double, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var weightInput by remember { mutableStateOf("") }
    var chestInput by remember { mutableStateOf("") }
    var bicepsInput by remember { mutableStateOf("") }
    var waistInput by remember { mutableStateOf("") }
    var thighsInput by remember { mutableStateOf("") }
    var shouldersInput by remember { mutableStateOf("") }
    var bodyFatInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SquareFoot,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("LOG BODY & MUSCLE SIZE", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Track your body weight and muscle tape measurements to monitor physique changes.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Body Weight
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Body Weight ($weightUnit)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("measurement_input_weight")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Chest
                    OutlinedTextField(
                        value = chestInput,
                        onValueChange = { chestInput = it },
                        label = { Text("Chest (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("measurement_input_chest")
                    )

                    // Arms / Biceps
                    OutlinedTextField(
                        value = bicepsInput,
                        onValueChange = { bicepsInput = it },
                        label = { Text("Arms / Biceps (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("measurement_input_biceps")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Waist
                    OutlinedTextField(
                        value = waistInput,
                        onValueChange = { waistInput = it },
                        label = { Text("Waist (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("measurement_input_waist")
                    )

                    // Thighs
                    OutlinedTextField(
                        value = thighsInput,
                        onValueChange = { thighsInput = it },
                        label = { Text("Thighs (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("measurement_input_thighs")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Shoulders
                    OutlinedTextField(
                        value = shouldersInput,
                        onValueChange = { shouldersInput = it },
                        label = { Text("Shoulders (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("measurement_input_shoulders")
                    )

                    // Body Fat %
                    OutlinedTextField(
                        value = bodyFatInput,
                        onValueChange = { bodyFatInput = it },
                        label = { Text("Body Fat %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("measurement_input_bodyfat")
                    )
                }

                // Notes
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Notes (optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("measurement_input_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val weight = weightInput.toDoubleOrNull() ?: 0.0
                    val chest = chestInput.toDoubleOrNull() ?: 0.0
                    val biceps = bicepsInput.toDoubleOrNull() ?: 0.0
                    val waist = waistInput.toDoubleOrNull() ?: 0.0
                    val thighs = thighsInput.toDoubleOrNull() ?: 0.0
                    val shoulders = shouldersInput.toDoubleOrNull() ?: 0.0
                    val bodyFat = bodyFatInput.toDoubleOrNull() ?: 0.0

                    if (weight > 0.0 || chest > 0.0 || biceps > 0.0 || waist > 0.0) {
                        onSaveMeasurement(
                            weight, chest, biceps, waist, thighs, shoulders, bodyFat, notesInput
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_measurement_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Entry", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
