package com.penguenlabs.pushnote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.penguenlabs.pushnote.data.local.entity.ScheduledNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledNoteDao {

    @Insert
    suspend fun insert(scheduledNote: ScheduledNoteEntity): Long

    @Query("SELECT * FROM schedulednoteentity WHERE is_active = 1 ORDER BY created_at DESC")
    fun getAllActive(): Flow<List<ScheduledNoteEntity>>

    @Query("SELECT * FROM schedulednoteentity WHERE id = :id")
    suspend fun getById(id: Long): ScheduledNoteEntity?

    @Query("UPDATE schedulednoteentity SET is_active = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("DELETE FROM schedulednoteentity WHERE id = :id")
    suspend fun delete(id: Long)
}
