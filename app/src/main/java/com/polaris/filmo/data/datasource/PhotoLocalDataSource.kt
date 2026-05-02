package com.polaris.filmo.data.datasource

import com.polaris.filmo.core.renderer.CapturedFrame
import com.polaris.filmo.domain.model.CapturedPhoto
import com.polaris.filmo.domain.model.FilmType

interface PhotoLocalDataSource {
    suspend fun savePhoto(
        frame: CapturedFrame,
        filmType: FilmType
    ): CapturedPhoto

    suspend fun getCapturedPhotos(): List<CapturedPhoto>

    suspend fun deletePhoto(photoId: String)
}
