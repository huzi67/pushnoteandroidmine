package com.penguenlabs.pushnote.features.home.data

import com.penguenlabs.pushnote.data.local.entity.HistoryEntity
import com.penguenlabs.pushnote.data.local.entity.ScheduledNoteEntity
import com.penguenlabs.pushnote.features.home.data.local.HomeLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val homeLocalDataSource: HomeLocalDataSource
) {

    suspend fun insertHistory(historyEntity: HistoryEntity): Long = withContext(Dispatchers.IO) {
        homeLocalDataSource.insertHistory(historyEntity)
    }

    suspend fun insertScheduledNote(scheduledNote: ScheduledNoteEntity): Long =
        withContext(Dispatchers.IO) {
            homeLocalDataSource.insertScheduledNote(scheduledNote)
        }

    suspend fun deleteScheduledNote(id: Long) = withContext(Dispatchers.IO) {
        homeLocalDataSource.deleteScheduledNote(id)
    }
}
