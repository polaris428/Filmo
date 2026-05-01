package com.polaris.filmo.feature.camera

sealed interface CameraEffect {
    data object RequestCameraPermission : CameraEffect
    data object OpenAppSettings : CameraEffect
    data object ShowFocusError : CameraEffect
    data object PlayShutterFeedback : CameraEffect
    data class ShowMessage(val message: String) : CameraEffect
    data class ShowError(val message: String) : CameraEffect
}
