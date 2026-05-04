package com.polaris.filmo.feature.camera

import com.polaris.filmo.domain.model.FilmAdjustmentParameter

sealed interface CameraIntent {
    data object OnEnterScreen : CameraIntent
    data object OnResume : CameraIntent
    data object OnPreviewViewCreated : CameraIntent
    data object OnPreviewViewDisposed : CameraIntent
    data object OnRetryPreview : CameraIntent
    data object OnClickPermissionPrimaryAction : CameraIntent
    data object OnSwitchCamera : CameraIntent
    data object OnClickShutter : CameraIntent
    data object OnRetrySave : CameraIntent
    data object OnCaptureFeedbackFinished : CameraIntent
    data object OnOpenFilmSettings : CameraIntent
    data object OnCloseFilmSettings : CameraIntent
    data object OnResetFilmAdjustment : CameraIntent
    data class OnTapPreview(val x: Float, val y: Float) : CameraIntent
    data class OnSelectFilm(val filmId: String) : CameraIntent
    data class OnChangeFilmAdjustment(
        val parameter: FilmAdjustmentParameter,
        val value: Float
    ) : CameraIntent
    data class OnPermissionResult(
        val granted: Boolean,
        val canRequestAgain: Boolean
    ) : CameraIntent
}
