package com.penguenlabs.pushnote.features.home.ui

enum class RepeatMode { NONE, DAILY, WEEKLY, MONTHLY }

data class ScheduleConfig(
    val isScheduled: Boolean = false,
    val hour: Int = 9,
    val minute: Int = 0,
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    val dayOfWeek: Int? = null,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val systemAlarm: Boolean = false
)

data class HomeScreenState(
    val textFieldValue: String = "",
    val isPinnedNote: Boolean = false,
    val isError: Boolean = false,
    val scheduleConfig: ScheduleConfig = ScheduleConfig()
)
