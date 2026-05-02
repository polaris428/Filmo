package com.polaris.filmo.feature.gallery.detail

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.polaris.filmo.R
import com.polaris.filmo.core.design.FilmoColors
import com.polaris.filmo.core.design.FilmoRadius
import com.polaris.filmo.core.design.FilmoSize
import com.polaris.filmo.core.design.FilmoSpacing
import com.polaris.filmo.core.design.FilmoTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

@Composable
fun PhotoDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: PhotoDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.handleIntent(PhotoDetailIntent.OnEnterScreen)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                PhotoDetailEffect.NavigateBack,
                PhotoDetailEffect.NavigateBackWithRefresh -> onNavigateBack()

                is PhotoDetailEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FilmoColors.Background)
    ) {
        when {
            state.isLoading -> LoadingState(modifier = Modifier.fillMaxSize())
            state.errorMessage != null && state.photo == null -> ErrorState(
                message = state.errorMessage.orEmpty(),
                onBack = { viewModel.handleIntent(PhotoDetailIntent.OnBackClick) },
                modifier = Modifier.fillMaxSize()
            )

            state.photo != null -> PhotoContent(photoUri = state.photo!!.uri)
        }

        CircleIconButton(
            onClick = { viewModel.handleIntent(PhotoDetailIntent.OnBackClick) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(FilmoSpacing.Space16)
        ) {
            BackIcon()
        }

        CircleIconButton(
            onClick = { viewModel.handleIntent(PhotoDetailIntent.OnDeleteClick) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(FilmoSpacing.Space16)
        ) {
            DeleteIcon()
        }

        if (state.isDeleteDialogVisible) {
            DeleteConfirmDialog(
                isDeleting = state.isDeleting,
                errorMessage = state.errorMessage,
                onDismiss = { viewModel.handleIntent(PhotoDetailIntent.OnCancelDelete) },
                onConfirm = { viewModel.handleIntent(PhotoDetailIntent.OnConfirmDelete) }
            )
        }
    }
}

@Composable
private fun PhotoContent(photoUri: String) {
    val context = LocalContext.current
    val image by produceState<ImageBitmap?>(initialValue = null, photoUri) {
        value = loadImage(context, photoUri)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (image == null) {
            CircularProgressIndicator(
                color = FilmoColors.Primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(28.dp)
            )
        } else {
            Image(
                bitmap = image!!,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    isDeleting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FilmoColors.Surface,
        titleContentColor = FilmoColors.TextPrimary,
        textContentColor = FilmoColors.TextSecondary,
        title = {
            Text(text = "Delete photo?", style = FilmoTextStyle.Title)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(FilmoSpacing.Space8)) {
                Text(
                    text = "This removes the photo from Filmo and the device gallery.",
                    style = FilmoTextStyle.Body
                )
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        color = FilmoColors.Error,
                        style = FilmoTextStyle.Caption
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isDeleting,
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = FilmoColors.TextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !isDeleting,
                onClick = onConfirm,
                shape = RoundedCornerShape(FilmoRadius.Full),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FilmoColors.Error,
                    contentColor = Color.White,
                    disabledContainerColor = FilmoColors.ErrorPressed,
                    disabledContentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = FilmoSpacing.Space16)
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(text = stringResource(R.string.action_delete))
                }
            }
        }
    )
}

@Composable
private fun ErrorState(
    message: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(FilmoSpacing.Space24),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FilmoSpacing.Space16)
        ) {
            Text(
                text = "Photo unavailable",
                color = FilmoColors.TextPrimary,
                style = FilmoTextStyle.Title,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                color = FilmoColors.TextSecondary,
                style = FilmoTextStyle.Body,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onBack,
                modifier = Modifier.heightIn(min = FilmoSize.TouchTarget),
                shape = RoundedCornerShape(FilmoRadius.Full),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FilmoColors.Primary,
                    contentColor = FilmoColors.TextOnLight
                ),
                contentPadding = PaddingValues(horizontal = FilmoSpacing.Space16)
            ) {
                Text(text = "Back to gallery", style = FilmoTextStyle.Label)
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = FilmoColors.Primary,
            strokeWidth = 2.dp,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun CircleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(FilmoSize.IconButton)
            .clip(CircleShape)
            .background(FilmoColors.SurfaceOverlay)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun BackIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(FilmoSize.Icon)) {
        val strokeWidth = 2.dp.toPx()
        drawLine(
            color = FilmoColors.TextPrimary,
            start = Offset(size.width * 0.68f, size.height * 0.18f),
            end = Offset(size.width * 0.32f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = FilmoColors.TextPrimary,
            start = Offset(size.width * 0.32f, size.height * 0.5f),
            end = Offset(size.width * 0.68f, size.height * 0.82f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun DeleteIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(FilmoSize.Icon)) {
        val strokeWidth = 2.dp.toPx()
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        drawLine(
            color = FilmoColors.TextPrimary,
            start = Offset(size.width * 0.28f, size.height * 0.32f),
            end = Offset(size.width * 0.72f, size.height * 0.32f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = FilmoColors.TextPrimary,
            start = Offset(size.width * 0.42f, size.height * 0.22f),
            end = Offset(size.width * 0.58f, size.height * 0.22f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawRoundRect(
            color = FilmoColors.TextPrimary,
            topLeft = Offset(size.width * 0.34f, size.height * 0.38f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.32f, size.height * 0.42f),
            style = stroke
        )
    }
}

private suspend fun loadImage(
    context: Context,
    uriString: String
): ImageBitmap? = withContext(Dispatchers.IO) {
    runCatching {
        context.contentResolver.openInputStream(Uri.parse(uriString))?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
        }
    }.getOrNull()
}
