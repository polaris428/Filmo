package com.polaris.filmo.feature.filmselection

import com.polaris.filmo.domain.model.FilmType

data class FilmSelectionState(
    val films: List<FilmType> = emptyList(),
    val selectedFilmId: String = FilmType.Portra400.id,
    val errorMessage: String? = null
)
