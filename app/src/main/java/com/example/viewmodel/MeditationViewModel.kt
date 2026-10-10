package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AmbientSoundEngine
import com.example.audio.SoundChannel
import com.example.audio.SoundMixerState
import com.example.data.database.AppDatabase
import com.example.data.model.MeditationSession
import com.example.data.model.SessionType
import com.example.data.model.SoundPreset
import com.example.data.repository.MeditationRepository
import com.example.data.repository.UserStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppScreen {
    SANCTUARY,
    SOUNDSCAPES,
    BREATHWORK,
    TIMER,
    INSIGHTS
}

enum class BreathPattern(
    val title: String,
    val subtitle: String,
    val inhaleSeconds: Int,
    val holdInSeconds: Int,
    val exhaleSeconds: Int,
    val holdOutSeconds: Int
) {
    BOX("Box Breathing", "Reset & Focus", 4, 4, 4, 4),
    RELAX_4_7_8("4-7-8 Relaxation", "Deep Calm & Sleep", 4, 7, 8, 0),
    CALM_5_5("Coherent Flow", "Heart Rate Variability", 5, 0, 5, 0),
    AWAKE("Awakening Rhythm", "Clarity & Vitality", 4, 2, 4, 2);

    val totalCycleSeconds: Int
        get() = inhaleSeconds + holdInSeconds + exhaleSeconds + holdOutSeconds
}

enum class BreathPhase(val label: String, val actionHint: String) {
    INHALE("Inhale", "Fill your lungs with calm starlight"),
    HOLD_IN("Hold", "Rest gently in the stillness"),
    EXHALE("Exhale", "Release tension with the breath"),
    HOLD_OUT("Sustain", "Empty, peaceful, centered")
}

data class BreathSessionState(
    val isActive: Boolean = false,
    val selectedPattern: BreathPattern = BreathPattern.BOX,
    val currentPhase: BreathPhase = BreathPhase.INHALE,
    val phaseProgress: Float = 0f, // 0.0f .. 1.0f within current phase
    val phaseSecondsRemaining: Int = 4,
    val elapsedSessionSeconds: Int = 0,
    val completedCycles: Int = 0
)

data class TimerSessionState(
    val targetMinutes: Int = 10,
    val remainingSeconds: Int = 600,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val intervalBellMinutes: Int? = 5, // chime every 5 min
    val totalElapsedSeconds: Int = 0
)

data class CompletionDialogState(
    val isOpen: Boolean = false,
    val sessionType: SessionType = SessionType.TIMED_MEDITATION,
    val durationSeconds: Int = 0,
    val title: String = "Meditation Session",
    val selectedMood: String = "Serene 😌",
    val notes: String = ""
)

class MeditationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MeditationRepository
    val soundEngine: AmbientSoundEngine

    // Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.SANCTUARY)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Breathwork
    private val _breathState = MutableStateFlow(BreathSessionState())
    val breathState: StateFlow<BreathSessionState> = _breathState.asStateFlow()
    private var breathJob: Job? = null

    // Meditation Timer
    private val _timerState = MutableStateFlow(TimerSessionState())
    val timerState: StateFlow<TimerSessionState> = _timerState.asStateFlow()
    private var timerJob: Job? = null

    // Completion Dialog
    private val _completionState = MutableStateFlow(CompletionDialogState())
    val completionState: StateFlow<CompletionDialogState> = _completionState.asStateFlow()

    // Database Flows
    val allSessions: StateFlow<List<MeditationSession>>
    val allPresets: StateFlow<List<SoundPreset>>
    val userStats: StateFlow<UserStats>

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MeditationRepository(database.meditationDao())
        soundEngine = AmbientSoundEngine(viewModelScope)

        allSessions = repository.allSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allPresets = repository.allPresets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        userStats = allSessions.map { sessions ->
            repository.calculateStats(sessions)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserStats()
        )

        viewModelScope.launch {
            repository.ensureDefaultPresets()
        }
    }

    // Navigation
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // Breathwork Logic
    fun setBreathPattern(pattern: BreathPattern) {
        if (!_breathState.value.isActive) {
            _breathState.value = _breathState.value.copy(
                selectedPattern = pattern,
                currentPhase = BreathPhase.INHALE,
                phaseProgress = 0f,
                phaseSecondsRemaining = pattern.inhaleSeconds
            )
        }
    }

    fun startBreathwork() {
        if (_breathState.value.isActive) return

        val pattern = _breathState.value.selectedPattern
        _breathState.value = _breathState.value.copy(
            isActive = true,
            currentPhase = BreathPhase.INHALE,
            phaseProgress = 0f,
            phaseSecondsRemaining = pattern.inhaleSeconds,
            elapsedSessionSeconds = 0,
            completedCycles = 0
        )

        triggerGentleHaptic()

        breathJob?.cancel()
        breathJob = viewModelScope.launch {
            val stepMs = 50L // 20 updates per second for smooth fluid animation
            var phaseMsRemaining = pattern.inhaleSeconds * 1000L
            var phaseTotalMs = pattern.inhaleSeconds * 1000L
            var currentPhase = BreathPhase.INHALE
            var cycles = 0
            var totalElapsed = 0

            while (isActive) {
                delay(stepMs)
                phaseMsRemaining -= stepMs
                totalElapsed += stepMs.toInt()

                val progress = 1f - (phaseMsRemaining.toFloat() / phaseTotalMs.toFloat()).coerceIn(0f, 1f)
                val secRemaining = (phaseMsRemaining / 1000L).toInt() + 1

                _breathState.value = _breathState.value.copy(
                    phaseProgress = progress,
                    phaseSecondsRemaining = secRemaining,
                    elapsedSessionSeconds = totalElapsed / 1000,
                    completedCycles = cycles
                )

                if (phaseMsRemaining <= 0) {
                    // Transition to next phase
                    val (nextPhase, nextSec) = getNextBreathPhase(pattern, currentPhase)
                    currentPhase = nextPhase
                    phaseTotalMs = nextSec * 1000L
                    phaseMsRemaining = phaseTotalMs

                    if (nextPhase == BreathPhase.INHALE) {
                        cycles++
                    }

                    triggerGentleHaptic()

                    _breathState.value = _breathState.value.copy(
                        currentPhase = nextPhase,
                        phaseProgress = 0f,
                        phaseSecondsRemaining = nextSec,
                        completedCycles = cycles
                    )
                }
            }
        }
    }

    private fun getNextBreathPhase(pattern: BreathPattern, current: BreathPhase): Pair<BreathPhase, Int> {
        return when (current) {
            BreathPhase.INHALE -> {
                if (pattern.holdInSeconds > 0) BreathPhase.HOLD_IN to pattern.holdInSeconds
                else BreathPhase.EXHALE to pattern.exhaleSeconds
            }
            BreathPhase.HOLD_IN -> BreathPhase.EXHALE to pattern.exhaleSeconds
            BreathPhase.EXHALE -> {
                if (pattern.holdOutSeconds > 0) BreathPhase.HOLD_OUT to pattern.holdOutSeconds
                else BreathPhase.INHALE to pattern.inhaleSeconds
            }
            BreathPhase.HOLD_OUT -> BreathPhase.INHALE to pattern.inhaleSeconds
        }
    }

    fun stopBreathwork(logSession: Boolean = true) {
        val elapsed = _breathState.value.elapsedSessionSeconds
        breathJob?.cancel()
        breathJob = null

        _breathState.value = _breathState.value.copy(
            isActive = false,
            phaseProgress = 0f
        )

        if (logSession && elapsed >= 10) {
            _completionState.value = CompletionDialogState(
                isOpen = true,
                sessionType = SessionType.BREATHWORK,
                durationSeconds = elapsed,
                title = "${_breathState.value.selectedPattern.title} Session"
            )
        }
    }

    // Timer Logic
    fun setTimerTargetMinutes(minutes: Int) {
        if (!_timerState.value.isRunning) {
            _timerState.value = _timerState.value.copy(
                targetMinutes = minutes,
                remainingSeconds = minutes * 60,
                totalElapsedSeconds = 0
            )
        }
    }

    fun toggleIntervalBell() {
        val current = _timerState.value.intervalBellMinutes
        val next = when (current) {
            null -> 5
            5 -> 10
            10 -> 15
            else -> null
        }
        _timerState.value = _timerState.value.copy(intervalBellMinutes = next)
    }

    fun startTimer() {
        if (_timerState.value.isRunning && !_timerState.value.isPaused) return

        if (_timerState.value.isPaused) {
            _timerState.value = _timerState.value.copy(isRunning = true, isPaused = false)
        } else {
            _timerState.value = _timerState.value.copy(
                isRunning = true,
                isPaused = false,
                remainingSeconds = _timerState.value.targetMinutes * 60,
                totalElapsedSeconds = 0
            )
            soundEngine.triggerBellChime()
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _timerState.value.remainingSeconds > 0) {
                delay(1000L)
                val remaining = _timerState.value.remainingSeconds - 1
                val elapsed = _timerState.value.totalElapsedSeconds + 1

                _timerState.value = _timerState.value.copy(
                    remainingSeconds = remaining,
                    totalElapsedSeconds = elapsed
                )

                // Check interval bell
                val intervalMins = _timerState.value.intervalBellMinutes
                if (intervalMins != null && intervalMins > 0) {
                    val intervalSec = intervalMins * 60
                    if (elapsed > 0 && elapsed % intervalSec == 0 && remaining > 0) {
                        soundEngine.triggerBellChime()
                        triggerGentleHaptic()
                    }
                }
            }

            if (isActive && _timerState.value.remainingSeconds <= 0) {
                // Completed!
                soundEngine.triggerBellChime()
                triggerGentleHaptic()
                val totalTime = _timerState.value.totalElapsedSeconds
                _timerState.value = _timerState.value.copy(isRunning = false, isPaused = false)

                _completionState.value = CompletionDialogState(
                    isOpen = true,
                    sessionType = SessionType.TIMED_MEDITATION,
                    durationSeconds = totalTime,
                    title = "${_timerState.value.targetMinutes}-Minute Mindful Meditation"
                )
            }
        }
    }

    fun pauseTimer() {
        if (_timerState.value.isRunning) {
            timerJob?.cancel()
            _timerState.value = _timerState.value.copy(isRunning = false, isPaused = true)
        }
    }

    fun stopTimer(logSession: Boolean = true) {
        val elapsed = _timerState.value.totalElapsedSeconds
        timerJob?.cancel()
        timerJob = null

        _timerState.value = _timerState.value.copy(
            isRunning = false,
            isPaused = false,
            remainingSeconds = _timerState.value.targetMinutes * 60
        )

        if (logSession && elapsed >= 10) {
            _completionState.value = CompletionDialogState(
                isOpen = true,
                sessionType = SessionType.TIMED_MEDITATION,
                durationSeconds = elapsed,
                title = "Mindful Meditation Session"
            )
        }
    }

    // Completion Dialog & Session Logging
    fun setCompletionMood(mood: String) {
        _completionState.value = _completionState.value.copy(selectedMood = mood)
    }

    fun setCompletionNotes(notes: String) {
        _completionState.value = _completionState.value.copy(notes = notes)
    }

    fun dismissCompletionDialog() {
        _completionState.value = _completionState.value.copy(isOpen = false)
    }

    fun saveCompletedSession() {
        val state = _completionState.value
        viewModelScope.launch {
            repository.insertSession(
                MeditationSession(
                    sessionType = state.sessionType,
                    title = state.title,
                    durationSeconds = state.durationSeconds,
                    moodAfter = state.selectedMood,
                    notes = state.notes.ifBlank { null }
                )
            )
            _completionState.value = _completionState.value.copy(isOpen = false)
        }
    }

    fun logQuickSession(title: String, durationSeconds: Int, type: SessionType) {
        viewModelScope.launch {
            repository.insertSession(
                MeditationSession(
                    sessionType = type,
                    title = title,
                    durationSeconds = durationSeconds,
                    moodAfter = "Serene 😌"
                )
            )
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            repository.deleteSession(id)
        }
    }

    fun saveCurrentMixAsPreset(name: String) {
        val mixer = soundEngine.mixerState.value
        viewModelScope.launch {
            repository.insertPreset(
                SoundPreset(
                    name = name,
                    rainVolume = mixer.channelVolumes[SoundChannel.RAIN] ?: 0f,
                    oceanVolume = mixer.channelVolumes[SoundChannel.OCEAN] ?: 0f,
                    windVolume = mixer.channelVolumes[SoundChannel.WIND] ?: 0f,
                    cricketsVolume = mixer.channelVolumes[SoundChannel.CRICKETS] ?: 0f,
                    fireVolume = mixer.channelVolumes[SoundChannel.FIRE] ?: 0f,
                    bowlVolume = mixer.channelVolumes[SoundChannel.BOWL] ?: 0f,
                    isBuiltIn = false
                )
            )
        }
    }

    fun deleteCustomPreset(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomPreset(id)
        }
    }

    fun loadPreset(preset: SoundPreset) {
        val map = mapOf(
            SoundChannel.RAIN to preset.rainVolume,
            SoundChannel.OCEAN to preset.oceanVolume,
            SoundChannel.WIND to preset.windVolume,
            SoundChannel.CRICKETS to preset.cricketsVolume,
            SoundChannel.FIRE to preset.fireVolume,
            SoundChannel.BOWL to preset.bowlVolume
        )
        soundEngine.applyPreset(preset.name, map)
    }

    private fun triggerGentleHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(45)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.release()
    }
}
