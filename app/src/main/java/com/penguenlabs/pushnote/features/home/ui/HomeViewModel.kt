package com.penguenlabs.pushnote.features.home.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.penguenlabs.pushnote.analytics.Event
import com.penguenlabs.pushnote.analytics.EventLogger
import com.penguenlabs.pushnote.data.local.entity.HistoryEntity
import com.penguenlabs.pushnote.data.local.entity.ScheduledNoteEntity
import com.penguenlabs.pushnote.features.home.data.HomeRepository
import com.penguenlabs.pushnote.features.schedule.ScheduleAlarmManager
import com.penguenlabs.pushnote.pushnotification.counter.NotificationCounter
import com.penguenlabs.pushnote.pushnotification.sender.NotificationSender
import com.penguenlabs.pushnote.userdefault.pinnednotification.PinnedNoteUserDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pinnedNoteUserDefault: PinnedNoteUserDefault,
    private val notificationSender: NotificationSender,
    private val homeRepository: HomeRepository,
    private val eventLogger: EventLogger,
    private val notificationCounter: NotificationCounter,
    private val scheduleAlarmManager: ScheduleAlarmManager
) : ViewModel() {

    var homeScreeState by mutableStateOf(
        HomeScreenState(
            isPinnedNote = pinnedNoteUserDefault.getUserDefault()
        )
    )
        private set

    private val _inAppReviewLauncher = MutableSharedFlow<Unit>()
    val inAppReviewLauncher = _inAppReviewLauncher.asSharedFlow()

    private val _dismissRequest = MutableSharedFlow<Unit>()
    val dismissRequest = _dismissRequest.asSharedFlow()

    private val _scheduleResult = MutableSharedFlow<Boolean>()
    val scheduleResult = _scheduleResult.asSharedFlow()

    private val requiredNotificationCountRange = 8..10

    fun onTextFieldValueChange(textFieldValue: String) {
        homeScreeState = homeScreeState.copy(
            textFieldValue = textFieldValue, isError = textFieldValue.isEmpty()
        )
    }

    fun onCheckedChange(isChecked: Boolean) {
        homeScreeState = homeScreeState.copy(isPinnedNote = isChecked)
    }

    fun sendNotification(pushNotificationText: String, isPinnedNote: Boolean) {
        viewModelScope.launch {
            homeScreeState = if (pushNotificationText.isEmpty()) {
                homeScreeState.copy(isError = true)
            } else {
                val isScheduled = homeScreeState.scheduleConfig.isScheduled
                val scheduleConfig = homeScreeState.scheduleConfig

                // For scheduled notes, calculate trigger time and store history as inactive
                val triggerTime: Long
                val historyActive: Boolean
                if (isScheduled) {
                    val tempNote = ScheduledNoteEntity(
                        note = pushNotificationText,
                        hour = scheduleConfig.hour,
                        minute = scheduleConfig.minute,
                        repeatMode = scheduleConfig.repeatMode.name,
                        year = scheduleConfig.year,
                        month = scheduleConfig.month,
                        day = scheduleConfig.day
                    )
                    triggerTime = ScheduleAlarmManager.calculateNextTriggerTime(tempNote)
                    historyActive = false
                } else {
                    triggerTime = System.currentTimeMillis()
                    historyActive = true
                }

                val notificationEntityId: Long = insertHistory(
                    pushNotificationText, isPinnedNote, isScheduled, triggerTime, historyActive
                )

                if (isScheduled) {
                    val scheduledNote = ScheduledNoteEntity(
                        note = pushNotificationText,
                        hour = scheduleConfig.hour,
                        minute = scheduleConfig.minute,
                        repeatMode = scheduleConfig.repeatMode.name,
                        year = scheduleConfig.year,
                        month = scheduleConfig.month,
                        day = scheduleConfig.day
                    )
                    val scheduledNoteId = homeRepository.insertScheduledNote(scheduledNote)
                    val ok = scheduleAlarmManager.schedule(scheduledNote.copy(id = scheduledNoteId))
                    _scheduleResult.emit(ok)
                } else {
                    if (isPinnedNote) {
                        notificationSender.sendPinnedNotification(
                            notificationEntityId = notificationEntityId,
                            pushNotificationText = pushNotificationText
                        )
                    } else {
                        notificationSender.sendNotification(
                            notificationEntityId = notificationEntityId,
                            pushNotificationText = pushNotificationText
                        )
                    }
                }

                logPush(pushNotificationText, isPinnedNote)

                launchInAppReview()

                // Dismiss the dialog after successful push/schedule
                _dismissRequest.emit(Unit)

                homeScreeState.copy(textFieldValue = "", isError = false, scheduleConfig = ScheduleConfig())
            }
        }
    }

    fun updateScreenState() {
        homeScreeState = homeScreeState.copy(isPinnedNote = pinnedNoteUserDefault.getUserDefault())
    }

    private suspend fun insertHistory(
        pushNotificationText: String,
        isPinnedNote: Boolean,
        isScheduled: Boolean,
        timestamp: Long = System.currentTimeMillis(),
        active: Boolean = true
    ): Long {
        return homeRepository.insertHistory(
            HistoryEntity(
                note = pushNotificationText,
                time = timestamp,
                isPinnedNote = isPinnedNote,
                isScheduledNote = isScheduled,
                active = active,
            )
        )
    }

    private fun logPush(pushNotificationText: String, isPinnedNote: Boolean) {
        eventLogger.log(
            Event.Push, bundleOf(
                Pair(Event.Push.PARAM_KEY_PUSH_NOTIFICATION_TEXT, pushNotificationText),
                Pair(Event.Push.PARAM_KEY_IS_PINNED_NOTE, isPinnedNote)
            )
        )
    }

    private fun launchInAppReview() {
        if (requiredNotificationCountRange.contains(notificationCounter.getNotificationCount())) {
            viewModelScope.launch {
                _inAppReviewLauncher.emit(Unit)
            }
            eventLogger.log(Event.InAppReviewLaunched)
        }
    }

    fun onScheduleToggled(isScheduled: Boolean) {
        homeScreeState = homeScreeState.copy(
            scheduleConfig = homeScreeState.scheduleConfig.copy(isScheduled = isScheduled)
        )
    }

    fun onScheduleTimeChanged(hour: Int, minute: Int) {
        homeScreeState = homeScreeState.copy(
            scheduleConfig = homeScreeState.scheduleConfig.copy(hour = hour, minute = minute)
        )
    }

    fun onScheduleDateChanged(year: Int, month: Int, day: Int) {
        homeScreeState = homeScreeState.copy(
            scheduleConfig = homeScreeState.scheduleConfig.copy(year = year, month = month, day = day)
        )
    }

    fun onRepeatModeChanged(repeatMode: RepeatMode) {
        homeScreeState = homeScreeState.copy(
            scheduleConfig = homeScreeState.scheduleConfig.copy(repeatMode = repeatMode)
        )
    }

    fun clearSchedule() {
        homeScreeState = homeScreeState.copy(scheduleConfig = ScheduleConfig())
    }
}
