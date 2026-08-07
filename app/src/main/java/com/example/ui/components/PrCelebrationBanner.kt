package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.PersonalRecordEntity
import com.example.ui.theme.NextDarkSurface
import com.example.ui.theme.NextGoldAccent

@Composable
fun PrCelebrationDialog(
    prRecord: PersonalRecordEntity,
    weightUnit: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NextDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("pr_celebration_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "New PR",
                    tint = NextGoldAccent,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "NEW PERSONAL RECORD!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = NextGoldAccent,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = prRecord.exerciseName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = NextGoldAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MAX WEIGHT", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${prRecord.maxWeightKg} $weightUnit",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = NextGoldAccent
                            )
                        }

                        Divider(
                            modifier = Modifier
                                .height(32.dp)
                                .width(1.dp),
                            color = NextGoldAccent.copy(alpha = 0.3f)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MAX REPS", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${prRecord.maxReps} reps",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = NextGoldAccent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pr_dismiss_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NextGoldAccent, contentColor = androidx.compose.ui.graphics.Color.White)
                ) {
                    Text("KEEP CRUSHING IT", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
