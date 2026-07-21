package com.penguenlabs.pushnote.features.home.ui

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mertceyhan.compose.inappreviews.rememberInAppReviewManager
import com.penguenlabs.pushnote.R
import com.penguenlabs.pushnote.navigation.Destination
import com.penguenlabs.pushnote.theme.NeuButton
import com.penguenlabs.pushnote.theme.NeuCard
import com.penguenlabs.pushnote.theme.NeuFilterChip
import com.penguenlabs.pushnote.theme.NeuIconButton
import com.penguenlabs.pushnote.theme.NeuOutlinedButton
import com.penguenlabs.pushnote.theme.NeuTextField
import com.penguenlabs.pushnote.theme.neuRed
import com.penguenlabs.pushnote.util.Screen
import com.penguenlabs.pushnote.util.findActivity
import kotlinx.coroutines.delay

private const val FOCUS_REQUEST_DELAY: Long = 300
private val SCHEDULE_RED = neuRed

private val repeatChips = listOf(
    RepeatMode.NONE to R.string.repeat_none,
    RepeatMode.DAILY to R.string.repeat_daily,
    RepeatMode.WEEKLY to R.string.repeat_weekly,
    RepeatMode.MONTHLY to R.string.repeat_monthly
)

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
            var showScheduleConfig by remember { mutableStateOf(false) }

            // Schedule config state (local to avoid affecting main state until confirmed)
            var cfgHour by remember { mutableStateOf(homeScreeState.scheduleConfig.hour) }
            var cfgMinute by remember { mutableStateOf(homeScreeState.scheduleConfig.minute) }
            var cfgRepeat by remember { mutableStateOf(homeScreeState.scheduleConfig.repeatMode) }
            var cfgYear by remember { mutableStateOf(homeScreeState.scheduleConfig.year) }
            var cfgMonth by remember { mutableStateOf(homeScreeState.scheduleConfig.month) }
            var cfgDay by remember { mutableStateOf(homeScreeState.scheduleConfig.day) }
            var cfgDayOfWeek by remember { mutableStateOf(homeScreeState.scheduleConfig.dayOfWeek) }
            var showTimePicker by remember { mutableStateOf(false) }
            var showDatePicker by remember { mutableStateOf(false) }

            NeuCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                backgroundColor = MaterialTheme.colorScheme.background,
                borderColor = MaterialTheme.colorScheme.outline,
                shadowColor = MaterialTheme.colorScheme.outline
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Title
                    Text(
                        text = if (showScheduleConfig) stringResource(id = R.string.schedule_note)
                        else stringResource(id = R.string.push_note),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // TextField
                    NeuTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(textFieldFocusRequester),
                        value = homeScreeState.textFieldValue,
                        onValueChange = homeViewModel::onTextFieldValueChange,
                        placeholder = {
                            Text(
                                stringResource(id = R.string.your_note_goes_here),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            homeViewModel.sendNotification(homeScreeState.textFieldValue, true)
                        }),
                        isError = homeScreeState.isError
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Alarm icon — toggle schedule panel
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NeuIconButton(
                            onClick = {
                                if (showScheduleConfig) {
                                    homeViewModel.clearSchedule()
                                    showScheduleConfig = false
                                } else {
                                    val hasExisting = homeScreeState.scheduleConfig.isScheduled
                                    val now = java.util.Calendar.getInstance()
                                    cfgHour = if (hasExisting) homeScreeState.scheduleConfig.hour else now.get(java.util.Calendar.HOUR_OF_DAY)
                                    cfgMinute = if (hasExisting) homeScreeState.scheduleConfig.minute else now.get(java.util.Calendar.MINUTE)
                                    cfgRepeat = homeScreeState.scheduleConfig.repeatMode
                                    cfgYear = if (hasExisting) homeScreeState.scheduleConfig.year else now.get(java.util.Calendar.YEAR)
                                    cfgMonth = if (hasExisting) homeScreeState.scheduleConfig.month else now.get(java.util.Calendar.MONTH)
                                    cfgDay = if (hasExisting) homeScreeState.scheduleConfig.day else now.get(java.util.Calendar.DAY_OF_MONTH)
                                    cfgDayOfWeek = homeScreeState.scheduleConfig.dayOfWeek
                                    showScheduleConfig = true
                                }
                            },
                            backgroundColor = if (showScheduleConfig || homeScreeState.scheduleConfig.isScheduled) SCHEDULE_RED
                            else MaterialTheme.colorScheme.surface,
                            contentColor = if (showScheduleConfig || homeScreeState.scheduleConfig.isScheduled) Color.Black
                            else MaterialTheme.colorScheme.onBackground,
                            borderColor = MaterialTheme.colorScheme.outline
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_alarm),
                                contentDescription = stringResource(id = R.string.schedule_note),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.schedule_note),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (showScheduleConfig || homeScreeState.scheduleConfig.isScheduled) SCHEDULE_RED
                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Schedule config — expand first, then content slides in
                    AnimatedVisibility(
                        visible = showScheduleConfig,
                        enter = expandVertically(tween(280)) +
                            fadeIn(tween(300, delayMillis = 180)) +
                            slideInVertically(tween(300, delayMillis = 180)) { it / 6 },
                        exit = fadeOut(tween(150)) +
                            slideOutVertically(tween(150)) { -it / 6 } +
                            shrinkVertically(tween(220, delayMillis = 80))
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))

                            // Date + time buttons
                            if (cfgRepeat == RepeatMode.NONE || cfgRepeat == RepeatMode.MONTHLY) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val dateText = String.format("%02d-%02d", (cfgMonth ?: 0) + 1, cfgDay ?: 1)
                                    NeuOutlinedButton(
                                        onClick = { showDatePicker = true },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            dateText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    val timeText = String.format("%02d:%02d", cfgHour, cfgMinute)
                                    NeuOutlinedButton(
                                        onClick = { showTimePicker = true },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            timeText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            } else {
                                val timeText = String.format("%02d:%02d", cfgHour, cfgMinute)
                                NeuOutlinedButton(
                                    onClick = { showTimePicker = true },
                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        timeText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            // Repeat chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                repeatChips.forEach { (mode, label) ->
                                    NeuFilterChip(
                                        selected = cfgRepeat == mode,
                                        onClick = { cfgRepeat = mode },
                                        label = { Text(stringResource(label), fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Weekly day-of-week
                            if (cfgRepeat == RepeatMode.WEEKLY) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    listOf("一", "二", "三", "四", "五", "六", "日").forEachIndexed { i, l ->
                                        NeuFilterChip(
                                            selected = cfgDayOfWeek == i,
                                            onClick = { cfgDayOfWeek = i },
                                            label = { Text(l, fontSize = 11.sp) },
                                            modifier = Modifier.height(30.dp).weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Schedule info (always visible when configured)
                    if (homeScreeState.scheduleConfig.isScheduled) {
                        val sc = homeScreeState.scheduleConfig
                        val timeText = String.format("%02d:%02d", sc.hour, sc.minute)
                        val datePart = if (sc.year != null && sc.month != null && sc.day != null)
                            String.format("%04d-%02d-%02d  ", sc.year, sc.month + 1, sc.day) else ""
                        val repeatText = when (sc.repeatMode) {
                            RepeatMode.NONE -> stringResource(id = R.string.repeat_none)
                            RepeatMode.DAILY -> stringResource(id = R.string.repeat_daily)
                            RepeatMode.WEEKLY -> stringResource(id = R.string.repeat_weekly)
                            RepeatMode.MONTHLY -> stringResource(id = R.string.repeat_monthly)
                        }
                        Text(
                            text = "$datePart$timeText  $repeatText",
                            style = MaterialTheme.typography.bodySmall,
                            color = SCHEDULE_RED,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary button
                    NeuButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        onClick = {
                            if (showScheduleConfig) {
                                homeViewModel.onScheduleTimeChanged(cfgHour, cfgMinute)
                                val y = cfgYear; val m = cfgMonth; val d = cfgDay
                                if (y != null && m != null && d != null) homeViewModel.onScheduleDateChanged(y, m, d)
                                cfgDayOfWeek?.let { homeViewModel.onScheduleDayOfWeekChanged(it) }
                                homeViewModel.onRepeatModeChanged(cfgRepeat)
                                homeViewModel.onScheduleToggled(true)
                                showScheduleConfig = false
                            } else if (notificationPermissionState?.status?.isGranted?.not() == true) {
                                onNotificationPermissionNeed(homeScreeState.textFieldValue)
                            } else {
                                homeViewModel.sendNotification(
                                    pushNotificationText = homeScreeState.textFieldValue,
                                    isPinnedNote = true
                                )
                            }
                        }
                    ) {
                        Text(
                            text = if (showScheduleConfig) stringResource(id = R.string.confirm)
                            else if (homeScreeState.scheduleConfig.isScheduled) stringResource(id = R.string.schedule_push)
                            else stringResource(id = R.string.push),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary button
                    NeuOutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        onClick = onSettingsButtonClick
                    ) {
                        Text(
                            text = stringResource(id = R.string.menu),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Time picker dialog
            if (showTimePicker) {
                Dialog(onDismissRequest = { showTimePicker = false }) {
                    AnimatedVisibility(
                        visible = true,
                        enter = expandVertically(tween(250)) + fadeIn(tween(250)),
                        exit = fadeOut()
                    ) {
                        NeuCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colorScheme.background
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                val tp = rememberTimePickerState(cfgHour, cfgMinute, true)
                                TimePicker(state = tp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { showTimePicker = false }) {
                                        Text(
                                            stringResource(id = R.string.cancel),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    NeuButton(
                                        modifier = Modifier
                                            .width(90.dp)
                                            .height(40.dp),
                                        onClick = {
                                            cfgHour = tp.hour
                                            cfgMinute = tp.minute
                                            showTimePicker = false
                                        }
                                    ) {
                                        Text(
                                            stringResource(id = R.string.confirm),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Date picker dialog
            if (showDatePicker) {
                val init = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.YEAR, cfgYear ?: 2026)
                    set(java.util.Calendar.MONTH, cfgMonth ?: 0)
                    set(java.util.Calendar.DAY_OF_MONTH, cfgDay ?: 1)
                }.timeInMillis
                val dp = rememberDatePickerState(
                    initialSelectedDateMillis = init,
                    selectableDates = object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                            val today = java.util.Calendar.getInstance().apply {
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }.timeInMillis
                            return utcTimeMillis >= today
                        }
                        override fun isSelectableYear(year: Int): Boolean = true
                    }
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
                    confirmButton = {
                        NeuButton(
                            modifier = Modifier
                                .width(90.dp)
                                .height(40.dp),
                            onClick = {
                                dp.selectedDateMillis?.let {
                                    val c = java.util.Calendar.getInstance().apply { timeInMillis = it }
                                    cfgYear = c.get(java.util.Calendar.YEAR)
                                    cfgMonth = c.get(java.util.Calendar.MONTH)
                                    cfgDay = c.get(java.util.Calendar.DAY_OF_MONTH)
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text(
                                stringResource(id = R.string.confirm),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(
                                stringResource(id = R.string.cancel),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                ) {
                    DatePicker(state = dp)
                }
            }

            LaunchedEffect(Unit) {
                homeViewModel.updateScreenState()
                delay(timeMillis = FOCUS_REQUEST_DELAY)
                textFieldFocusRequester.requestFocus()
                if (pushNotificationText.isNotEmpty() or pushNotificationText.isNotBlank()) {
                    homeViewModel.sendNotification(
                        pushNotificationText, true
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
