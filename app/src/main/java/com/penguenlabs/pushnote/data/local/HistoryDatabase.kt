package com.penguenlabs.pushnote.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.penguenlabs.pushnote.data.local.dao.HistoryDao
import com.penguenlabs.pushnote.data.local.dao.ScheduledNoteDao
import com.penguenlabs.pushnote.data.local.entity.HistoryEntity
import com.penguenlabs.pushnote.data.local.entity.ScheduledNoteEntity

const val DATABASE_NAME = "history"
private const val DATABASE_VERSION = 7

@Suppress("unused")
@Database(
    entities = [HistoryEntity::class, ScheduledNoteEntity::class],
    version = DATABASE_VERSION,
    exportSchema = false
)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun scheduledNoteDao(): ScheduledNoteDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE HistoryEntity ADD COLUMN is_pinned_note INTEGER DEFAULT 0 NOT NULL")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE HistoryEntity ADD COLUMN active INTEGER DEFAULT 0 NOT NULL")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ScheduledNoteEntity (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                note TEXT NOT NULL,
                schedule_hour INTEGER NOT NULL,
                schedule_minute INTEGER NOT NULL,
                repeat_mode TEXT NOT NULL,
                is_active INTEGER NOT NULL DEFAULT 1,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ScheduledNoteEntity ADD COLUMN schedule_year INTEGER")
        database.execSQL("ALTER TABLE ScheduledNoteEntity ADD COLUMN schedule_month INTEGER")
        database.execSQL("ALTER TABLE ScheduledNoteEntity ADD COLUMN schedule_day INTEGER")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE HistoryEntity ADD COLUMN is_scheduled_note INTEGER DEFAULT 0 NOT NULL")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ScheduledNoteEntity ADD COLUMN schedule_day_of_week INTEGER")
    }
}
