package com.polaris.filmo.feature.filmselection

sealed interface FilmSelectionIntent {
    data object OnBackClick : FilmSelectionIntent
    data class OnClickFilm(val filmId: String) : FilmSelectionIntent
}
