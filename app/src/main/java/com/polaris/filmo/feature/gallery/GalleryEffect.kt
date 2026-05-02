package com.polaris.filmo.feature.gallery

sealed interface GalleryEffect {
    data class NavigateToPhotoDetail(val photoId: String) : GalleryEffect
}
