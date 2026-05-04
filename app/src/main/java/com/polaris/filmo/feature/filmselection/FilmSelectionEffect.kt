package com.polaris.filmo.feature.filmselection

sealed interface FilmSelectionEffect {
    data object NavigateBack : FilmSelectionEffect
    data class SelectFilmAndNavigateBack(val filmId: String) : FilmSelectionEffect
}
