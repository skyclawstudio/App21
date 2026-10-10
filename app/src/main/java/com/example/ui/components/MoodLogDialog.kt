package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlowCyan
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MoonMuted
import com.example.ui.theme.MysticLavender
import com.example.ui.theme.StarlightWhite
import com.example.ui.theme.TwilightSurfaceVariant
import com.example.viewmodel.CompletionDialogState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodLogDialog(
    state: CompletionDialogState,
    onMoodSelected: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!state.isOpen) return

    val moods = listOf(
        "Serene 😌",
        "Grateful 🙏",
        "Calm 🧘",
        "Focused 🎯",
        "Recharged ⚡",
        "Sleepy 🌙"
    )

    val minutes = state.durationSeconds / 60
    val seconds = state.durationSeconds % 60
    val durationText = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(28.dp))
                .border(
                    1.dp,
                    Brush.linearGradient(listOf(GlowCyan.copy(alpha = 0.5f), MysticLavender.copy(alpha = 0.3f))),
                    RoundedCornerShape(28.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = GlassSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Lotus emblem celebrating practice
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(GlowCyan.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                        .border(1.5.dp, GlowCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Spa,
                        contentDescription = "Session Completed",
                        tint = GlowCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Mindful Practice Complete",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarlightWhite,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${state.title} • $durationText",
                    fontSize = 13.sp,
                    color = GlowCyan,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "How are you feeling right now?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = StarlightWhite,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mood Selection FlowRow
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moods.forEach { mood ->
                        val isSelected = state.selectedMood == mood
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) GlowCyan.copy(alpha = 0.25f) else TwilightSurfaceVariant
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) GlowCyan else GlassBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onMoodSelected(mood) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("mood_${mood.take(4).lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mood,
                                fontSize = 13.sp,
                                color = if (isSelected) StarlightWhite else MoonMuted,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Reflection Note
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = onNotesChanged,
                    label = { Text("Mindful Reflection (Optional)", color = MoonMuted, fontSize = 12.sp) },
                    placeholder = { Text("What thoughts or peace arose today?", color = MoonMuted.copy(alpha = 0.5f), fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("reflection_note_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlowCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = StarlightWhite,
                        unfocusedTextColor = StarlightWhite,
                        cursorColor = GlowCyan
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("dismiss_session_button")
                    ) {
                        Text("Skip", color = MoonMuted, fontSize = 14.sp)
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("save_session_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlowCyan,
                            contentColor = MidnightBlack
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Save",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Practice",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
