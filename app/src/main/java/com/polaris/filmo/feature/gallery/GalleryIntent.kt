package com.polaris.filmo.feature.gallery

sealed interface GalleryIntent {
    data object OnEnterScreen : GalleryIntent
    data object OnRefresh : GalleryIntent
    data class OnClickPhoto(val photoId: String) : GalleryIntent
}
