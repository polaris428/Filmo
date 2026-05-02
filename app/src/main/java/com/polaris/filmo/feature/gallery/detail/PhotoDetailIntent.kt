package com.polaris.filmo.feature.gallery.detail

sealed interface PhotoDetailIntent {
    data object OnEnterScreen : PhotoDetailIntent
    data object OnBackClick : PhotoDetailIntent
    data object OnDeleteClick : PhotoDetailIntent
    data object OnCancelDelete : PhotoDetailIntent
    data object OnConfirmDelete : PhotoDetailIntent
}
