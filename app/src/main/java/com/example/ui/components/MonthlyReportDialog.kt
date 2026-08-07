package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PersonalRecordEntity
import com.example.data.WorkoutSessionEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MonthlyReportDialog(
    completedSessions: List<WorkoutSessionEntity>,
    personalRecords: List<PersonalRecordEntity>,
    weightUnit: String,
    onDismiss: () -> Unit
) {
    val cal = Calendar.getInstance()
    val currentMonthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)

    val monthStartCal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    val thisMonthSessions = completedSessions.filter {
        val sessionTime = it.endTime ?: it.startTime
        sessionTime >= monthStartCal.timeInMillis
    }

    val totalWorkouts = thisMonthSessions.size
    val totalVolume = thisMonthSessions.fold(0.0) { acc, s -> acc + s.totalVolumeKg }

    val totalDurationMillis = thisMonthSessions.fold(0L) { acc, s ->
        val end = s.endTime ?: s.startTime
        val duration = (end - s.startTime).coerceAtLeast(0L)
        acc + duration
    }
    val totalHours = totalDurationMillis / (1000 * 60 * 60)
    val totalMinutes = (totalDurationMillis / (1000 * 60)) % 60

    val prsThisMonth = personalRecords.filter { pr ->
        pr.achievedAt >= monthStartCal.timeInMillis
    }.size

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("MONTHLY REPORT", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                    Text(currentMonthName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Workouts Completed", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$totalWorkouts sessions", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Volume Lifted", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${totalVolume.toInt()} $weightUnit", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Time Spent Training", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${totalHours}h ${totalMinutes}m", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("PR Records Set", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$prsThisMonth new PRs 🏆", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }

                Text(
                    text = if (totalWorkouts >= 8) "🔥 Incredible consistency this month! Keep building muscle and strength."
                    else if (totalWorkouts > 0) "💪 Good start this month! Aim for 3-4 workouts per week to maximize progress."
                    else "📌 No workouts logged yet this month. Time to hit the gym!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_monthly_report_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Got It!", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
