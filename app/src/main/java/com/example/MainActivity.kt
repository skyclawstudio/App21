package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.MoodLogDialog
import com.example.ui.components.SereneBottomNav
import com.example.ui.components.SereneTopBar
import com.example.ui.screens.BreathworkScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.SanctuaryScreen
import com.example.ui.screens.SoundscapeScreen
import com.example.ui.theme.DeepCosmicNavy
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.SereneTheme
import com.example.ui.theme.TwilightSurface
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MeditationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SereneTheme {
                val viewModel: MeditationViewModel = viewModel()
                SereneMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SereneMainApp(viewModel: MeditationViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val mixerState by viewModel.soundEngine.mixerState.collectAsStateWithLifecycle()
    val breathState by viewModel.breathState.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val completionState by viewModel.completionState.collectAsStateWithLifecycle()
    val stats by viewModel.userStats.collectAsStateWithLifecycle()
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val presets by viewModel.allPresets.collectAsStateWithLifecycle()

    // Handle back button on sub-screens to return to Sanctuary
    BackHandler(enabled = currentScreen != AppScreen.SANCTUARY) {
        if (breathState.isActive) {
            viewModel.stopBreathwork(logSession = true)
        }
        if (timerState.isRunning) {
            viewModel.pauseTimer()
        }
        viewModel.navigateTo(AppScreen.SANCTUARY)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MidnightBlack,
                        DeepCosmicNavy,
                        MidnightBlack
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SereneTopBar(
                    currentStreak = stats.currentStreak,
                    isSoundPlaying = mixerState.isPlaying,
                    onSoundBadgeClick = {
                        viewModel.soundEngine.togglePlayPause()
                    }
                )
            },
            bottomBar = {
                SereneBottomNav(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        viewModel.navigateTo(screen)
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        AppScreen.SANCTUARY -> {
                            SanctuaryScreen(
                                stats = stats,
                                presets = presets,
                                recentSessions = sessions,
                                isSoundPlaying = mixerState.isPlaying,
                                onNavigate = { viewModel.navigateTo(it) },
                                onStartBreathwork = { pattern ->
                                    viewModel.setBreathPattern(pattern)
                                    viewModel.startBreathwork()
                                },
                                onStartTimer = { mins ->
                                    viewModel.setTimerTargetMinutes(mins)
                                    viewModel.startTimer()
                                },
                                onLoadPreset = { preset ->
                                    viewModel.loadPreset(preset)
                                }
                            )
                        }

                        AppScreen.SOUNDSCAPES -> {
                            SoundscapeScreen(
                                mixerState = mixerState,
                                presets = presets,
                                onTogglePlay = { viewModel.soundEngine.togglePlayPause() },
                                onSetMasterVolume = { viewModel.soundEngine.setMasterVolume(it) },
                                onSetChannelVolume = { ch, vol -> viewModel.soundEngine.setChannelVolume(ch, vol) },
                                onApplyPreset = { viewModel.loadPreset(it) },
                                onSaveMix = { name -> viewModel.saveCurrentMixAsPreset(name) },
                                onDeleteCustomPreset = { id -> viewModel.deleteCustomPreset(id) },
                                onSetSleepTimer = { mins -> viewModel.soundEngine.setSleepTimerMinutes(mins) }
                            )
                        }

                        AppScreen.BREATHWORK, AppScreen.TIMER -> {
                            BreathworkScreen(
                                breathState = breathState,
                                timerState = timerState,
                                isSoundPlaying = mixerState.isPlaying,
                                onToggleSound = { viewModel.soundEngine.togglePlayPause() },
                                onSelectBreathPattern = { viewModel.setBreathPattern(it) },
                                onStartBreathwork = { viewModel.startBreathwork() },
                                onStopBreathwork = { viewModel.stopBreathwork(logSession = true) },
                                onSetTimerMinutes = { viewModel.setTimerTargetMinutes(it) },
                                onToggleIntervalBell = { viewModel.toggleIntervalBell() },
                                onStartTimer = { viewModel.startTimer() },
                                onPauseTimer = { viewModel.pauseTimer() },
                                onStopTimer = { viewModel.stopTimer(logSession = true) }
                            )
                        }

                        AppScreen.INSIGHTS -> {
                            InsightsScreen(
                                stats = stats,
                                sessions = sessions,
                                onDeleteSession = { viewModel.deleteSession(it) }
                            )
                        }
                    }
                }
            }
        }

        // Post-Session Reflection & Mood Logging Dialog
        MoodLogDialog(
            state = completionState,
            onMoodSelected = { viewModel.setCompletionMood(it) },
            onNotesChanged = { viewModel.setCompletionNotes(it) },
            onSave = { viewModel.saveCompletedSession() },
            onDismiss = { viewModel.dismissCompletionDialog() }
        )
    }
}
