package com.polaris.filmo.feature.gallery

data class GalleryState(
    val isLoading: Boolean = false,
    val photos: List<PhotoUiModel> = emptyList(),
    val errorMessage: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && photos.isEmpty()
}
