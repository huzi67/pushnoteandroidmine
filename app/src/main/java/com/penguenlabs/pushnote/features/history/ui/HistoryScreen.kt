package com.penguenlabs.pushnote.features.history.ui

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.penguenlabs.pushnote.R
import com.penguenlabs.pushnote.data.local.entity.HistoryEntity
import com.penguenlabs.pushnote.navigation.Destination
import com.penguenlabs.pushnote.theme.NeuCard
import com.penguenlabs.pushnote.theme.NeuIconButton
import com.penguenlabs.pushnote.theme.NeuTopBar
import com.penguenlabs.pushnote.theme.neuGreen
import com.penguenlabs.pushnote.theme.neuRed
import com.penguenlabs.pushnote.util.Screen
import com.penguenlabs.pushnote.util.TimeFormat

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel = hiltViewModel(), onBackPressClick: () -> Unit = {}
) {
    val historyScreenState = historyViewModel.historyScreenState
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    Screen(context = context, destination = Destination.History) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.fillMaxSize()) {
                NeuTopBar(
                    title = stringResource(id = R.string.history),
                    onBackClick = onBackPressClick,
                    actions = {
                        AnimatedVisibility(
                            visible = historyScreenState.isSelectable(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row {
                                NeuIconButton(
                                    onClick = {
                                        historyViewModel.onDeleteAllClick(
                                            historyScreenState.selectedHistoryEntities
                                        )
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    painter = painterResource(id = R.drawable.ic_delete_forever),
                                    contentDescription = stringResource(id = R.string.delete),
                                    backgroundColor = neuRed,
                                    contentColor = Color.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                NeuIconButton(
                                    onClick = {
                                        historyViewModel.onSelectAllClick()
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    painter = painterResource(id = R.drawable.ic_select_all),
                                    contentDescription = stringResource(id = R.string.select_all)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                NeuIconButton(
                                    onClick = {
                                        historyViewModel.onDeselectAllClick()
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    painter = painterResource(id = R.drawable.ic_close),
                                    contentDescription = stringResource(id = R.string.close)
                                )
                            }
                        }
                    }
                )
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = historyScreenState.historyItems,
                        key = { it.id }) { historyEntity ->
                        HistoryItem(
                            modifier = Modifier.animateItemPlacement(),
                            historyEntity = historyEntity,
                            isSelectable = historyScreenState.isSelectable(),
                            isSelected = historyScreenState.isSelected(historyEntity),
                            isScheduledActive = historyScreenState.activeScheduledNotes.any { it.note == historyEntity.note },
                            scheduledRepeatMode = historyScreenState.activeScheduledNotes.find { it.note == historyEntity.note }?.repeatMode,
                            onSendClick = historyViewModel::sendNotification,
                            onDeactivateClick = { historyViewModel.deactivateScheduledNote(historyEntity.note) },
                            onLongClick = {
                                historyViewModel.onHistoryEntitySelect(isSelected = true, it)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onCheckedChange = { isSelected, it ->
                                historyViewModel.onHistoryEntitySelect(isSelected, it)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            })
                    }
                }
            }
            AnimatedVisibility(
                visible = historyScreenState.hasHistory().not(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = stringResource(id = R.string.no_history),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
    LaunchedEffect(Unit) {
        historyViewModel.getAllHistory()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@Composable
private fun HistoryItem(
    modifier: Modifier = Modifier,
    isSelectable: Boolean = false,
    isSelected: Boolean = false,
    isScheduledActive: Boolean = false,
    scheduledRepeatMode: String? = null,
    historyEntity: HistoryEntity,
    onSendClick: (note: String, isPinnedNote: Boolean) -> Unit = { _, _ -> },
    onDeactivateClick: () -> Unit = {},
    onLongClick: (historyEntity: HistoryEntity) -> Unit = { },
    onCheckedChange: (isSelected: Boolean, historyEntity: HistoryEntity) -> Unit = { _, _ -> },
) {
    val cardBackground = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
    else MaterialTheme.colorScheme.surface

    NeuCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectable) {
                        onCheckedChange(isSelected.not(), historyEntity)
                    }
                },
                onLongClick = {
                    if (isSelectable.not()) {
                        onLongClick(historyEntity)
                    }
                }
            ),
        backgroundColor = cardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)
                .height(54.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(
                visible = isSelectable,
                enter = fadeIn() + expandIn(),
                exit = fadeOut() + shrinkOut()
            ) {
                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                    val hapticFeedback = LocalHapticFeedback.current

                    Checkbox(
                        modifier = Modifier.padding(end = 12.dp),
                        checked = isSelected,
                        onCheckedChange = {
                            onCheckedChange(it, historyEntity)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.secondary,
                            uncheckedColor = MaterialTheme.colorScheme.outline,
                            checkmarkColor = Color.Black
                        )
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = historyEntity.note,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (historyEntity.isScheduledNote) {
                        Spacer(modifier = Modifier.width(6.dp))
                        val repeatLabel = when (scheduledRepeatMode) {
                            "DAILY" -> "每天"
                            "WEEKLY" -> "每周"
                            "MONTHLY" -> "每月"
                            else -> null
                        }
                        Text(
                            text = if (isScheduledActive && repeatLabel != null) "⏰ $repeatLabel" else "⏰",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isScheduledActive) neuGreen else neuRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = TimeFormat.format(historyEntity.time),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (isScheduledActive) {
                NeuIconButton(
                    onClick = onDeactivateClick,
                    modifier = Modifier.size(42.dp),
                    painter = null,
                    contentDescription = stringResource(id = R.string.close),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                ) {
                    Text("✕", fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
            }
            NeuIconButton(
                onClick = { onSendClick(historyEntity.note, historyEntity.isPinnedNote) },
                modifier = Modifier.size(42.dp),
                painter = painterResource(id = R.drawable.ic_send),
                contentDescription = stringResource(id = R.string.push),
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            )
        }
    }
}
