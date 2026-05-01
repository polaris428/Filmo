package com.polaris.filmo.domain.usecase

import com.polaris.filmo.core.renderer.CapturedFrame
import com.polaris.filmo.domain.model.CapturedPhoto
import com.polaris.filmo.domain.model.FilmType
import com.polaris.filmo.domain.repository.PhotoRepository
import javax.inject.Inject

class SavePhotoUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(
        frame: CapturedFrame,
        filmType: FilmType
    ): Result<CapturedPhoto> =
        photoRepository.savePhoto(frame, filmType)
}
