package com.polaris.filmo.feature.camera

import androidx.compose.ui.geometry.Offset
import com.polaris.filmo.core.camera.LensFacing
import com.polaris.filmo.domain.model.CapturedPhoto
import com.polaris.filmo.domain.model.FilmAdjustment
import com.polaris.filmo.domain.model.FilmType

data class CameraState(
    val previewState: PreviewState = PreviewState.Idle,
    val permissionState: CameraPermissionState = CameraPermissionState.Unknown,
    val lensFacing: LensFacing = LensFacing.Back,
    val isSwitchingCamera: Boolean = false,
    val focusPoint: Offset? = null,
    val isCapturing: Boolean = false,
    val captureFeedbackState: CaptureFeedbackState = CaptureFeedbackState.Idle,
    val saveStatus: SaveStatus? = null,
    val lastSavedPhoto: CapturedPhoto? = null,
    val films: List<FilmType> = FilmType.entries,
    val selectedFilm: FilmType = FilmType.Portra400,
    val selectedFilmId: String = FilmType.Portra400.id,
    val filmAdjustment: FilmAdjustment = FilmType.Portra400.defaultAdjustment,
    val isFilmSettingsVisible: Boolean = false,
    val isApplyingFilm: Boolean = false,
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

sealed interface CaptureFeedbackState {
    data object Idle : CaptureFeedbackState
    data object Flash : CaptureFeedbackState
}

sealed interface SaveStatus {
    data object Saving : SaveStatus
    data class Success(val photo: CapturedPhoto) : SaveStatus
    data class Failure(val reason: String, val canRetry: Boolean) : SaveStatus
}
