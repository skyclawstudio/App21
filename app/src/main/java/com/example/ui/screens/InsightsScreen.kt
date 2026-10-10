package com.example.ui.screens

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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MeditationSession
import com.example.data.model.SessionType
import com.example.data.repository.UserStats
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingGlassCard
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MilestoneBadge(
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val iconEmoji: String
)

@Composable
fun InsightsScreen(
    stats: UserStats,
    sessions: List<MeditationSession>,
    onDeleteSession: (Long) -> Unit
) {
    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val calDays = listOf(
        Calendar.SUNDAY,
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY
    )

    val milestones = listOf(
        MilestoneBadge("First Breath", "Completed your 1st session", sessions.isNotEmpty(), "🌱"),
        MilestoneBadge("3-Day Stillness", "Maintained 3-day streak", stats.bestStreak >= 3, "🔥"),
        MilestoneBadge("Zen Master", "Achieved 7-day streak", stats.bestStreak >= 7, "🧘"),
        MilestoneBadge("One Hour Calm", "Accumulated 60+ minutes", stats.totalMinutes >= 60, "⏳"),
        MilestoneBadge("Deep Devotion", "Logged 10+ mindful sessions", stats.totalSessions >= 10, "✨")
    )

    // Calculate Mood Counts
    val moodCounts = sessions.mapNotNull { it.moodAfter }.groupingBy { it }.eachCount()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title & Header
        item {
            Column {
                Text(
                    text = "Personal Progress",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarlightWhite
                )
                Text(
                    text = "Your journey of presence, breath, and inner clarity",
                    fontSize = 13.sp,
                    color = MoonMuted
                )
            }
        }

        // Streak & Total Time Banner Card
        item {
            GlowingGlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = WarmAmber
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
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${stats.currentStreak}-Day Streak",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (stats.currentStreak > 0)
                                "Consistency is quiet power. Keep the fire glowing."
                            else "Begin today’s practice to cultivate your streak.",
                            fontSize = 12.sp,
                            color = MoonMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text("Best Streak", fontSize = 11.sp, color = MoonMuted)
                                Text("${stats.bestStreak} days", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StarlightWhite)
                            }
                            Column {
                                Text("Total Sessions", fontSize = 11.sp, color = MoonMuted)
                                Text("${stats.totalSessions}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GlowCyan)
                            }
                        }
                    }

                    // Circle with total minutes
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(WarmAmber.copy(alpha = 0.25f), GlassSurface)
                                )
                            )
                            .border(1.dp, WarmAmber.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${stats.totalMinutes}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = StarlightWhite
                            )
                            Text(
                                text = "Mins",
                                fontSize = 11.sp,
                                color = WarmAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Weekly Activity Chart Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "This Week's Minutes",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarlightWhite
                        )
                        Text(
                            text = "Target: 15m/day",
                            fontSize = 12.sp,
                            color = GlowCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 7-day Bar Chart Canvas
                    val maxVal = maxOf(25, stats.weeklyMinutes.values.maxOrNull() ?: 15)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val barWidth = 22.dp.toPx()
                            val spacing = (size.width - (7 * barWidth)) / 8f
                            val chartHeight = size.height - 24.dp.toPx()

                            // Target guideline at 15 mins
                            val targetY = chartHeight - (15f / maxVal.toFloat() * chartHeight)
                            drawLine(
                                color = GlassBorder.copy(alpha = 0.4f),
                                start = Offset(0f, targetY),
                                end = Offset(size.width, targetY),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )

                            calDays.forEachIndexed { index, dayCal ->
                                val mins = stats.weeklyMinutes[dayCal] ?: 0
                                val barHeight = if (mins > 0) {
                                    (mins.toFloat() / maxVal.toFloat() * chartHeight).coerceAtLeast(8.dp.toPx())
                                } else 4.dp.toPx()

                                val x = spacing + index * (barWidth + spacing)
                                val y = chartHeight - barHeight

                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = if (mins > 0) listOf(GlowCyan, MysticLavender)
                                        else listOf(TwilightSurfaceVariant, TwilightSurfaceVariant)
                                    ),
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                            }
                        }

                        // Day Labels Row below the chart
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            dayNames.forEach { dayName ->
                                Text(
                                    text = dayName,
                                    fontSize = 11.sp,
                                    color = MoonMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Mindful Milestones & Badges
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "Badges",
                        tint = GlowCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mindfulness Milestones",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarlightWhite
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(milestones) { badge ->
                        GlassCard(
                            modifier = Modifier
                                .width(140.dp)
                                .testTag("badge_${badge.title.take(4).lowercase()}"),
                            shape = RoundedCornerShape(18.dp),
                            borderColor = if (badge.isUnlocked) GlowCyan.copy(alpha = 0.4f) else GlassBorder
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (badge.isUnlocked) GlowCyan.copy(alpha = 0.2f) else TwilightSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (badge.isUnlocked) {
                                        Text(text = badge.iconEmoji, fontSize = 22.sp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.Lock,
                                            contentDescription = "Locked",
                                            tint = MoonMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = badge.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (badge.isUnlocked) StarlightWhite else MoonMuted,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = badge.description,
                                    fontSize = 10.sp,
                                    color = MoonMuted,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Mood Distribution Breakdown
        if (moodCounts.isNotEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Inner State Distribution",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarlightWhite
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            moodCounts.entries.take(4).forEach { (mood, count) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = mood.take(2), fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$count times",
                                        fontSize = 11.sp,
                                        color = GlowCyan,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = mood.drop(2).trim(),
                                        fontSize = 10.sp,
                                        color = MoonMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Session Log History
        item {
            Text(
                text = "Practice History (${sessions.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = StarlightWhite
            )
        }

        if (sessions.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📜", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("No sessions recorded yet", fontSize = 14.sp, color = StarlightWhite)
                        Text("Your reflections and timestamps will appear here", fontSize = 12.sp, color = MoonMuted)
                    }
                }
            }
        } else {
            items(sessions) { session ->
                val formatter = SimpleDateFormat("EEEE, MMM d • h:mm a", Locale.getDefault())
                val dateStr = formatter.format(Date(session.timestamp))
                val mins = session.durationSeconds / 60
                val secs = session.durationSeconds % 60
                val durText = if (mins > 0) "${mins}m ${secs}s" else "${secs}s"

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("session_item_${session.id}"),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (session.sessionType) {
                                            SessionType.BREATHWORK -> GlowCyan.copy(alpha = 0.2f)
                                            SessionType.TIMED_MEDITATION -> MysticLavender.copy(alpha = 0.2f)
                                            SessionType.SOUNDSCAPE_FLOW -> WarmAmber.copy(alpha = 0.2f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (session.sessionType) {
                                        SessionType.BREATHWORK -> Icons.Filled.SelfImprovement
                                        SessionType.TIMED_MEDITATION -> Icons.Filled.Timer
                                        SessionType.SOUNDSCAPE_FLOW -> Icons.Filled.Spa
                                    },
                                    contentDescription = session.sessionType.name,
                                    tint = when (session.sessionType) {
                                        SessionType.BREATHWORK -> GlowCyan
                                        SessionType.TIMED_MEDITATION -> MysticLavender
                                        SessionType.SOUNDSCAPE_FLOW -> WarmAmber
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }

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
                                    fontSize = 11.sp,
                                    color = MoonMuted
                                )
                                if (!session.notes.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "“${session.notes}”",
                                        fontSize = 12.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = GlowCyan
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (session.moodAfter != null) {
                                Text(
                                    text = session.moodAfter.take(2),
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }

                            IconButton(
                                onClick = { onDeleteSession(session.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Delete entry",
                                    tint = DarkMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
