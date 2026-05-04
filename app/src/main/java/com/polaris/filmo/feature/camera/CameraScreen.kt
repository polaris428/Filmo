package com.polaris.filmo.feature.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CameraScreen(
    onOpenGallery: () -> Unit,
    onOpenFilmSelection: (String) -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
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
    val selectedFilmUi = remember(context, state.selectedFilm) {
        state.selectedFilm.toUiModel(context)
    }
    var previewSize by remember { androidx.compose.runtime.mutableStateOf(IntSize.Zero) }

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

                CameraEffect.ShowFocusError -> {
                    Toast.makeText(context, "Focus could not be adjusted.", Toast.LENGTH_SHORT)
                        .show()
                }

                CameraEffect.PlayShutterFeedback -> {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }

                is CameraEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                is CameraEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
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
            onSurfaceReady = {
                viewModel.startPreview(lifecycleOwner, force = true)
            },
            onViewDisposed = {
                viewModel.handleIntent(CameraIntent.OnPreviewViewDisposed)
            },
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { previewSize = it }
                .pointerInput(state.previewState) {
                    detectTapGestures { offset ->
                        if (state.previewState is PreviewState.Ready &&
                            size.width > 0 &&
                            size.height > 0
                        ) {
                            viewModel.handleIntent(
                                CameraIntent.OnTapPreview(
                                    x = offset.x / size.width,
                                    y = offset.y / size.height
                                )
                            )
                        }
                    }
                }
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

        if (state.previewState is PreviewState.Ready) {
            CircleIconButton(
                enabled = true,
                onClick = onOpenGallery,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        top = FilmoSpacing.Space32,
                        start = FilmoSpacing.Space16
                    )
            ) {
                GalleryIcon()
            }

            CircleIconButton(
                enabled = !state.isSwitchingCamera,
                onClick = { viewModel.handleIntent(CameraIntent.OnSwitchCamera) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = FilmoSpacing.Space32,
                        end = FilmoSpacing.Space16
                    )
            ) {
                CameraSwitchIcon()
            }

            FilmPickerButton(
                filmName = selectedFilmUi.displayName,
                applying = state.isApplyingFilm,
                onClick = { onOpenFilmSelection(state.selectedFilmId) },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = FilmoSpacing.Space16,
                        bottom = FilmoSpacing.Space32
                    )
            )

            FocusRing(
                focusPoint = state.focusPoint,
                previewSize = previewSize,
                modifier = Modifier.fillMaxSize()
            )

            CaptureFlash(
                visible = state.captureFeedbackState is CaptureFeedbackState.Flash,
                onFinished = {
                    viewModel.handleIntent(CameraIntent.OnCaptureFeedbackFinished)
                },
                modifier = Modifier.fillMaxSize()
            )

            ShutterButton(
                capturing = state.isCapturing,
                onClick = { viewModel.handleIntent(CameraIntent.OnClickShutter) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 156.dp)
            )

            SaveStatusToast(
                saveStatus = state.saveStatus,
                onRetry = { viewModel.handleIntent(CameraIntent.OnRetrySave) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 244.dp)
            )
        }
    }
}

@Composable
private fun FilmPickerButton(
    filmName: String,
    applying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !applying,
        modifier = modifier.heightIn(min = FilmoSize.TouchTarget),
        shape = RoundedCornerShape(FilmoRadius.Full),
        colors = ButtonDefaults.buttonColors(
            containerColor = FilmoColors.SurfaceOverlay,
            contentColor = FilmoColors.TextPrimary,
            disabledContainerColor = FilmoColors.SurfaceOverlayLight,
            disabledContentColor = FilmoColors.TextSecondary
        ),
        contentPadding = PaddingValues(horizontal = FilmoSpacing.Space16)
    ) {
        if (applying) {
            CircularProgressIndicator(
                color = FilmoColors.Primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = "Film · $filmName",
                style = FilmoTextStyle.Label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GalleryIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(FilmoSize.Icon)) {
        val gap = 3.dp.toPx()
        val cell = (size.width - gap) / 2f
        val color = FilmoColors.TextPrimary
        drawRect(color = color, topLeft = Offset.Zero, size = Size(cell, cell))
        drawRect(color = color, topLeft = Offset(cell + gap, 0f), size = Size(cell, cell))
        drawRect(color = color, topLeft = Offset(0f, cell + gap), size = Size(cell, cell))
        drawRect(color = color, topLeft = Offset(cell + gap, cell + gap), size = Size(cell, cell))
    }
}

@Composable
private fun CircleIconButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(FilmoSize.IconButton)
            .clip(CircleShape)
            .background(FilmoColors.SurfaceOverlay)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun CameraSwitchIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(FilmoSize.Icon)) {
        val strokeWidth = 2.dp.toPx()
        val inset = 4.dp.toPx()
        val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

        drawArc(
            color = FilmoColors.TextPrimary,
            startAngle = 35f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = stroke
        )
        drawArc(
            color = FilmoColors.TextPrimary,
            startAngle = 215f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = stroke
        )
        drawLine(
            color = FilmoColors.TextPrimary,
            start = Offset(size.width - 5.dp.toPx(), 5.dp.toPx()),
            end = Offset(size.width - 8.dp.toPx(), 12.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = FilmoColors.TextPrimary,
            start = Offset(5.dp.toPx(), size.height - 5.dp.toPx()),
            end = Offset(8.dp.toPx(), size.height - 12.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun FocusRing(
    focusPoint: Offset?,
    previewSize: IntSize,
    modifier: Modifier = Modifier
) {
    if (focusPoint == null || previewSize == IntSize.Zero) return

    val scale = remember(focusPoint) { Animatable(0.82f) }
    val alpha = remember(focusPoint) { Animatable(1f) }
    val ringSize = 64.dp

    LaunchedEffect(focusPoint) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 180)
        )
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 470)
        )
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .offset {
                    val ringSizePx = ringSize.roundToPx()
                    IntOffset(
                        x = (focusPoint.x * previewSize.width - ringSizePx / 2f).roundToInt(),
                        y = (focusPoint.y * previewSize.height - ringSizePx / 2f).roundToInt()
                    )
                }
                .size(ringSize)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                }
                .border(
                    width = 2.dp,
                    color = FilmoColors.Focus,
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun CaptureFlash(
    visible: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val alpha = remember(visible) { Animatable(0.65f) }

    LaunchedEffect(visible) {
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 120)
        )
        delay(20)
        onFinished()
    }

    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = alpha.value))
    )
}

@Composable
private fun ShutterButton(
    capturing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(FilmoSize.ShutterOuter)
            .clip(CircleShape)
            .background(FilmoColors.SurfaceOverlay)
            .border(
                width = 2.dp,
                color = FilmoColors.TextPrimary,
                shape = CircleShape
            )
            .clickable(enabled = !capturing, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (capturing) 46.dp else FilmoSize.ShutterInner)
                .clip(CircleShape)
                .background(
                    if (capturing) {
                        FilmoColors.PrimaryMuted
                    } else {
                        FilmoColors.Primary
                    }
                )
        )
    }
}

@Composable
private fun SaveStatusToast(
    saveStatus: SaveStatus?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = saveStatus ?: return
    val message = when (status) {
        SaveStatus.Saving -> "Saving photo..."
        is SaveStatus.Success -> "Saved to gallery"
        is SaveStatus.Failure -> status.reason
    }

    Column(
        modifier = modifier
            .widthIn(max = 360.dp)
            .padding(horizontal = FilmoSpacing.Space16)
            .background(
                color = FilmoColors.SurfaceOverlay,
                shape = RoundedCornerShape(FilmoRadius.Full)
            )
            .padding(
                horizontal = FilmoSpacing.Space16,
                vertical = FilmoSpacing.Space8
            ),
        verticalArrangement = Arrangement.spacedBy(FilmoSpacing.Space4),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = FilmoColors.TextPrimary,
            style = FilmoTextStyle.Label,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (status is SaveStatus.Failure && status.canRetry) {
            Text(
                text = "Retry",
                color = FilmoColors.Primary,
                style = FilmoTextStyle.Label,
                modifier = Modifier.clickable(onClick = onRetry)
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
