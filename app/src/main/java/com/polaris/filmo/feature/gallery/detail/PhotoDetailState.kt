package com.polaris.filmo.feature.gallery.detail

import com.polaris.filmo.feature.gallery.PhotoUiModel

data class PhotoDetailState(
    val isLoading: Boolean = false,
    val photo: PhotoUiModel? = null,
    val isDeleteDialogVisible: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null
)
