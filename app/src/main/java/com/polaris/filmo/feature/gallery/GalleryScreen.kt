package com.polaris.filmo.feature.gallery

import android.content.Context
import android.graphics.BitmapFactory
import android.util.LruCache
import android.net.Uri
import android.os.Build
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
fun GalleryScreen(
    onNavigateToPhotoDetail: (String) -> Unit,
    onNavigateToCamera: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.handleIntent(GalleryIntent.OnEnterScreen)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.handleIntent(GalleryIntent.OnRefresh)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is GalleryEffect.NavigateToPhotoDetail -> {
                    onNavigateToPhotoDetail(effect.photoId)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FilmoColors.Background)
            .statusBarsPadding()
    ) {
        GalleryTopBar(
            onBack = onNavigateToCamera,
            onRefresh = { viewModel.handleIntent(GalleryIntent.OnRefresh) }
        )

        when {
            state.isLoading -> GallerySkeletonGrid()
            state.errorMessage != null -> ErrorState(
                message = state.errorMessage.orEmpty(),
                onRetry = { viewModel.handleIntent(GalleryIntent.OnRefresh) },
                modifier = Modifier.fillMaxSize()
            )

            state.isEmpty -> EmptyState(
                onReturnToCamera = onNavigateToCamera,
                modifier = Modifier.fillMaxSize()
            )

            else -> PhotoGrid(
                photos = state.photos,
                onClickPhoto = { photoId ->
                    viewModel.handleIntent(GalleryIntent.OnClickPhoto(photoId))
                }
            )
        }
    }
}

@Composable
private fun GalleryTopBar(
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = FilmoSpacing.Space8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FilmoSpacing.Space8)
    ) {
        TopBarTextButton(text = "Back", onClick = onBack)
        Text(
            text = stringResource(R.string.gallery_title),
            color = FilmoColors.TextPrimary,
            style = FilmoTextStyle.Title,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        TopBarTextButton(text = "Refresh", onClick = onRefresh)
    }
}

@Composable
private fun TopBarTextButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .heightIn(min = FilmoSize.TouchTarget)
            .clip(RoundedCornerShape(FilmoRadius.Full))
            .clickable(onClick = onClick)
            .padding(horizontal = FilmoSpacing.Space12),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = FilmoColors.Primary,
            style = FilmoTextStyle.Label,
            maxLines = 1
        )
    }
}

@Composable
private fun PhotoGrid(
    photos: List<PhotoUiModel>,
    onClickPhoto: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(FilmoSize.GalleryGap),
        horizontalArrangement = Arrangement.spacedBy(FilmoSize.GalleryGap),
        verticalArrangement = Arrangement.spacedBy(FilmoSize.GalleryGap)
    ) {
        items(
            items = photos,
            key = { it.id }
        ) { photo ->
            PhotoThumbnail(
                photo = photo,
                onClick = { onClickPhoto(photo.id) }
            )
        }
    }
}

@Composable
private fun PhotoThumbnail(
    photo: PhotoUiModel,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, photo.uri) {
        value = loadThumbnail(context, photo.uri)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(FilmoColors.Surface)
            .clickable(onClick = onClick)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = photo.createdAtText,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FilmoColors.Surface)
            )
        }
    }
}

@Composable
private fun GallerySkeletonGrid() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(FilmoSize.GalleryGap),
        horizontalArrangement = Arrangement.spacedBy(FilmoSize.GalleryGap),
        verticalArrangement = Arrangement.spacedBy(FilmoSize.GalleryGap)
    ) {
        items(12) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(FilmoColors.Surface)
            )
        }
    }
}

@Composable
private fun EmptyState(
    onReturnToCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    MessageState(
        title = "No photos yet",
        message = "Captured Filmo photos will appear here.",
        actionText = "Back to camera",
        onAction = onReturnToCamera,
        modifier = modifier
    )
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    MessageState(
        title = "Gallery unavailable",
        message = message,
        actionText = stringResource(R.string.action_retry),
        onAction = onRetry,
        modifier = modifier
    )
}

@Composable
private fun MessageState(
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit,
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
                text = title,
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
                onClick = onAction,
                modifier = Modifier.heightIn(min = FilmoSize.TouchTarget),
                shape = RoundedCornerShape(FilmoRadius.Full),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FilmoColors.Primary,
                    contentColor = FilmoColors.TextOnLight
                ),
                contentPadding = PaddingValues(horizontal = FilmoSpacing.Space16)
            ) {
                Text(text = actionText, style = FilmoTextStyle.Label)
            }
        }
    }
}

private suspend fun loadThumbnail(
    context: Context,
    uriString: String
): ImageBitmap? = withContext(Dispatchers.IO) {
    GalleryThumbnailCache.get(uriString)?.let { return@withContext it }

    runCatching {
        val uri = Uri.parse(uriString)
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(uri, Size(420, 420), null)
        } else {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        }
        bitmap?.asImageBitmap()?.also { imageBitmap ->
            GalleryThumbnailCache.put(uriString, imageBitmap)
        }
    }.getOrNull()
}

private object GalleryThumbnailCache {
    private val cache = LruCache<String, ImageBitmap>(THUMBNAIL_CACHE_SIZE)

    fun get(uri: String): ImageBitmap? = cache.get(uri)

    fun put(uri: String, imageBitmap: ImageBitmap) {
        cache.put(uri, imageBitmap)
    }

    private const val THUMBNAIL_CACHE_SIZE = 80
}
