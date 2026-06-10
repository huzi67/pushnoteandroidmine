package com.penguenlabs.pushnote.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ScheduledNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val note: String,
    @ColumnInfo(name = "schedule_hour") val hour: Int,
    @ColumnInfo(name = "schedule_minute") val minute: Int,
    @ColumnInfo(name = "repeat_mode") val repeatMode: String,
    @ColumnInfo(name = "schedule_year") val year: Int? = null,
    @ColumnInfo(name = "schedule_month") val month: Int? = null,
    @ColumnInfo(name = "schedule_day") val day: Int? = null,
    @ColumnInfo(name = "schedule_day_of_week") val dayOfWeek: Int? = null,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
