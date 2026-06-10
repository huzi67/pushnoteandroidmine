package com.penguenlabs.pushnote.features.home.data.local

import com.penguenlabs.pushnote.data.local.dao.HistoryDao
import com.penguenlabs.pushnote.data.local.dao.ScheduledNoteDao
import com.penguenlabs.pushnote.data.local.entity.HistoryEntity
import com.penguenlabs.pushnote.data.local.entity.ScheduledNoteEntity
import javax.inject.Inject

class HomeLocalDataSource @Inject constructor(
    private val historyDao: HistoryDao,
    private val scheduledNoteDao: ScheduledNoteDao
) {

    suspend fun insertHistory(historyEntity: HistoryEntity): Long =
        historyDao.insertHistory(historyEntity)

    suspend fun insertScheduledNote(scheduledNote: ScheduledNoteEntity): Long =
        scheduledNoteDao.insert(scheduledNote)

    suspend fun deleteScheduledNote(id: Long) =
        scheduledNoteDao.delete(id)
}
