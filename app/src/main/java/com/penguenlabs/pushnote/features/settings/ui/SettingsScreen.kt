package com.penguenlabs.pushnote.features.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
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
import com.penguenlabs.pushnote.BuildConfig
import com.penguenlabs.pushnote.R
import com.penguenlabs.pushnote.navigation.Destination
import com.penguenlabs.pushnote.theme.NeuCard
import com.penguenlabs.pushnote.theme.NeuSwitch
import com.penguenlabs.pushnote.theme.NeuTopBar
import com.penguenlabs.pushnote.util.Screen

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onDarkModeChange: (Boolean) -> Unit,
    onHistoryClick: () -> Unit,
    onBackPressClick: () -> Unit,
) {
    val context = LocalContext.current

    Screen(context = context, destination = Destination.Settings) {
        Column {
            val scrollableState = rememberScrollState()
            val settingsScreenState = settingsViewModel.settingsScreenState

            NeuTopBar(
                title = stringResource(id = R.string.settings),
                onBackClick = onBackPressClick
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollableState)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    text = stringResource(id = R.string.general_settings),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start
                )

                SettingItem(
                    settingTitle = stringResource(id = R.string.history),
                    settingDescription = stringResource(id = R.string.your_previous_notes),
                    settingIcon = painterResource(id = R.drawable.ic_history),
                    onItemClick = onHistoryClick
                )
                Spacer(modifier = Modifier.height(12.dp))

                SettingSwitchableItem(
                    settingTitle = stringResource(id = R.string.dark_mode),
                    settingDescription = stringResource(id = R.string.enable_dark_mode),
                    settingIcon = painterResource(id = R.drawable.ic_night_mode),
                    isChecked = settingsScreenState.darkModeEnabled,
                    onCheckedChange = {
                        settingsViewModel.setDarkModeUserDefault(it)
                        onDarkModeChange(it)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))

                SettingSwitchableItem(
                    settingTitle = stringResource(id = R.string.default_pinned_note),
                    settingDescription = stringResource(id = R.string.select_pinned_note_by_default),
                    settingIcon = painterResource(id = R.drawable.ic_checkbox),
                    isChecked = settingsScreenState.defaultPinnedNoteEnabled,
                    onCheckedChange = settingsViewModel::setPinnedNoteUserDefault
                )
                Spacer(modifier = Modifier.height(12.dp))

                SettingItem(
                    settingTitle = stringResource(id = R.string.version, BuildConfig.VERSION_NAME),
                    settingDescription = stringResource(id = R.string.current_application_version),
                    settingIcon = painterResource(id = R.drawable.ic_code),
                    isClickable = false
                )
            }
        }
    }
}

@Composable
private fun SettingItem(
    modifier: Modifier = Modifier,
    settingTitle: String,
    settingDescription: String,
    settingIcon: Painter,
    isClickable: Boolean = true,
    onItemClick: () -> Unit = {}
) {
    NeuCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick, enabled = isClickable)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = settingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = settingTitle,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = settingDescription,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SettingSwitchableItem(
    modifier: Modifier = Modifier,
    settingTitle: String,
    settingDescription: String,
    settingIcon: Painter,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    val hapticFeedback = LocalHapticFeedback.current

    NeuCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = settingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = settingTitle,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = settingDescription,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            NeuSwitch(
                checked = isChecked,
                onCheckedChange = {
                    onCheckedChange(it)
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )
        }
    }
}
