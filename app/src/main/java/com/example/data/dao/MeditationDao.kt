package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MeditationSession
import com.example.data.model.SoundPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface MeditationDao {

    @Query("SELECT * FROM meditation_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<MeditationSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: MeditationSession): Long

    @Query("DELETE FROM meditation_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT SUM(durationSeconds) FROM meditation_sessions")
    fun getTotalDurationSeconds(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM meditation_sessions")
    fun getTotalSessionCount(): Flow<Int>

    // Sound Presets
    @Query("SELECT * FROM sound_presets ORDER BY isBuiltIn DESC, id ASC")
    fun getAllPresets(): Flow<List<SoundPreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: SoundPreset): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPresets(presets: List<SoundPreset>)

    @Query("DELETE FROM sound_presets WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteCustomPreset(id: Long)
}
