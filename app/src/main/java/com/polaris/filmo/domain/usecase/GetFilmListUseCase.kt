package com.polaris.filmo.domain.usecase

import com.polaris.filmo.domain.model.FilmType
import javax.inject.Inject

class GetFilmListUseCase @Inject constructor() {
    operator fun invoke(): List<FilmType> = FilmType.entries
}
