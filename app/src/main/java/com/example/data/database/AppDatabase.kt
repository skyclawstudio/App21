package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.dao.MeditationDao
import com.example.data.model.MeditationSession
import com.example.data.model.SessionType
import com.example.data.model.SoundPreset

class Converters {
    @TypeConverter
    fun fromSessionType(value: SessionType): String = value.name

    @TypeConverter
    fun toSessionType(value: String): SessionType = runCatching {
        SessionType.valueOf(value)
    }.getOrDefault(SessionType.TIMED_MEDITATION)
}

@Database(
    entities = [MeditationSession::class, SoundPreset::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun meditationDao(): MeditationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "serene_meditation.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
