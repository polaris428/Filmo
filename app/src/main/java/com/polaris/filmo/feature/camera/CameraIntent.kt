package com.polaris.filmo.feature.camera

sealed interface CameraIntent {
    data object OnEnterScreen : CameraIntent
    data object OnResume : CameraIntent
    data object OnRetryPreview : CameraIntent
    data object OnClickPermissionPrimaryAction : CameraIntent
    data object OnSwitchCamera : CameraIntent
    data object OnClickShutter : CameraIntent
    data object OnRetrySave : CameraIntent
    data object OnCaptureFeedbackFinished : CameraIntent
    data class OnTapPreview(val x: Float, val y: Float) : CameraIntent
    data class OnSelectFilm(val filmId: String) : CameraIntent
    data class OnPermissionResult(
        val granted: Boolean,
        val canRequestAgain: Boolean
    ) : CameraIntent
}
