package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

enum class SoundChannel(val displayName: String, val iconEmoji: String) {
    RAIN("Rain on Canopy", "🌧️"),
    OCEAN("Ocean Swells", "🌊"),
    WIND("Mountain Breeze", "🍃"),
    CRICKETS("Night Forest", "🦗"),
    FIRE("Ember Hearth", "🔥"),
    BOWL("Singing Bowl", "🔔")
}

data class SoundMixerState(
    val isPlaying: Boolean = false,
    val masterVolume: Float = 0.8f,
    val channelVolumes: Map<SoundChannel, Float> = mapOf(
        SoundChannel.RAIN to 0.7f,
        SoundChannel.OCEAN to 0.0f,
        SoundChannel.WIND to 0.3f,
        SoundChannel.CRICKETS to 0.2f,
        SoundChannel.FIRE to 0.0f,
        SoundChannel.BOWL to 0.15f
    ),
    val activePresetName: String? = "Midnight Rain",
    val sleepTimerRemainingSeconds: Int? = null
)

class AmbientSoundEngine(private val scope: CoroutineScope) {

    private val _mixerState = MutableStateFlow(SoundMixerState())
    val mixerState: StateFlow<SoundMixerState> = _mixerState.asStateFlow()

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private var sleepTimerJob: Job? = null

    private val sampleRate = 22050
    private val bufferSize = 2048 // ~92ms chunks

    // Random generators and filter states
    private val rng = Random()

    // Rain filter states (Paul Kellet's filter)
    private var b0 = 0.0; private var b1 = 0.0; private var b2 = 0.0
    private var b3 = 0.0; private var b4 = 0.0; private var b5 = 0.0; private var b6 = 0.0

    // Ocean brown noise state & swell phase
    private var brownNoise = 0.0
    private var oceanPhase = 0.0

    // Wind filter states
    private var windFilterState = 0.0
    private var windLfoPhase = 0.0

    // Crickets chirp phase & phrase timing
    private var cricketPhase = 0.0
    private var cricketChirpSampleCounter = 0

    // Fire crackle burst timer
    private var fireCrackleCountdown = 0
    private var fireCrackleAmp = 0.0

    // Singing bowl strike phase & decay
    private var bowlPhase = 0.0
    private var bowlEnvelope = 0.0
    private var bowlTriggerCountdown = 0

    // One-shot interval bell
    private var bellTriggered = false
    private var bellPhase = 0.0
    private var bellEnvelope = 0.0

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val trackBuf = maxOf(minBuf * 2, bufferSize * 4)

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(format)
            .setBufferSizeInBytes(trackBuf)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    }

    fun play() {
        if (_mixerState.value.isPlaying) return

        _mixerState.value = _mixerState.value.copy(isPlaying = true)
        try {
            audioTrack?.play()
        } catch (_: Exception) {
            initAudioTrack()
            audioTrack?.play()
        }

        startAudioLoop()
    }

    fun pause() {
        _mixerState.value = _mixerState.value.copy(isPlaying = false)
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (_: Exception) {}
    }

    fun togglePlayPause() {
        if (_mixerState.value.isPlaying) pause() else play()
    }

    fun setMasterVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _mixerState.value = _mixerState.value.copy(masterVolume = clamped)
    }

    fun setChannelVolume(channel: SoundChannel, volume: Float) {
        val current = _mixerState.value.channelVolumes.toMutableMap()
        current[channel] = volume.coerceIn(0f, 1f)
        _mixerState.value = _mixerState.value.copy(
            channelVolumes = current,
            activePresetName = null
        )
        if (!_mixerState.value.isPlaying && volume > 0f) {
            play()
        }
    }

    fun applyPreset(name: String, volumes: Map<SoundChannel, Float>) {
        _mixerState.value = _mixerState.value.copy(
            channelVolumes = volumes,
            activePresetName = name
        )
        if (!_mixerState.value.isPlaying) {
            play()
        }
    }

    fun setSleepTimerMinutes(minutes: Int?) {
        sleepTimerJob?.cancel()
        sleepTimerJob = null

        if (minutes == null || minutes <= 0) {
            _mixerState.value = _mixerState.value.copy(sleepTimerRemainingSeconds = null)
            return
        }

        val totalSeconds = minutes * 60
        _mixerState.value = _mixerState.value.copy(sleepTimerRemainingSeconds = totalSeconds)

        sleepTimerJob = scope.launch(Dispatchers.Default) {
            var remaining = totalSeconds
            while (isActive && remaining > 0) {
                delay(1000L)
                remaining--
                _mixerState.value = _mixerState.value.copy(sleepTimerRemainingSeconds = remaining)

                // Fade out in last 30 seconds
                if (remaining in 1..30) {
                    val fadeFactor = remaining / 30f
                    // smoothly scale master volume
                    audioTrack?.setVolume(fadeFactor * _mixerState.value.masterVolume)
                }
            }
            if (isActive) {
                pause()
                audioTrack?.setVolume(_mixerState.value.masterVolume)
                _mixerState.value = _mixerState.value.copy(sleepTimerRemainingSeconds = null)
            }
        }
    }

    fun triggerBellChime() {
        bellTriggered = true
        bellPhase = 0.0
        bellEnvelope = 1.0
    }

    private fun startAudioLoop() {
        playbackJob?.cancel()
        playbackJob = scope.launch(Dispatchers.Default) {
            val pcmBuffer = ShortArray(bufferSize)

            while (isActive && _mixerState.value.isPlaying) {
                val state = _mixerState.value
                val masterVol = state.masterVolume
                val rainVol = state.channelVolumes[SoundChannel.RAIN] ?: 0f
                val oceanVol = state.channelVolumes[SoundChannel.OCEAN] ?: 0f
                val windVol = state.channelVolumes[SoundChannel.WIND] ?: 0f
                val cricketsVol = state.channelVolumes[SoundChannel.CRICKETS] ?: 0f
                val fireVol = state.channelVolumes[SoundChannel.FIRE] ?: 0f
                val bowlVol = state.channelVolumes[SoundChannel.BOWL] ?: 0f

                for (i in 0 until bufferSize) {
                    var sampleSum = 0.0

                    // 1. RAIN SYNTHESIS (Pink noise with water droplets)
                    if (rainVol > 0.01f) {
                        val white = (rng.nextDouble() * 2.0 - 1.0)
                        b0 = 0.99886 * b0 + white * 0.0555179
                        b1 = 0.99332 * b1 + white * 0.0750759
                        b2 = 0.96900 * b2 + white * 0.1538520
                        b3 = 0.86650 * b3 + white * 0.3104856
                        b4 = 0.55000 * b4 + white * 0.5329522
                        b5 = -0.7616 * b5 - white * 0.0168980
                        val pink = (b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362) * 0.11
                        b6 = white * 0.115926

                        // Random soft droplet click
                        var drop = 0.0
                        if (rng.nextInt(2500) == 0) {
                            drop = (rng.nextDouble() - 0.5) * 0.7
                        }
                        sampleSum += (pink + drop) * rainVol
                    }

                    // 2. OCEAN WAVES SYNTHESIS (Brownian noise with slow 0.12 Hz swell cycle)
                    if (oceanVol > 0.01f) {
                        val white = (rng.nextDouble() * 2.0 - 1.0)
                        brownNoise = (brownNoise + (0.03 * white)) / 1.02
                        oceanPhase += (2.0 * PI * 0.11) / sampleRate
                        if (oceanPhase > 2.0 * PI) oceanPhase -= 2.0 * PI

                        val swell = (sin(oceanPhase) * 0.5 + 0.5) * (sin(oceanPhase * 0.33) * 0.2 + 0.8)
                        sampleSum += (brownNoise * 3.5 * swell) * oceanVol
                    }

                    // 3. MOUNTAIN BREEZE (Fluctuating filtered wind noise)
                    if (windVol > 0.01f) {
                        windLfoPhase += (2.0 * PI * 0.08) / sampleRate
                        if (windLfoPhase > 2.0 * PI) windLfoPhase -= 2.0 * PI

                        val lfo = (sin(windLfoPhase) * 0.5 + 0.5) * (cos(windLfoPhase * 0.41) * 0.3 + 0.7)
                        val white = (rng.nextDouble() * 2.0 - 1.0)
                        val cutoffCoeff = 0.02 + 0.06 * lfo
                        windFilterState += (white - windFilterState) * cutoffCoeff
                        sampleSum += (windFilterState * 2.2 * (0.4 + 0.6 * lfo)) * windVol
                    }

                    // 4. NIGHT FOREST / CRICKETS (4.6 kHz modulated chirps with pauses)
                    if (cricketsVol > 0.01f) {
                        cricketChirpSampleCounter++
                        val phraseCycle = sampleRate * 2 // 2-second repeat
                        val posInPhrase = cricketChirpSampleCounter % phraseCycle

                        // Chirp during first 600ms of each cycle in rapid pulses
                        var chirpAmp = 0.0
                        if (posInPhrase < (sampleRate * 0.6)) {
                            val subPulse = (posInPhrase % (sampleRate / 14)) < (sampleRate / 28)
                            if (subPulse) {
                                chirpAmp = 0.35
                            }
                        }

                        if (chirpAmp > 0.0) {
                            cricketPhase += (2.0 * PI * 4650.0) / sampleRate
                            val cricketWave = sin(cricketPhase) + 0.3 * sin(cricketPhase * 2.0)
                            sampleSum += (cricketWave * chirpAmp) * cricketsVol
                        }
                    }

                    // 5. EMBER HEARTH / CAMPFIRE (Low warmth rumble + snap crackles)
                    if (fireVol > 0.01f) {
                        // Low rumble
                        val white = (rng.nextDouble() * 2.0 - 1.0)
                        val fireLow = white * 0.12

                        // Random crackle pop
                        if (fireCrackleCountdown <= 0) {
                            if (rng.nextInt(600) == 0) {
                                fireCrackleCountdown = rng.nextInt(350) + 50
                                fireCrackleAmp = (rng.nextDouble() * 0.8 + 0.2) * (if (rng.nextBoolean()) 1.0 else -1.0)
                            }
                        } else {
                            fireCrackleCountdown--
                            fireCrackleAmp *= 0.985
                        }

                        sampleSum += (fireLow + fireCrackleAmp) * fireVol
                    }

                    // 6. TIBETAN SINGING BOWL (432 Hz resonant bell with harmonic ring)
                    if (bowlVol > 0.01f) {
                        bowlTriggerCountdown--
                        if (bowlTriggerCountdown <= 0) {
                            bowlTriggerCountdown = sampleRate * 9 // re-ring every 9s
                            bowlEnvelope = 1.0
                            bowlPhase = 0.0
                        }

                        if (bowlEnvelope > 0.001) {
                            bowlPhase += (2.0 * PI * 432.0) / sampleRate
                            // 432 Hz fundamental + 864 Hz octave + 1296 Hz 3rd harmonic
                            val bellSound = (sin(bowlPhase) * 0.7 +
                                    sin(bowlPhase * 2.004) * 0.35 +
                                    sin(bowlPhase * 3.01) * 0.15)
                            sampleSum += (bellSound * bowlEnvelope * 0.5) * bowlVol
                            bowlEnvelope *= 0.999975 // gentle decay
                        }
                    }

                    // 7. INTERVAL GONG BELL (528 Hz crystal chime)
                    if (bellTriggered && bellEnvelope > 0.001) {
                        bellPhase += (2.0 * PI * 528.0) / sampleRate
                        val gongSound = (sin(bellPhase) * 0.75 +
                                sin(bellPhase * 2.76) * 0.3 +
                                sin(bellPhase * 5.4) * 0.12)
                        sampleSum += (gongSound * bellEnvelope * 0.7)
                        bellEnvelope *= 0.99994
                    } else if (bellEnvelope <= 0.001) {
                        bellTriggered = false
                    }

                    // Master gain & soft limiter
                    val output = (sampleSum * masterVol).coerceIn(-1.0, 1.0)
                    pcmBuffer[i] = (output * 32767.0).toInt().toShort()
                }

                audioTrack?.write(pcmBuffer, 0, bufferSize)
            }
        }
    }

    fun release() {
        pause()
        try {
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
