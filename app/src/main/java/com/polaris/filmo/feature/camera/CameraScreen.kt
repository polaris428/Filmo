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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.polaris.filmo.domain.model.FilmAdjustment
import com.polaris.filmo.domain.model.FilmAdjustmentParameter
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
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(Color.Black)
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
                }
            }

            CameraMenuArea(
                visible = state.previewState is PreviewState.Ready,
                selectedFilmName = selectedFilmUi.displayName,
                applyingFilm = state.isApplyingFilm,
                switchingCamera = state.isSwitchingCamera,
                capturing = state.isCapturing,
                saveStatus = state.saveStatus,
                onOpenGallery = onOpenGallery,
                onOpenFilmSelection = { onOpenFilmSelection(state.selectedFilmId) },
                onToggleFilmSettings = {
                    viewModel.handleIntent(CameraIntent.OnOpenFilmSettings)
                },
                onSwitchCamera = { viewModel.handleIntent(CameraIntent.OnSwitchCamera) },
                onClickShutter = { viewModel.handleIntent(CameraIntent.OnClickShutter) },
                onRetrySave = { viewModel.handleIntent(CameraIntent.OnRetrySave) }
            )
        }

        if (state.previewState is PreviewState.Ready && state.isFilmSettingsVisible) {
            FilmSettingsPanel(
                filmName = selectedFilmUi.displayName,
                adjustment = state.filmAdjustment,
                onClose = {
                    viewModel.handleIntent(CameraIntent.OnCloseFilmSettings)
                },
                onReset = {
                    viewModel.handleIntent(CameraIntent.OnResetFilmAdjustment)
                },
                onChange = { parameter, value ->
                    viewModel.handleIntent(CameraIntent.OnChangeFilmAdjustment(parameter, value))
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = FilmoSpacing.Space16,
                        end = FilmoSpacing.Space16,
                        bottom = 196.dp
                    )
            )
        }
    }
}

@Composable
private fun CameraMenuArea(
    visible: Boolean,
    selectedFilmName: String,
    applyingFilm: Boolean,
    switchingCamera: Boolean,
    capturing: Boolean,
    saveStatus: SaveStatus?,
    onOpenGallery: () -> Unit,
    onOpenFilmSelection: () -> Unit,
    onToggleFilmSettings: () -> Unit,
    onSwitchCamera: () -> Unit,
    onClickShutter: () -> Unit,
    onRetrySave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(FilmoColors.Surface)
            .padding(
                start = FilmoSpacing.Space16,
                top = FilmoSpacing.Space16,
                end = FilmoSpacing.Space16,
                bottom = FilmoSpacing.Space24
            ),
        verticalArrangement = Arrangement.spacedBy(FilmoSpacing.Space16),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (visible) {
            SaveStatusToast(
                saveStatus = saveStatus,
                onRetry = onRetrySave,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleIconButton(
                    enabled = true,
                    onClick = onOpenGallery
                ) {
                    GalleryIcon()
                }

                Spacer(modifier = Modifier.weight(1f))

                FilmPickerButton(
                    filmName = selectedFilmName,
                    applying = applyingFilm,
                    onClick = onOpenFilmSelection,
                    modifier = Modifier.weight(3f)
                )

                Spacer(modifier = Modifier.weight(0.45f))

                CircleIconButton(
                    enabled = !applyingFilm,
                    onClick = onToggleFilmSettings
                ) {
                    SettingsIcon()
                }

                Spacer(modifier = Modifier.weight(0.55f))

                CircleIconButton(
                    enabled = !switchingCamera,
                    onClick = onSwitchCamera
                ) {
                    CameraSwitchIcon()
                }
            }

            ShutterButton(
                capturing = capturing,
                onClick = onClickShutter
            )
        } else {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 164.dp)
            )
        }
    }
}

@Composable
private fun FilmSettingsPanel(
    filmName: String,
    adjustment: FilmAdjustment,
    onClose: () -> Unit,
    onReset: () -> Unit,
    onChange: (FilmAdjustmentParameter, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp)
            .background(
                color = FilmoColors.SurfaceOverlay,
                shape = RoundedCornerShape(FilmoRadius.Md)
            )
            .padding(FilmoSpacing.Space16)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(FilmoSpacing.Space8)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.film_settings_title, filmName),
                color = FilmoColors.TextPrimary,
                style = FilmoTextStyle.Label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onReset) {
                Text(
                    text = stringResource(R.string.action_default),
                    style = FilmoTextStyle.Label
                )
            }
            TextButton(onClick = onClose) {
                Text(
                    text = stringResource(R.string.action_close),
                    style = FilmoTextStyle.Label
                )
            }
        }

        AdjustmentSlider(
            label = stringResource(R.string.film_adjustment_warmth),
            value = adjustment.temperature,
            valueRange = -1f..1f,
            valueText = signedPercent(adjustment.temperature),
            onValueChange = { onChange(FilmAdjustmentParameter.Temperature, it) }
        )
        AdjustmentSlider(
            label = stringResource(R.string.film_adjustment_tint),
            value = adjustment.tint,
            valueRange = -1f..1f,
            valueText = signedPercent(adjustment.tint),
            onValueChange = { onChange(FilmAdjustmentParameter.Tint, it) }
        )
        AdjustmentSlider(
            label = stringResource(R.string.film_adjustment_contrast),
            value = adjustment.contrast,
            valueRange = 0.5f..1.5f,
            valueText = multiplierPercent(adjustment.contrast),
            onValueChange = { onChange(FilmAdjustmentParameter.Contrast, it) }
        )
        AdjustmentSlider(
            label = stringResource(R.string.film_adjustment_saturation),
            value = adjustment.saturation,
            valueRange = 0f..2f,
            valueText = multiplierPercent(adjustment.saturation),
            onValueChange = { onChange(FilmAdjustmentParameter.Saturation, it) }
        )
        AdjustmentSlider(
            label = stringResource(R.string.film_adjustment_grain),
            value = adjustment.grain,
            valueRange = 0f..1f,
            valueText = plainPercent(adjustment.grain),
            onValueChange = { onChange(FilmAdjustmentParameter.Grain, it) }
        )
        AdjustmentSlider(
            label = stringResource(R.string.film_adjustment_vignette),
            value = adjustment.vignette,
            valueRange = 0f..1f,
            valueText = plainPercent(adjustment.vignette),
            onValueChange = { onChange(FilmAdjustmentParameter.Vignette, it) }
        )
    }
}

@Composable
private fun AdjustmentSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FilmoSpacing.Space4)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = FilmoColors.TextPrimary,
                style = FilmoTextStyle.Caption,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valueText,
                color = FilmoColors.TextSecondary,
                style = FilmoTextStyle.Caption
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

private fun signedPercent(value: Float): String {
    val percent = (value * 100).roundToInt()
    return if (percent > 0) "+$percent" else percent.toString()
}

private fun multiplierPercent(value: Float): String =
    "${(value * 100).roundToInt()}%"

private fun plainPercent(value: Float): String =
    "${(value * 100).roundToInt()}%"

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
                text = stringResource(R.string.film_picker_change_current, filmName),
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
private fun SettingsIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(FilmoSize.Icon)) {
        val strokeWidth = 2.dp.toPx()
        val color = FilmoColors.TextPrimary
        val y1 = size.height * 0.30f
        val y2 = size.height * 0.52f
        val y3 = size.height * 0.74f

        drawLine(
            color = color,
            start = Offset(size.width * 0.18f, y1),
            end = Offset(size.width * 0.82f, y1),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.18f, y2),
            end = Offset(size.width * 0.82f, y2),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.18f, y3),
            end = Offset(size.width * 0.82f, y3),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(size.width * 0.62f, y1))
        drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(size.width * 0.38f, y2))
        drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(size.width * 0.68f, y3))
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
