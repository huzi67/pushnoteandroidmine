package com.penguenlabs.pushnote.features.home.ui

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mertceyhan.compose.inappreviews.rememberInAppReviewManager
import com.penguenlabs.pushnote.R
import com.penguenlabs.pushnote.navigation.Destination
import com.penguenlabs.pushnote.util.Screen
import com.penguenlabs.pushnote.util.findActivity
import kotlinx.coroutines.delay

private const val FOCUS_REQUEST_DELAY: Long = 300

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    pushNotificationText: String = "",
    onDialogDismissRequest: () -> Unit,
    onSettingsButtonClick: () -> Unit,
    onNotificationPermissionNeed: (pushNotificationText: String) -> Unit
) {
    Screen(destination = Destination.Home, backgroundColor = Color.Transparent) {
        Dialog(
            onDismissRequest = onDialogDismissRequest
        ) {
            val textFieldFocusRequester = remember { FocusRequester() }
            val homeScreeState = homeViewModel.homeScreeState
            val notificationPermissionState: PermissionState? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    rememberPermissionState(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )
                } else {
                    null
                }
            val inAppReviewManager = rememberInAppReviewManager()
            val context = LocalContext.current

            var showScheduleDialog by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(color = MaterialTheme.colorScheme.background)
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.push_note),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(textFieldFocusRequester),
                    value = homeScreeState.textFieldValue,
                    onValueChange = homeViewModel::onTextFieldValueChange,
                    placeholder = {
                        Text(text = stringResource(id = R.string.your_note_goes_here))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        homeViewModel.sendNotification(
                            pushNotificationText = homeScreeState.textFieldValue,
                            isPinnedNote = homeScreeState.isPinnedNote
                        )
                    }),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onBackground),
                    isError = homeScreeState.isError,
                )
                Spacer(
                    modifier = Modifier.height(12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                        val hapticFeedback = LocalHapticFeedback.current

                        Checkbox(checked = homeScreeState.isPinnedNote, onCheckedChange = {
                            homeViewModel.onCheckedChange(it)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        })
                    }
                    Spacer(
                        modifier = Modifier.width(4.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.pinned_note),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    // Schedule button
                    IconButton(onClick = { showScheduleDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(id = R.string.schedule_note),
                            tint = if (homeScreeState.scheduleConfig.isScheduled)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }

                // Show schedule info if scheduled
                if (homeScreeState.scheduleConfig.isScheduled) {
                    val scheduleConfig = homeScreeState.scheduleConfig
                    val timeText = String.format(
                        "%02d:%02d",
                        scheduleConfig.hour,
                        scheduleConfig.minute
                    )
                    val datePart = if (scheduleConfig.year != null && scheduleConfig.month != null && scheduleConfig.day != null) {
                        String.format("%04d-%02d-%02d  ", scheduleConfig.year, scheduleConfig.month + 1, scheduleConfig.day)
                    } else ""
                    val repeatText = when (scheduleConfig.repeatMode) {
                        RepeatMode.NONE -> stringResource(id = R.string.repeat_none)
                        RepeatMode.DAILY -> stringResource(id = R.string.repeat_daily)
                        RepeatMode.WEEKLY -> stringResource(id = R.string.repeat_weekly)
                        RepeatMode.MONTHLY -> stringResource(id = R.string.repeat_monthly)
                    }
                    Text(
                        text = "$datePart$timeText  $repeatText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp), onClick = {
                        if (notificationPermissionState?.status?.isGranted?.not() == true) {
                            onNotificationPermissionNeed(homeScreeState.textFieldValue)
                        } else {
                            homeViewModel.sendNotification(
                                pushNotificationText = homeScreeState.textFieldValue,
                                isPinnedNote = homeScreeState.isPinnedNote
                            )
                        }
                    }, shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (homeScreeState.scheduleConfig.isScheduled)
                            stringResource(id = R.string.schedule_push)
                        else
                            stringResource(id = R.string.push),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(
                    modifier = Modifier.height(8.dp)
                )
                TextButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp),
                    onClick = onSettingsButtonClick,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.settings),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Schedule configuration dialog
            if (showScheduleDialog) {
                ScheduleDialog(
                    scheduleConfig = homeScreeState.scheduleConfig,
                    onDismiss = { showScheduleDialog = false },
                    onConfirm = { hour, minute, repeatMode, year, month, day, isScheduled ->
                        homeViewModel.onScheduleTimeChanged(hour, minute)
                        if (year != null && month != null && day != null) {
                            homeViewModel.onScheduleDateChanged(year, month, day)
                        }
                        homeViewModel.onRepeatModeChanged(repeatMode)
                        homeViewModel.onScheduleToggled(isScheduled)
                        showScheduleDialog = false
                    },
                    onClear = {
                        homeViewModel.clearSchedule()
                        showScheduleDialog = false
                    }
                )
            }

            LaunchedEffect(Unit) {
                homeViewModel.updateScreenState()
                delay(timeMillis = FOCUS_REQUEST_DELAY)
                textFieldFocusRequester.requestFocus()

                if (pushNotificationText.isNotEmpty() or pushNotificationText.isNotBlank()) {
                    homeViewModel.sendNotification(
                        pushNotificationText, homeScreeState.isPinnedNote
                    )
                }
            }

            LaunchedEffect(Unit) {
                homeViewModel.inAppReviewLauncher.collect {
                    inAppReviewManager.launchReviewFlow(context.findActivity())
                }
            }

            LaunchedEffect(Unit) {
                homeViewModel.dismissRequest.collect {
                    onDialogDismissRequest()
                }
            }

            LaunchedEffect(Unit) {
                homeViewModel.scheduleResult.collect { success ->
                    val msg = if (success)
                        context.getString(R.string.schedule_set_success)
                    else
                        context.getString(R.string.schedule_set_failed)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleDialog(
    scheduleConfig: ScheduleConfig,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int, repeatMode: RepeatMode, year: Int?, month: Int?, day: Int?, isScheduled: Boolean) -> Unit,
    onClear: () -> Unit
) {
    var selectedHour by remember { mutableStateOf(scheduleConfig.hour) }
    var selectedMinute by remember { mutableStateOf(scheduleConfig.minute) }
    var selectedRepeatMode by remember { mutableStateOf(scheduleConfig.repeatMode) }
    var selectedYear by remember { mutableStateOf(scheduleConfig.year ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(scheduleConfig.month ?: java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(scheduleConfig.day ?: java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.schedule_note),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Date picker trigger (only for no-repeat mode)
                if (selectedRepeatMode == RepeatMode.NONE) {
                    val dateText = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(id = R.string.schedule_date) + ": $dateText",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Time picker trigger
                val timeText = String.format("%02d:%02d", selectedHour, selectedMinute)
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.schedule_time) + ": $timeText",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Repeat mode selection
                Text(
                    text = stringResource(id = R.string.repeat_mode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedRepeatMode == RepeatMode.NONE,
                        onClick = { selectedRepeatMode = RepeatMode.NONE },
                        label = { Text(stringResource(id = R.string.repeat_none)) }
                    )
                    FilterChip(
                        selected = selectedRepeatMode == RepeatMode.DAILY,
                        onClick = { selectedRepeatMode = RepeatMode.DAILY },
                        label = { Text(stringResource(id = R.string.repeat_daily)) }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedRepeatMode == RepeatMode.WEEKLY,
                        onClick = { selectedRepeatMode = RepeatMode.WEEKLY },
                        label = { Text(stringResource(id = R.string.repeat_weekly)) }
                    )
                    FilterChip(
                        selected = selectedRepeatMode == RepeatMode.MONTHLY,
                        onClick = { selectedRepeatMode = RepeatMode.MONTHLY },
                        label = { Text(stringResource(id = R.string.repeat_monthly)) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedHour, selectedMinute, selectedRepeatMode, selectedYear, selectedMonth, selectedDay, true)
                }
            ) {
                Text(stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) {
                    Text(
                        stringResource(id = R.string.clear_schedule),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        }
    )

    // Time picker dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = {
                Text(
                    text = stringResource(id = R.string.select_time),
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedHour = timePickerState.hour
                        selectedMinute = timePickerState.minute
                        showTimePicker = false
                    }
                ) {
                    Text(stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    // Date picker dialog
    if (showDatePicker) {
        val initialDateMillis = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, selectedYear)
            set(java.util.Calendar.MONTH, selectedMonth)
            set(java.util.Calendar.DAY_OF_MONTH, selectedDay)
        }.timeInMillis

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = java.util.Calendar.getInstance().apply {
                                timeInMillis = millis
                            }
                            selectedYear = calendar.get(java.util.Calendar.YEAR)
                            selectedMonth = calendar.get(java.util.Calendar.MONTH)
                            selectedDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
