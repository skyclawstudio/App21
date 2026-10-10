package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlowCyan
import com.example.ui.theme.TwilightSurfaceVariant
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MoonMuted
import com.example.ui.theme.MysticLavender
import com.example.ui.theme.StarlightWhite
import com.example.ui.theme.WarmAmber
import com.example.viewmodel.AppScreen

@Composable
fun SereneTopBar(
    currentStreak: Int,
    isSoundPlaying: Boolean,
    onSoundBadgeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title & Lotus Icon
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                GlowCyan.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(1.dp, GlowCyan.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Spa,
                    contentDescription = "Serene Lotus Logo",
                    tint = GlowCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Serene",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = StarlightWhite,
                letterSpacing = 1.sp
            )
        }

        // Streak badge & Ambient Sound Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Streak Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassSurface)
                    .border(1.dp, WarmAmber.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = WarmAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$currentStreak d",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StarlightWhite
                )
            }

            // Audio Player Active Pill
            val soundBgColor by animateColorAsState(
                if (isSoundPlaying) GlowCyan.copy(alpha = 0.2f) else GlassSurface,
                label = "sound_bg"
            )
            val soundBorderColor by animateColorAsState(
                if (isSoundPlaying) GlowCyan else GlassBorder,
                label = "sound_border"
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(soundBgColor)
                    .border(1.dp, soundBorderColor, RoundedCornerShape(16.dp))
                    .clickable(onClick = onSoundBadgeClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("audio_toggle_pill"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSoundPlaying) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                    contentDescription = "Ambient Sound Status",
                    tint = if (isSoundPlaying) GlowCyan else MoonMuted,
                    modifier = Modifier
                        .size(16.dp)
                        .then(if (isSoundPlaying) Modifier.scale(pulseScale) else Modifier)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isSoundPlaying) "Nature Live" else "Muted",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSoundPlaying) GlowCyan else MoonMuted
                )
            }
        }
    }
}

data class NavItem(
    val screen: AppScreen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun SereneBottomNav(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(
            screen = AppScreen.SANCTUARY,
            title = "Sanctuary",
            selectedIcon = Icons.Filled.Spa,
            unselectedIcon = Icons.Outlined.Spa,
            testTag = "nav_sanctuary"
        ),
        NavItem(
            screen = AppScreen.SOUNDSCAPES,
            title = "Soundscapes",
            selectedIcon = Icons.Filled.GraphicEq,
            unselectedIcon = Icons.Outlined.GraphicEq,
            testTag = "nav_soundscapes"
        ),
        NavItem(
            screen = AppScreen.BREATHWORK,
            title = "Breathe",
            selectedIcon = Icons.Filled.SelfImprovement,
            unselectedIcon = Icons.Outlined.SelfImprovement,
            testTag = "nav_breathwork"
        ),
        NavItem(
            screen = AppScreen.INSIGHTS,
            title = "Insights",
            selectedIcon = Icons.Filled.BarChart,
            unselectedIcon = Icons.Outlined.BarChart,
            testTag = "nav_insights"
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        NavigationBar(
            modifier = Modifier
                .clip(RoundedCornerShape(26.dp))
                .border(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(
                            GlassBorder.copy(alpha = 0.4f),
                            MysticLavender.copy(alpha = 0.2f),
                            GlassBorder.copy(alpha = 0.4f)
                        )
                    ),
                    RoundedCornerShape(26.dp)
                ),
            containerColor = GlassSurface,
            tonalElevation = 8.dp
        ) {
            items.forEach { item ->
                val selected = currentScreen == item.screen
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.screen) },
                    icon = {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GlowCyan,
                        selectedTextColor = GlowCyan,
                        indicatorColor = TwilightSurfaceVariant,
                        unselectedIconColor = MoonMuted,
                        unselectedTextColor = MoonMuted
                    ),
                    modifier = Modifier.testTag(item.testTag)
                )
            }
        }
    }
}
