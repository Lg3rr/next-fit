package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BodyMeasurementEntity
import com.example.data.PersonalRecordEntity
import com.example.data.WorkoutSessionEntity
import com.example.ui.components.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryAnalyticsScreen(
    completedSessions: List<WorkoutSessionEntity>,
    personalRecords: List<PersonalRecordEntity>,
    bodyMeasurements: List<BodyMeasurementEntity>,
    weightUnit: String,
    onToggleWeightUnit: () -> Unit,
    onDeleteCompletedSession: (WorkoutSessionEntity) -> Unit,
    onSaveBodyMeasurement: (weightKg: Double, chestCm: Double, bicepsCm: Double, waistCm: Double, thighsCm: Double, shouldersCm: Double, bodyFatPercentage: Double, notes: String) -> Unit,
    onDeleteBodyMeasurement: (BodyMeasurementEntity) -> Unit,
    onExportBackup: () -> String = { "" },
    onImportBackup: suspend (String) -> Boolean = { false },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var selectedSubTab by remember { mutableStateOf("History") } // "History", "Muscle & Weight", "PR Trophies", "Data Export"
    var showBackupDialog by remember { mutableStateOf(false) }
    var showMonthlyReportDialog by remember { mutableStateOf(false) }
    var showAddMeasurementDialog by remember { mutableStateOf(false) }
    var selectedCalendarDateMillis by remember { mutableStateOf<Long?>(null) }

    var importJsonInput by remember { mutableStateOf("") }
    var isImportingData by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("EEE, MMM d, yyyy • h:mm a", Locale.getDefault()) }

    // Check if today is 1st day of the month
    val isFirstDayOfMonth = remember {
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1
    }

    val totalVolume = remember(completedSessions) {
        completedSessions.fold(0.0) { acc, s -> acc + s.totalVolumeKg }
    }

    // Filter sessions by calendar date if selected
    val displaySessions = remember(completedSessions, selectedCalendarDateMillis) {
        if (selectedCalendarDateMillis == null) {
            completedSessions
        } else {
            val selCal = Calendar.getInstance().apply { timeInMillis = selectedCalendarDateMillis!! }
            completedSessions.filter { session ->
                val sCal = Calendar.getInstance().apply { timeInMillis = session.endTime ?: session.startTime }
                sCal.get(Calendar.YEAR) == selCal.get(Calendar.YEAR) &&
                        sCal.get(Calendar.DAY_OF_YEAR) == selCal.get(Calendar.DAY_OF_YEAR)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // --- Header Stats Grid ---
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ANALYTICS & HISTORY",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Monthly Report Button
                        IconButton(
                            onClick = { showMonthlyReportDialog = true },
                            modifier = Modifier.testTag("open_monthly_report_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Monthly Report",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Weight Unit Switch
                        ElevatedButton(
                            onClick = onToggleWeightUnit,
                            modifier = Modifier.testTag("toggle_weight_unit_button"),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Unit: ${weightUnit.uppercase()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // 1st Day of Month Banner Prompt
                if (isFirstDayOfMonth) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("1st of the Month!", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("Your monthly progress report is ready", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                            Button(
                                onClick = { showMonthlyReportDialog = true },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total Sessions
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("WORKOUTS", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${completedSessions.size}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Total Volume
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("TOTAL LIFTED", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${totalVolume.toInt()} $weightUnit",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // PRs Count
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("PR RECORDS", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${personalRecords.size}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }

        // --- Sub Navigation Tab Row ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("History", "Muscle & Weight", "PR Trophies", "Data Export").forEach { tab ->
                    val isSelected = selectedSubTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSubTab = tab },
                        label = { Text(tab, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("analytics_tab_$tab")
                    )
                }
            }
        }

        // --- Tab 1: Workout History with Calendar ---
        if (selectedSubTab == "History") {
            item {
                WorkoutCalendarView(
                    completedSessions = completedSessions,
                    selectedDateMillis = selectedCalendarDateMillis,
                    onDateSelected = { selectedCalendarDateMillis = it }
                )
            }

            if (displaySessions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (selectedCalendarDateMillis != null) "No workout logged on this date" else "No completed workouts yet",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (selectedCalendarDateMillis != null) "Tap another date or 'Show All' on calendar" else "Finish a workout session to see your history here",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(displaySessions) { session ->
                    CompletedWorkoutCard(
                        session = session,
                        personalRecords = personalRecords,
                        weightUnit = weightUnit,
                        dateFormat = dateFormat,
                        onDelete = { onDeleteCompletedSession(session) }
                    )
                }
            }
        }

        // --- Tab 2: Muscle Heat Graph & Body Weight Tracking ---
        if (selectedSubTab == "Muscle & Weight") {
            item {
                MuscleHeatGraph(
                    completedSessions = completedSessions,
                    weightUnit = weightUnit
                )
            }

            item {
                BodyWeightChart(
                    measurements = bodyMeasurements,
                    weightUnit = weightUnit,
                    onOpenAddMeasurement = { showAddMeasurementDialog = true },
                    onDeleteMeasurement = onDeleteBodyMeasurement
                )
            }
        }

        // --- Tab 3: PR Trophies ---
        if (selectedSubTab == "PR Trophies") {
            if (personalRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No personal records unlocked yet", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text("Complete sets during workouts to automatically set PRs", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(personalRecords) { pr ->
                    PersonalRecordItemCard(pr = pr, weightUnit = weightUnit)
                }
            }
        }

        // --- Tab 4: Offline Data Backup & Export ---
        if (selectedSubTab == "Data Export") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("100% Offline Data Ownership", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Your fitness logs never leave this device.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Export Section
                        Text("EXPORT BACKUP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val json = onExportBackup()
                                    clipboardManager.setText(AnnotatedString(json))
                                    Toast.makeText(context, "Full JSON backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_full_json_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy JSON", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { showBackupDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_backup_data_button")
                            ) {
                                Icon(imageVector = Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Text Summary", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Import Section
                        Text("IMPORT BACKUP DATA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        OutlinedTextField(
                            value = importJsonInput,
                            onValueChange = { importJsonInput = it },
                            placeholder = { Text("Paste JSON backup data here...", fontSize = 13.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .testTag("import_backup_json_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                if (importJsonInput.isBlank()) {
                                    Toast.makeText(context, "Please paste valid backup JSON text", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isImportingData = true
                                coroutineScope.launch {
                                    val success = onImportBackup(importJsonInput)
                                    isImportingData = false
                                    if (success) {
                                        Toast.makeText(context, "Fitness data imported successfully!", Toast.LENGTH_LONG).show()
                                        importJsonInput = ""
                                    } else {
                                        Toast.makeText(context, "Failed to parse JSON backup text", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            enabled = !isImportingData && importJsonInput.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_import_backup_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isImportingData) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Import Data", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Monthly Report Dialog
    if (showMonthlyReportDialog) {
        MonthlyReportDialog(
            completedSessions = completedSessions,
            personalRecords = personalRecords,
            weightUnit = weightUnit,
            onDismiss = { showMonthlyReportDialog = false }
        )
    }

    // Add Measurement Window Dialog
    if (showAddMeasurementDialog) {
        BodyMeasurementDialog(
            weightUnit = weightUnit,
            onSaveMeasurement = onSaveBodyMeasurement,
            onDismiss = { showAddMeasurementDialog = false }
        )
    }

    // Export Backup Dialog
    if (showBackupDialog) {
        val summaryText = buildString {
            append("NEXTFITNESS OFFLINE BACKUP SUMMARY\n")
            append("Generated: ${dateFormat.format(Date())}\n")
            append("Total Workouts Completed: ${completedSessions.size}\n")
            append("Total Volume Lifted: ${totalVolume.toInt()} $weightUnit\n")
            append("Total Personal Records: ${personalRecords.size}\n\n")
            append("PERSONAL RECORDS:\n")
            personalRecords.forEach { pr ->
                append("• ${pr.exerciseName}: ${pr.maxWeightKg} $weightUnit x ${pr.maxReps} reps\n")
            }
        }

        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Offline Data Export", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Your exported fitness summary:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = summaryText,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("NextFitness Backup", summaryText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Summary copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showBackupDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    modifier = Modifier.testTag("copy_export_data_button")
                ) {
                    Text("Copy Summary", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun CompletedWorkoutCard(
    session: WorkoutSessionEntity,
    personalRecords: List<PersonalRecordEntity>,
    weightUnit: String,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit
) {
    // Calculate total duration
    val durationMs = if (session.endTime != null && session.endTime > session.startTime) {
        session.endTime - session.startTime
    } else 0L

    val durationMins = (durationMs / (1000 * 60)).toInt()
    val durationText = when {
        durationMins >= 60 -> "${durationMins / 60}h ${durationMins % 60}m"
        durationMins > 0 -> "$durationMins mins"
        else -> "< 1 min"
    }

    // Check PRs achieved during this workout session
    val sessionCal = Calendar.getInstance().apply { timeInMillis = session.startTime }
    val prsInSession = personalRecords.filter { pr ->
        val prCal = Calendar.getInstance().apply { timeInMillis = pr.achievedAt }
        prCal.get(Calendar.YEAR) == sessionCal.get(Calendar.YEAR) &&
                prCal.get(Calendar.DAY_OF_YEAR) == sessionCal.get(Calendar.DAY_OF_YEAR)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("completed_session_card_${session.title}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (session.isRampUp || session.phase.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = if (session.isRampUp) "COMEBACK" else session.phase.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (prsInSession.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "🏆 ${prsInSession.size} PR unlocked",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete history", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text(
                text = session.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = dateFormat.format(Date(session.startTime)),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row: Duration, Sets, Volume
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL TIME", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(durationText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SETS LOGGED", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${session.totalSetsCount}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL VOLUME", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${session.totalVolumeKg.toInt()} $weightUnit", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (prsInSession.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    prsInSession.forEach { pr ->
                        Text(
                            text = "🏆 Record: ${pr.exerciseName} (${pr.maxWeightKg} $weightUnit x ${pr.maxReps} reps)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            if (session.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notes: ${session.notes}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun PersonalRecordItemCard(
    pr: PersonalRecordEntity,
    weightUnit: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pr_card_${pr.exerciseName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = pr.exerciseName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Max Reps: ${pr.maxReps}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${pr.maxWeightKg} $weightUnit",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "BEST WEIGHT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

