package com.polaris.filmo.feature.camera

sealed interface CameraEffect {
    data object RequestCameraPermission : CameraEffect
    data object OpenAppSettings : CameraEffect
    data class ShowError(val message: String) : CameraEffect
}
