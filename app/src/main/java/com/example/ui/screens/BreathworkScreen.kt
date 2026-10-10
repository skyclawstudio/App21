package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingGlassCard
import com.example.ui.theme.AquaTeal
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlowCyan
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MoonMuted
import com.example.ui.theme.MysticLavender
import com.example.ui.theme.StarlightWhite
import com.example.ui.theme.TwilightSurfaceVariant
import com.example.ui.theme.WarmAmber
import com.example.viewmodel.BreathPattern
import com.example.viewmodel.BreathPhase
import com.example.viewmodel.BreathSessionState
import com.example.viewmodel.TimerSessionState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BreathworkScreen(
    breathState: BreathSessionState,
    timerState: TimerSessionState,
    isSoundPlaying: Boolean,
    onToggleSound: () -> Unit,
    onSelectBreathPattern: (BreathPattern) -> Unit,
    onStartBreathwork: () -> Unit,
    onStopBreathwork: () -> Unit,
    onSetTimerMinutes: (Int) -> Unit,
    onToggleIntervalBell: () -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onStopTimer: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Mode Switcher Tabs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(GlassSurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                .padding(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selectedTab == 0) GlowCyan else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 10.dp)
                        .testTag("tab_guided_breath"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.SelfImprovement,
                            contentDescription = "Breath",
                            tint = if (selectedTab == 0) MidnightBlack else MoonMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Guided Breath",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) MidnightBlack else StarlightWhite
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selectedTab == 1) GlowCyan else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 10.dp)
                        .testTag("tab_meditate_timer"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = "Timer",
                            tint = if (selectedTab == 1) MidnightBlack else MoonMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mindful Timer",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) MidnightBlack else StarlightWhite
                        )
                    }
                }
            }
        }

        if (selectedTab == 0) {
            GuidedBreathView(
                state = breathState,
                isSoundPlaying = isSoundPlaying,
                onToggleSound = onToggleSound,
                onSelectPattern = onSelectBreathPattern,
                onStart = onStartBreathwork,
                onStop = onStopBreathwork
            )
        } else {
            MindfulTimerView(
                state = timerState,
                isSoundPlaying = isSoundPlaying,
                onToggleSound = onToggleSound,
                onSetDuration = onSetTimerMinutes,
                onToggleIntervalBell = onToggleIntervalBell,
                onStart = onStartTimer,
                onPause = onPauseTimer,
                onStop = onStopTimer
            )
        }
    }
}

@Composable
private fun GuidedBreathView(
    state: BreathSessionState,
    isSoundPlaying: Boolean,
    onToggleSound: () -> Unit,
    onSelectPattern: (BreathPattern) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 110.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pattern Selector Chips (if not currently in active session)
        item {
            if (!state.isActive) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Choose Breathing Rhythm",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StarlightWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(BreathPattern.values().toList()) { pattern ->
                            val isSelected = state.selectedPattern == pattern
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) GlowCyan.copy(alpha = 0.2f) else GlassSurface)
                                    .border(1.dp, if (isSelected) GlowCyan else GlassBorder, RoundedCornerShape(16.dp))
                                    .clickable { onSelectPattern(pattern) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("pattern_${pattern.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column {
                                    Text(
                                        text = pattern.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) GlowCyan else StarlightWhite
                                    )
                                    Text(
                                        text = pattern.subtitle,
                                        fontSize = 11.sp,
                                        color = MoonMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Animated Breathing Visualizer Sphere
        item {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                BreathingCanvasOrb(
                    phase = state.currentPhase,
                    progress = state.phaseProgress,
                    isActive = state.isActive
                )

                // Central Phase & Countdown
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = if (state.isActive) state.currentPhase.label else "Ready",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarlightWhite,
                        letterSpacing = 1.5.sp
                    )

                    if (state.isActive) {
                        Text(
                            text = "${state.phaseSecondsRemaining}s",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light,
                            color = GlowCyan
                        )
                    } else {
                        Text(
                            text = "${state.selectedPattern.totalCycleSeconds}s cycle",
                            fontSize = 13.sp,
                            color = MoonMuted
                        )
                    }
                }
            }
        }

        // Subtitle Guidance Hint
        item {
            Text(
                text = if (state.isActive) state.currentPhase.actionHint else "Press Begin to synchronize your breath",
                fontSize = 14.sp,
                color = if (state.isActive) GlowCyan else MoonMuted,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        // Session Stats Summary Pill
        item {
            if (state.isActive) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Cycles: ${state.completedCycles}",
                        fontSize = 13.sp,
                        color = StarlightWhite
                    )
                    Text(
                        text = "Time: ${state.elapsedSessionSeconds / 60}m ${state.elapsedSessionSeconds % 60}s",
                        fontSize = 13.sp,
                        color = StarlightWhite
                    )
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!state.isActive) {
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("begin_breathwork_btn"),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlowCyan,
                            contentColor = MidnightBlack
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Start",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Begin Journey",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onStop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("stop_breathwork_btn"),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MysticLavender,
                            contentColor = MidnightBlack
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Complete",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Complete & Reflect",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BreathingCanvasOrb(
    phase: BreathPhase,
    progress: Float,
    isActive: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "idle_glow")
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "idle"
    )

    // Calculate dynamic radius factor 0.35f to 0.95f
    val scaleFactor = if (!isActive) {
        idlePulse
    } else {
        when (phase) {
            BreathPhase.INHALE -> 0.4f + (0.55f * progress)
            BreathPhase.HOLD_IN -> 0.95f
            BreathPhase.EXHALE -> 0.95f - (0.55f * progress)
            BreathPhase.HOLD_OUT -> 0.4f
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.width / 2f

        val currentRadius = maxRadius * scaleFactor

        // Outer ripple aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    GlowCyan.copy(alpha = 0.12f),
                    MysticLavender.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = center,
                radius = currentRadius * 1.3f
            ),
            center = center,
            radius = currentRadius * 1.3f
        )

        // Middle luminous ring
        drawCircle(
            color = GlowCyan.copy(alpha = 0.3f),
            center = center,
            radius = currentRadius * 1.12f,
            style = Stroke(width = 2.dp.toPx())
        )

        // Main radiant orb
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    GlowCyan.copy(alpha = 0.45f),
                    AquaTeal.copy(alpha = 0.25f),
                    GlassSurface
                ),
                center = center,
                radius = currentRadius
            ),
            center = center,
            radius = currentRadius
        )

        // Delicate glowing boundary stroke
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(
                    GlowCyan,
                    MysticLavender,
                    AquaTeal,
                    GlowCyan
                ),
                center = center
            ),
            center = center,
            radius = currentRadius,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

@Composable
private fun MindfulTimerView(
    state: TimerSessionState,
    isSoundPlaying: Boolean,
    onToggleSound: () -> Unit,
    onSetDuration: (Int) -> Unit,
    onToggleIntervalBell: () -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit
) {
    val durationOptions = listOf(3, 5, 10, 15, 20, 30, 45)
    val minsLeft = state.remainingSeconds / 60
    val secsLeft = state.remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minsLeft, secsLeft)

    val progress = if (state.targetMinutes > 0) {
        (state.totalElapsedSeconds.toFloat() / (state.targetMinutes * 60f)).coerceIn(0f, 1f)
    } else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 110.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Duration Picker Chips (if not currently running)
        item {
            if (!state.isRunning && !state.isPaused) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Session Duration",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StarlightWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(durationOptions) { mins ->
                            val isSelected = state.targetMinutes == mins
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) GlowCyan.copy(alpha = 0.2f) else GlassSurface)
                                    .border(1.dp, if (isSelected) GlowCyan else GlassBorder, RoundedCornerShape(16.dp))
                                    .clickable { onSetDuration(mins) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("timer_duration_${mins}m"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${mins} min",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) GlowCyan else StarlightWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        // Circular Timer Display
        item {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = (size.width - 24.dp.toPx()) / 2f

                    // Background track
                    drawCircle(
                        color = TwilightSurfaceVariant,
                        center = center,
                        radius = radius,
                        style = Stroke(width = 12.dp.toPx())
                    )

                    // Active progress arc
                    val sweepAngle = progress * 360f
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(GlowCyan, MysticLavender, GlowCyan),
                            center = center
                        ),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = 12.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeFormatted,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarlightWhite,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (state.isRunning) "Breathe in peace" else if (state.isPaused) "Paused" else "Ready to sit",
                        fontSize = 13.sp,
                        color = GlowCyan
                    )
                }
            }
        }

        // Interval Bell & Ambient Audio Toggles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Interval Gong Bell Toggle
                GlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    onClick = onToggleIntervalBell
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (state.intervalBellMinutes != null) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                                contentDescription = "Interval Bell",
                                tint = if (state.intervalBellMinutes != null) WarmAmber else MoonMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Interval Gong",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StarlightWhite
                                )
                                Text(
                                    text = state.intervalBellMinutes?.let { "Every ${it}m" } ?: "Disabled",
                                    fontSize = 11.sp,
                                    color = MoonMuted
                                )
                            }
                        }
                    }
                }

                // Ambient Sound Companion Toggle
                GlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    onClick = onToggleSound
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.VolumeUp,
                                contentDescription = "Sound Companion",
                                tint = if (isSoundPlaying) GlowCyan else MoonMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Ambience",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StarlightWhite
                                )
                                Text(
                                    text = if (isSoundPlaying) "Playing" else "Silent",
                                    fontSize = 11.sp,
                                    color = if (isSoundPlaying) GlowCyan else MoonMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Timer Controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!state.isRunning && !state.isPaused) {
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("start_timer_btn"),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlowCyan,
                            contentColor = MidnightBlack
                        )
                    ) {
                        Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Start", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Begin Meditation", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (state.isRunning) {
                    Button(
                        onClick = onPause,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("pause_timer_btn"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TwilightSurfaceVariant, contentColor = StarlightWhite)
                    ) {
                        Icon(imageVector = Icons.Filled.Pause, contentDescription = "Pause", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pause", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onStop,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("finish_timer_btn"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MysticLavender, contentColor = MidnightBlack)
                    ) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = "Finish", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Finish", fontWeight = FontWeight.Bold)
                    }
                } else if (state.isPaused) {
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("resume_timer_btn"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GlowCyan, contentColor = MidnightBlack)
                    ) {
                        Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Resume", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Resume", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onStop,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("end_timer_btn"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MysticLavender, contentColor = MidnightBlack)
                    ) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = "End", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
