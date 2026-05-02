package com.polaris.filmo.domain.usecase

import com.polaris.filmo.domain.model.CapturedPhoto
import com.polaris.filmo.domain.repository.PhotoRepository
import javax.inject.Inject

class GetCapturedPhotosUseCase @Inject constructor(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(): Result<List<CapturedPhoto>> =
        photoRepository.getCapturedPhotos()
            .map { photos -> photos.sortedByDescending { it.createdAt } }
}
