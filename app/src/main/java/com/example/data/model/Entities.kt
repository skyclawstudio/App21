package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SessionType {
    BREATHWORK,
    TIMED_MEDITATION,
    SOUNDSCAPE_FLOW
}

@Entity(tableName = "meditation_sessions")
data class MeditationSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionType: SessionType,
    val title: String,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val moodBefore: String? = null,
    val moodAfter: String? = null,
    val notes: String? = null
)

@Entity(tableName = "sound_presets")
data class SoundPreset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val rainVolume: Float = 0f,
    val oceanVolume: Float = 0f,
    val windVolume: Float = 0f,
    val cricketsVolume: Float = 0f,
    val fireVolume: Float = 0f,
    val bowlVolume: Float = 0f,
    val isBuiltIn: Boolean = false
)
