package com.polaris.filmo.domain.repository

import com.polaris.filmo.core.renderer.CapturedFrame
import com.polaris.filmo.domain.model.CapturedPhoto
import com.polaris.filmo.domain.model.FilmType

interface PhotoRepository {
    suspend fun savePhoto(
        frame: CapturedFrame,
        filmType: FilmType
    ): Result<CapturedPhoto>
}
