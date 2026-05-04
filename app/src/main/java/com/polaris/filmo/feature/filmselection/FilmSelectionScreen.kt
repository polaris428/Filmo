package com.polaris.filmo.feature.filmselection

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.polaris.filmo.core.design.FilmoColors
import com.polaris.filmo.core.design.FilmoRadius
import com.polaris.filmo.core.design.FilmoSize
import com.polaris.filmo.core.design.FilmoSpacing
import com.polaris.filmo.core.design.FilmoTextStyle
import com.polaris.filmo.feature.camera.FilmUiModel
import com.polaris.filmo.feature.camera.toUiModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FilmSelectionScreen(
    onNavigateBack: () -> Unit,
    onSelectFilm: (String) -> Unit,
    viewModel: FilmSelectionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val films = remember(context, state.films) {
        state.films.map { it.toUiModel(context) }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                FilmSelectionEffect.NavigateBack -> onNavigateBack()
                is FilmSelectionEffect.SelectFilmAndNavigateBack -> {
                    onSelectFilm(effect.filmId)
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
        FilmSelectionTopBar(
            onBack = { viewModel.handleIntent(FilmSelectionIntent.OnBackClick) }
        )
        FilmGrid(
            films = films,
            selectedFilmId = state.selectedFilmId,
            onClickFilm = { filmId ->
                viewModel.handleIntent(FilmSelectionIntent.OnClickFilm(filmId))
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun FilmSelectionTopBar(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = FilmoSpacing.Space8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FilmoSpacing.Space8)
    ) {
        TextButtonLike(
            text = "Back",
            onClick = onBack
        )
        Text(
            text = "Film",
            color = FilmoColors.TextPrimary,
            style = FilmoTextStyle.Title,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(modifier = Modifier.heightIn(min = FilmoSize.TouchTarget).padding(horizontal = FilmoSpacing.Space12))
    }
}

@Composable
private fun TextButtonLike(
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
private fun FilmGrid(
    films: List<FilmUiModel>,
    selectedFilmId: String,
    onClickFilm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(FilmoSpacing.Space16),
        horizontalArrangement = Arrangement.spacedBy(FilmoSpacing.Space12),
        verticalArrangement = Arrangement.spacedBy(FilmoSpacing.Space12)
    ) {
        items(
            items = films,
            key = { it.id }
        ) { film ->
            FilmSelectionItem(
                film = film,
                selected = film.id == selectedFilmId,
                onClick = { onClickFilm(film.id) }
            )
        }
    }
}

@Composable
private fun FilmSelectionItem(
    film: FilmUiModel,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(FilmoRadius.Sm)
    val borderColor = if (selected) film.accentColor else FilmoColors.Divider

    Column(
        modifier = Modifier
            .clip(shape)
            .background(FilmoColors.Surface, shape)
            .border(width = if (selected) 2.dp else 1.dp, color = borderColor, shape = shape)
            .clickable(onClick = onClick)
            .padding(FilmoSpacing.Space12),
        verticalArrangement = Arrangement.spacedBy(FilmoSpacing.Space8)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.05f)
                .clip(RoundedCornerShape(FilmoRadius.Xs))
                .background(FilmoColors.SurfaceOverlayLight)
        ) {
            Image(
                painter = painterResource(film.thumbnailRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .heightIn(min = 5.dp)
                        .background(film.accentColor)
                )
            }
        }
        Text(
            text = film.displayName,
            color = FilmoColors.TextPrimary,
            style = FilmoTextStyle.Label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = film.description,
            color = FilmoColors.TextSecondary,
            style = FilmoTextStyle.Caption,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
