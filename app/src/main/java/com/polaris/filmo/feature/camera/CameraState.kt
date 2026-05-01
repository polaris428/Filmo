package com.polaris.filmo.feature.camera

import com.polaris.filmo.domain.model.FilmType

data class CameraState(
    val previewState: PreviewState = PreviewState.Idle,
    val permissionState: CameraPermissionState = CameraPermissionState.Unknown,
    val selectedFilm: FilmType = FilmType.Portra400,
    val isInitializing: Boolean = false,
    val errorMessage: String? = null
)

sealed interface PreviewState {
    data object Idle : PreviewState
    data object Loading : PreviewState
    data object Ready : PreviewState
    data object PermissionRequired : PreviewState
    data class Error(val message: String) : PreviewState
}

sealed interface CameraPermissionState {
    data object Unknown : CameraPermissionState
    data object Granted : CameraPermissionState
    data class Denied(val canRequestAgain: Boolean) : CameraPermissionState
}
