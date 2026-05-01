package com.polaris.filmo.feature.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.polaris.filmo.R
import com.polaris.filmo.core.design.FilmoColors
import com.polaris.filmo.core.design.FilmoRadius
import com.polaris.filmo.core.design.FilmoSize
import com.polaris.filmo.core.design.FilmoSpacing
import com.polaris.filmo.core.design.FilmoTextStyle
import com.polaris.filmo.core.renderer.CameraPreviewView
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context.findActivity()
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val canRequestAgain = activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(
                it,
                Manifest.permission.CAMERA
            )
        } ?: false
        viewModel.handleIntent(
            CameraIntent.OnPermissionResult(
                granted = isGranted,
                canRequestAgain = canRequestAgain
            )
        )
    }
    val filmRenderer = viewModel.filmRenderer

    LaunchedEffect(Unit) {
        viewModel.handleIntent(CameraIntent.OnEnterScreen)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                CameraEffect.RequestCameraPermission -> {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }

                CameraEffect.OpenAppSettings -> {
                    context.openAppSettings()
                }

                is CameraEffect.ShowError -> Unit
            }
        }
    }

    LaunchedEffect(state.previewState) {
        if (state.previewState is PreviewState.Loading) {
            viewModel.startPreview(lifecycleOwner)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.handleIntent(CameraIntent.OnResume)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FilmoColors.Background)
    ) {
        CameraPreviewView(
            renderer = filmRenderer,
            selectedFilmType = state.selectedFilm,
            modifier = Modifier.fillMaxSize()
        )

        when (val previewState = state.previewState) {
            PreviewState.Idle,
            PreviewState.Loading -> LoadingState()

            PreviewState.Ready -> Unit

            PreviewState.PermissionRequired -> OverlayPanel(
                title = stringResource(R.string.camera_permission_title),
                message = state.permissionState.permissionMessage(),
                actionText = state.permissionState.permissionActionText(),
                onAction = {
                    viewModel.handleIntent(CameraIntent.OnClickPermissionPrimaryAction)
                }
            )

            is PreviewState.Error -> OverlayPanel(
                title = stringResource(R.string.camera_preview_error_title),
                message = previewState.message,
                actionText = stringResource(R.string.action_retry),
                onAction = { viewModel.handleIntent(CameraIntent.OnRetryPreview) }
            )
        }
    }
}

@Composable
private fun CameraPermissionState.permissionMessage(): String =
    when (this) {
        is CameraPermissionState.Denied ->
            if (canRequestAgain) {
                stringResource(R.string.camera_permission_message)
            } else {
                stringResource(R.string.camera_permission_denied_forever_message)
            }

        CameraPermissionState.Granted,
        CameraPermissionState.Unknown -> stringResource(R.string.camera_permission_message)
    }

@Composable
private fun CameraPermissionState.permissionActionText(): String =
    when (this) {
        is CameraPermissionState.Denied ->
            if (canRequestAgain) {
                stringResource(R.string.action_allow_permission)
            } else {
                stringResource(R.string.action_open_settings)
            }

        CameraPermissionState.Granted,
        CameraPermissionState.Unknown -> stringResource(R.string.action_allow_permission)
    }

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FilmoColors.Background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = FilmoColors.Primary,
            strokeWidth = 2.dp,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun OverlayPanel(
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(FilmoSpacing.Space16),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .background(
                    color = FilmoColors.Surface,
                    shape = RoundedCornerShape(FilmoRadius.Md)
                )
                .padding(FilmoSpacing.Space24),
            verticalArrangement = Arrangement.spacedBy(FilmoSpacing.Space16)
        ) {
            Text(
                text = title,
                color = FilmoColors.TextPrimary,
                style = FilmoTextStyle.Title
            )
            Text(
                text = message,
                color = FilmoColors.TextSecondary,
                style = FilmoTextStyle.Body
            )
            PrimaryButton(
                text = actionText,
                onClick = onAction,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = FilmoSize.TouchTarget),
        shape = RoundedCornerShape(FilmoRadius.Full),
        colors = ButtonDefaults.buttonColors(
            containerColor = FilmoColors.Primary,
            contentColor = FilmoColors.TextOnLight
        ),
        contentPadding = PaddingValues(horizontal = FilmoSpacing.Space16)
    ) {
        Text(
            text = text,
            style = FilmoTextStyle.Label
        )
    }
}

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

private fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
