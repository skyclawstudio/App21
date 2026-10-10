package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MeditationSession
import com.example.data.model.SoundPreset
import com.example.data.repository.UserStats
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingGlassCard
import com.example.ui.theme.AquaTeal
import com.example.ui.theme.CampfireFlame
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlowCyan
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MoonMuted
import com.example.ui.theme.MysticLavender
import com.example.ui.theme.RainBlue
import com.example.ui.theme.StarlightWhite
import com.example.ui.theme.TwilightSurfaceVariant
import com.example.ui.theme.WarmAmber
import com.example.viewmodel.AppScreen
import com.example.viewmodel.BreathPattern
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SanctuaryScreen(
    stats: UserStats,
    presets: List<SoundPreset>,
    recentSessions: List<MeditationSession>,
    isSoundPlaying: Boolean,
    onNavigate: (AppScreen) -> Unit,
    onStartBreathwork: (BreathPattern) -> Unit,
    onStartTimer: (Int) -> Unit,
    onLoadPreset: (SoundPreset) -> Unit
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Peaceful Night"
        }
    }

    val dailyQuote = remember {
        val quotes = listOf(
            "“Peace comes from within. Do not seek it without.” — Buddha",
            "“Quiet the mind, and the soul will speak.” — Ma Jaya Sati",
            "“The present moment is the only moment available to us.” — Thich Nhat Hanh",
            "“In the midst of movement and chaos, keep stillness inside of you.” — Deepak Chopra",
            "“Breathe in calmness, breathe out tension. You are here.”"
        )
        quotes[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % quotes.size]
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Header & Mindful Quotation
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = greeting,
                    fontSize = 15.sp,
                    color = GlowCyan,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Welcome to Stillness",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarlightWhite,
                    lineHeight = 34.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(4.dp, 36.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(GlowCyan)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = dailyQuote,
                            fontSize = 13.sp,
                            color = MoonMuted,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Daily Progress Spotlight Card
        item {
            GlowingGlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = MysticLavender,
                onClick = { onNavigate(AppScreen.INSIGHTS) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = WarmAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${stats.currentStreak}-Day Mindfulness Streak",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WarmAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${stats.totalMinutes} Mindful Minutes",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarlightWhite
                        )

                        Text(
                            text = "${stats.totalSessions} sessions logged so far",
                            fontSize = 12.sp,
                            color = MoonMuted
                        )
                    }

                    Box(
                        modifier = Modifier.size(68.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { ((stats.totalMinutes % 30) / 30f).coerceIn(0.05f, 1f) },
                            modifier = Modifier.fillMaxSize(),
                            color = GlowCyan,
                            trackColor = TwilightSurfaceVariant,
                            strokeWidth = 6.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Spa,
                                contentDescription = "Lotus",
                                tint = GlowCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Start Practice Modules
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Quick Practices",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarlightWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Box Breathing Card
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_box_breath"),
                        shape = RoundedCornerShape(20.dp),
                        onClick = {
                            onStartBreathwork(BreathPattern.BOX)
                            onNavigate(AppScreen.BREATHWORK)
                        }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(GlowCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SelfImprovement,
                                    contentDescription = "Box Breathing",
                                    tint = GlowCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Box Breath",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StarlightWhite
                            )
                            Text(
                                text = "4-4-4-4 Calm",
                                fontSize = 12.sp,
                                color = MoonMuted
                            )
                        }
                    }

                    // 10-Min Timer Card
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_meditate_timer"),
                        shape = RoundedCornerShape(20.dp),
                        onClick = {
                            onStartTimer(10)
                            onNavigate(AppScreen.BREATHWORK)
                        }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MysticLavender.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Timer,
                                    contentDescription = "Timed Sit",
                                    tint = MysticLavender,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Mindful Sit",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StarlightWhite
                            )
                            Text(
                                text = "10 Min Chime",
                                fontSize = 12.sp,
                                color = MoonMuted
                            )
                        }
                    }

                    // Soundscape Mixer Card
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_soundscapes"),
                        shape = RoundedCornerShape(20.dp),
                        onClick = { onNavigate(AppScreen.SOUNDSCAPES) }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(AquaTeal.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.GraphicEq,
                                    contentDescription = "Soundscapes",
                                    tint = AquaTeal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Soundscape",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StarlightWhite
                            )
                            Text(
                                text = "6-Track Mixer",
                                fontSize = 12.sp,
                                color = MoonMuted
                            )
                        }
                    }
                }
            }
        }

        // Curated Nature Soundscapes
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nature Soundscapes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarlightWhite
                    )

                    Text(
                        text = "Customize",
                        fontSize = 13.sp,
                        color = GlowCyan,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigate(AppScreen.SOUNDSCAPES) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(presets) { preset ->
                        val presetIcon = when {
                            preset.name.contains("Rain") -> "🌧️"
                            preset.name.contains("Forest") -> "🌲"
                            preset.name.contains("Ocean") -> "🌊"
                            preset.name.contains("Hearth") -> "🔥"
                            preset.name.contains("Temple") || preset.name.contains("Zen") -> "🔔"
                            else -> "✨"
                        }

                        GlassCard(
                            modifier = Modifier
                                .width(155.dp)
                                .testTag("preset_${preset.name.take(4).lowercase()}"),
                            shape = RoundedCornerShape(20.dp),
                            onClick = { onLoadPreset(preset) }
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = presetIcon,
                                        fontSize = 26.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(TwilightSurfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = "Play",
                                            tint = GlowCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = preset.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StarlightWhite
                                )

                                Text(
                                    text = if (preset.isBuiltIn) "Curated Ambience" else "Custom Mix",
                                    fontSize = 11.sp,
                                    color = MoonMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Mindful Sessions
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Recent Moments",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarlightWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (recentSessions.isEmpty()) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🌱",
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Your mindfulness path begins here",
                                fontSize = 14.sp,
                                color = StarlightWhite,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Complete a quick breath or meditation to log your first moment",
                                fontSize = 12.sp,
                                color = MoonMuted
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        recentSessions.take(3).forEach { session ->
                            val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                            val dateStr = formatter.format(Date(session.timestamp))
                            val mins = session.durationSeconds / 60
                            val secs = session.durationSeconds % 60
                            val durText = if (mins > 0) "${mins}m" else "${secs}s"

                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = session.moodAfter?.take(2) ?: "🧘",
                                            fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = session.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = StarlightWhite
                                            )
                                            Text(
                                                text = "$durText • $dateStr",
                                                fontSize = 12.sp,
                                                color = MoonMuted
                                            )
                                        }
                                    }

                                    if (!session.notes.isNullOrBlank()) {
                                        Text(
                                            text = "“${session.notes.take(18)}…”",
                                            fontSize = 11.sp,
                                            color = GlowCyan,
                                            fontStyle = FontStyle.Italic
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
