package com.penguenlabs.pushnote.features.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.*
import com.penguenlabs.pushnote.R
import com.penguenlabs.pushnote.navigation.Destination
import com.penguenlabs.pushnote.theme.NeuButton
import com.penguenlabs.pushnote.theme.NeuCard
import com.penguenlabs.pushnote.theme.NeuTopBar
import com.penguenlabs.pushnote.util.Screen

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionScreen(
    pushNotificationText: String = "",
    onBackPressClick: () -> Unit = {},
    onPermissionGranted: (pushNotificationText: String) -> Unit = {}
) {
    Screen(destination = Destination.NotificationPermission) {
        val lifecycleOwner = LocalLifecycleOwner.current
        var isPermissionDenied = rememberSaveable { false }
        val notificationPermissionState: PermissionState? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS,
                    onPermissionResult = { isGranted ->
                        if (isGranted) {
                            onPermissionGranted(pushNotificationText)
                        }
                        isPermissionDenied = !isGranted
                    })
            } else {
                null
            }

        Column {
            NeuTopBar(
                title = stringResource(id = R.string.permission),
                onBackClick = onBackPressClick
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val context = LocalContext.current

                NeuCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.please_turn_on_notifications),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(id = R.string.notification_permission_description),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Start,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                )

                NeuButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    onClick = {
                        when {
                            notificationPermissionState?.status?.isGranted == true -> {
                                onPermissionGranted(pushNotificationText)
                            }

                            (notificationPermissionState?.status?.shouldShowRationale == true) or isPermissionDenied -> {
                                openAppNotificationDeviceSettings(context)
                            }

                            else -> {
                                notificationPermissionState?.launchPermissionRequest()
                            }
                        }
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.allow),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        DisposableEffect(key1 = lifecycleOwner, effect = {
            val lifecycleObserver = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME && notificationPermissionState?.status?.isGranted == true) {
                    onPermissionGranted(pushNotificationText)
                }
            }
            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            }
        })
    }
}

private fun openAppNotificationDeviceSettings(context: Context) {
    val intent = Intent().apply {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }

            else -> {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                addCategory(Intent.CATEGORY_DEFAULT)
                data = Uri.parse("package:" + context.packageName)
            }
        }
    }
    context.startActivity(intent)
}
