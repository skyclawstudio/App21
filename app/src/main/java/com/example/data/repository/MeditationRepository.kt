package com.example.data.repository

import com.example.data.dao.MeditationDao
import com.example.data.model.MeditationSession
import com.example.data.model.SessionType
import com.example.data.model.SoundPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

data class UserStats(
    val totalMinutes: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalSessions: Int = 0,
    val weeklyMinutes: Map<Int, Int> = emptyMap() // DayOfWeek (Calendar.SUNDAY..SATURDAY) -> minutes
)

class MeditationRepository(private val dao: MeditationDao) {

    val allSessions: Flow<List<MeditationSession>> = dao.getAllSessions()
    val allPresets: Flow<List<SoundPreset>> = dao.getAllPresets()

    suspend fun insertSession(session: MeditationSession): Long {
        return dao.insertSession(session)
    }

    suspend fun deleteSession(id: Long) {
        dao.deleteSessionById(id)
    }

    suspend fun insertPreset(preset: SoundPreset): Long {
        return dao.insertPreset(preset)
    }

    suspend fun deleteCustomPreset(id: Long) {
        dao.deleteCustomPreset(id)
    }

    suspend fun ensureDefaultPresets() {
        val existing = dao.getAllPresets().first()
        if (existing.isEmpty()) {
            val defaults = listOf(
                SoundPreset(
                    name = "Midnight Rain",
                    rainVolume = 0.85f,
                    oceanVolume = 0.0f,
                    windVolume = 0.35f,
                    cricketsVolume = 0.20f,
                    fireVolume = 0.0f,
                    bowlVolume = 0.15f,
                    isBuiltIn = true
                ),
                SoundPreset(
                    name = "Forest Twilight",
                    rainVolume = 0.10f,
                    oceanVolume = 0.0f,
                    windVolume = 0.40f,
                    cricketsVolume = 0.80f,
                    fireVolume = 0.25f,
                    bowlVolume = 0.20f,
                    isBuiltIn = true
                ),
                SoundPreset(
                    name = "Ocean Sanctuary",
                    rainVolume = 0.0f,
                    oceanVolume = 0.90f,
                    windVolume = 0.30f,
                    cricketsVolume = 0.0f,
                    fireVolume = 0.0f,
                    bowlVolume = 0.40f,
                    isBuiltIn = true
                ),
                SoundPreset(
                    name = "Hearth & Starlight",
                    rainVolume = 0.20f,
                    oceanVolume = 0.0f,
                    windVolume = 0.15f,
                    cricketsVolume = 0.40f,
                    fireVolume = 0.85f,
                    bowlVolume = 0.0f,
                    isBuiltIn = true
                ),
                SoundPreset(
                    name = "Zen Temple",
                    rainVolume = 0.30f,
                    oceanVolume = 0.0f,
                    windVolume = 0.25f,
                    cricketsVolume = 0.30f,
                    fireVolume = 0.0f,
                    bowlVolume = 0.85f,
                    isBuiltIn = true
                )
            )
            dao.insertPresets(defaults)
        }
    }

    fun calculateStats(sessions: List<MeditationSession>): UserStats {
        if (sessions.isEmpty()) return UserStats()

        val totalMinutes = sessions.sumOf { it.durationSeconds } / 60
        val totalSessions = sessions.size

        // Calculate streaks by normalizing timestamps to start-of-day epoch days
        val distinctDays = sessions.map { toDayEpoch(it.timestamp) }.distinct().sortedDescending()
        val todayEpoch = toDayEpoch(System.currentTimeMillis())

        var currentStreak = 0
        var checkEpoch = todayEpoch

        // If no session today, check if yesterday had a session to keep streak alive
        if (distinctDays.contains(todayEpoch)) {
            currentStreak = 1
            checkEpoch = todayEpoch - 1
        } else if (distinctDays.contains(todayEpoch - 1)) {
            currentStreak = 1
            checkEpoch = todayEpoch - 2
        }

        if (currentStreak > 0) {
            while (distinctDays.contains(checkEpoch)) {
                currentStreak++
                checkEpoch--
            }
        }

        // Longest streak
        var bestStreak = 0
        var tempStreak = 0
        var prevEpoch: Long? = null
        for (day in distinctDays.sorted()) {
            if (prevEpoch != null && day == prevEpoch + 1) {
                tempStreak++
            } else {
                tempStreak = 1
            }
            if (tempStreak > bestStreak) {
                bestStreak = tempStreak
            }
            prevEpoch = day
        }

        // Weekly minutes distribution (Sunday to Saturday of current week)
        val cal = Calendar.getInstance()
        val currentWeek = cal.get(Calendar.WEEK_OF_YEAR)
        val currentYear = cal.get(Calendar.YEAR)

        val weeklyMinutes = mutableMapOf<Int, Int>()
        for (day in Calendar.SUNDAY..Calendar.SATURDAY) {
            weeklyMinutes[day] = 0
        }

        for (session in sessions) {
            cal.timeInMillis = session.timestamp
            if (cal.get(Calendar.WEEK_OF_YEAR) == currentWeek && cal.get(Calendar.YEAR) == currentYear) {
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val mins = session.durationSeconds / 60
                weeklyMinutes[dayOfWeek] = (weeklyMinutes[dayOfWeek] ?: 0) + mins
            }
        }

        return UserStats(
            totalMinutes = totalMinutes,
            currentStreak = currentStreak,
            bestStreak = maxOf(bestStreak, currentStreak),
            totalSessions = totalSessions,
            weeklyMinutes = weeklyMinutes
        )
    }

    private fun toDayEpoch(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis / (24 * 60 * 60 * 1000L)
    }
}
