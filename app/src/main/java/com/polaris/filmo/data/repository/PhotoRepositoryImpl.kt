package com.polaris.filmo.data.repository

import com.polaris.filmo.core.renderer.CapturedFrame
import com.polaris.filmo.data.datasource.PhotoLocalDataSource
import com.polaris.filmo.domain.model.CapturedPhoto
import com.polaris.filmo.domain.model.FilmType
import com.polaris.filmo.domain.repository.PhotoRepository
import javax.inject.Inject

class PhotoRepositoryImpl @Inject constructor(
    private val localDataSource: PhotoLocalDataSource
) : PhotoRepository {
    override suspend fun savePhoto(
        frame: CapturedFrame,
        filmType: FilmType
    ): Result<CapturedPhoto> = runCatching {
        localDataSource.savePhoto(frame, filmType)
    }
}
