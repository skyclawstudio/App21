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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundChannel
import com.example.audio.SoundMixerState
import com.example.data.model.SoundPreset
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingGlassCard
import com.example.ui.theme.CampfireFlame
import com.example.ui.theme.CricketsForest
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlowCyan
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MoonMuted
import com.example.ui.theme.MysticLavender
import com.example.ui.theme.OceanTeal
import com.example.ui.theme.RainBlue
import com.example.ui.theme.SingingBowlGold
import com.example.ui.theme.StarlightWhite
import com.example.ui.theme.TwilightSurfaceVariant
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WindBreeze

@Composable
fun SoundscapeScreen(
    mixerState: SoundMixerState,
    presets: List<SoundPreset>,
    onTogglePlay: () -> Unit,
    onSetMasterVolume: (Float) -> Unit,
    onSetChannelVolume: (SoundChannel, Float) -> Unit,
    onApplyPreset: (SoundPreset) -> Unit,
    onSaveMix: (String) -> Unit,
    onDeleteCustomPreset: (Long) -> Unit,
    onSetSleepTimer: (Int?) -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }
    var showSleepTimerSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Title & Description
        item {
            Column {
                Text(
                    text = "Soundscape Studio",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarlightWhite
                )
                Text(
                    text = "Blend organic nature tracks into your bespoke sanctuary",
                    fontSize = 13.sp,
                    color = MoonMuted
                )
            }
        }

        // Master Control Station Card
        item {
            GlowingGlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = if (mixerState.isPlaying) GlowCyan else MysticLavender
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = mixerState.activePresetName ?: "Custom Blend",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = StarlightWhite
                            )
                            Text(
                                text = if (mixerState.isPlaying) "Live procedural sound" else "Paused",
                                fontSize = 12.sp,
                                color = if (mixerState.isPlaying) GlowCyan else MoonMuted
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Sleep Timer Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(TwilightSurfaceVariant)
                                    .clickable { showSleepTimerSheet = !showSleepTimerSheet }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                    .testTag("sleep_timer_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Bedtime,
                                        contentDescription = "Sleep Timer",
                                        tint = if (mixerState.sleepTimerRemainingSeconds != null) WarmAmber else MoonMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = mixerState.sleepTimerRemainingSeconds?.let {
                                            "${it / 60}m"
                                        } ?: "Timer",
                                        fontSize = 12.sp,
                                        color = if (mixerState.sleepTimerRemainingSeconds != null) WarmAmber else MoonMuted,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Master Play/Pause Floating Orb
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (mixerState.isPlaying) GlowCyan else MysticLavender)
                                    .clickable(onClick = onTogglePlay)
                                    .testTag("master_play_pause_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (mixerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = "Master Play/Pause",
                                    tint = MidnightBlack,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Master Volume Slider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VolumeDown,
                            contentDescription = "Low Volume",
                            tint = MoonMuted,
                            modifier = Modifier.size(18.dp)
                        )

                        Slider(
                            value = mixerState.masterVolume,
                            onValueChange = onSetMasterVolume,
                            valueRange = 0f..1f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .testTag("master_volume_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = GlowCyan,
                                activeTrackColor = GlowCyan,
                                inactiveTrackColor = TwilightSurfaceVariant
                            )
                        )

                        Icon(
                            imageVector = Icons.Filled.VolumeUp,
                            contentDescription = "High Volume",
                            tint = GlowCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Sleep Timer Options Panel (if expanded)
                    AnimatedVisibility(visible = showSleepTimerSheet) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Text(
                                text = "Gentle Sleep Fade Timer",
                                fontSize = 12.sp,
                                color = MoonMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(null to "Off", 5 to "5m", 15 to "15m", 30 to "30m", 60 to "60m").forEach { (mins, label) ->
                                    val isSelected = (mins == null && mixerState.sleepTimerRemainingSeconds == null) ||
                                            (mins != null && mixerState.sleepTimerRemainingSeconds != null && mixerState.sleepTimerRemainingSeconds / 60 == mins)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) GlowCyan.copy(alpha = 0.25f) else TwilightSurfaceVariant)
                                            .border(1.dp, if (isSelected) GlowCyan else GlassBorder, RoundedCornerShape(12.dp))
                                            .clickable {
                                                onSetSleepTimer(mins)
                                                showSleepTimerSheet = false
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            color = if (isSelected) StarlightWhite else MoonMuted,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Presets Ribbon & Save Mix Button
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ambience Presets",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarlightWhite
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(TwilightSurfaceVariant)
                            .clickable { showSaveDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("save_current_mix_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Save",
                            tint = GlowCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Save Mix",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GlowCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(presets) { preset ->
                        val isCurrent = mixerState.activePresetName == preset.name
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isCurrent) GlowCyan.copy(alpha = 0.2f) else GlassSurface)
                                .border(1.dp, if (isCurrent) GlowCyan else GlassBorder, RoundedCornerShape(16.dp))
                                .clickable { onApplyPreset(preset) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("apply_preset_${preset.name.take(4).lowercase()}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = preset.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCurrent) GlowCyan else StarlightWhite
                                )

                                if (!preset.isBuiltIn) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Delete Preset",
                                        tint = MoonMuted,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { onDeleteCustomPreset(preset.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6 Multi-Track Channel Cards
        item {
            Text(
                text = "Individual Track Mixers",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = StarlightWhite
            )
        }

        items(SoundChannel.values().toList()) { channel ->
            val volume = mixerState.channelVolumes[channel] ?: 0f
            val isPlaying = mixerState.isPlaying && volume > 0.01f

            val channelColor = when (channel) {
                SoundChannel.RAIN -> RainBlue
                SoundChannel.OCEAN -> OceanTeal
                SoundChannel.WIND -> WindBreeze
                SoundChannel.CRICKETS -> CricketsForest
                SoundChannel.FIRE -> CampfireFlame
                SoundChannel.BOWL -> SingingBowlGold
            }

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("channel_card_${channel.name.lowercase()}"),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = channel.iconEmoji,
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = channel.displayName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StarlightWhite
                                )
                                Text(
                                    text = "${(volume * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    color = if (volume > 0.01f) channelColor else MoonMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Sound Waveform Bars Animation
                        AudioWaveformVisualizer(
                            isActive = isPlaying,
                            color = channelColor,
                            modifier = Modifier.size(width = 46.dp, height = 24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Channel Slider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                onSetChannelVolume(channel, if (volume > 0f) 0f else 0.7f)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (volume > 0f) Icons.Filled.VolumeDown else Icons.Filled.VolumeMute,
                                contentDescription = "Mute/Unmute",
                                tint = if (volume > 0f) channelColor else DarkMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Slider(
                            value = volume,
                            onValueChange = { onSetChannelVolume(channel, it) },
                            valueRange = 0f..1f,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("slider_${channel.name.lowercase()}"),
                            colors = SliderDefaults.colors(
                                thumbColor = channelColor,
                                activeTrackColor = channelColor,
                                inactiveTrackColor = TwilightSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }

    // Save Preset Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text("Save Current Sound Mix", color = StarlightWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Give your custom soundscape balance a tranquil name:",
                        color = MoonMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        placeholder = { Text("e.g. Starlit Campfire", color = MoonMuted.copy(alpha = 0.5f)) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowCyan,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = StarlightWhite,
                            unfocusedTextColor = StarlightWhite
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("preset_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            onSaveMix(newPresetName.trim())
                            newPresetName = ""
                            showSaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowCyan, contentColor = MidnightBlack),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_save_preset_btn")
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = MoonMuted)
                }
            },
            containerColor = GlassSurface
        )
    }
}

@Composable
fun AudioWaveformVisualizer(
    isActive: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveAnim1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w1"
    )
    val waveAnim2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w2"
    )
    val waveAnim3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w3"
    )

    Canvas(modifier = modifier) {
        val barCount = 4
        val barWidth = 4.dp.toPx()
        val spacing = (size.width - (barCount * barWidth)) / (barCount - 1)

        val factors = if (isActive) {
            listOf(waveAnim1, waveAnim2, waveAnim3, (waveAnim1 + waveAnim2) / 2f)
        } else {
            listOf(0.15f, 0.15f, 0.15f, 0.15f)
        }

        factors.forEachIndexed { index, factor ->
            val barHeight = size.height * factor
            val x = index * (barWidth + spacing)
            val y = (size.height - barHeight) / 2f

            drawRoundRect(
                color = if (isActive) color else DarkMuted.copy(alpha = 0.3f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}
