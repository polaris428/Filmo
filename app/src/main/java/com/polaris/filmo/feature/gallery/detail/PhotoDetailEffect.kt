package com.polaris.filmo.feature.gallery.detail

sealed interface PhotoDetailEffect {
    data object NavigateBack : PhotoDetailEffect
    data object NavigateBackWithRefresh : PhotoDetailEffect
    data class ShowMessage(val message: String) : PhotoDetailEffect
}
