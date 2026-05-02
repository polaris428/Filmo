package com.polaris.filmo.domain.usecase

import com.polaris.filmo.domain.repository.PhotoRepository
import javax.inject.Inject

class DeletePhotoUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(photoId: String): Result<Unit> =
        photoRepository.deletePhoto(photoId)
}
