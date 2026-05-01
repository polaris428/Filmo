package com.polaris.filmo.feature.camera

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polaris.filmo.core.camera.CameraController
import com.polaris.filmo.core.camera.CameraPermissionChecker
import com.polaris.filmo.core.renderer.FilmRenderer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val permissionChecker: CameraPermissionChecker,
    private val cameraController: CameraController,
    val filmRenderer: FilmRenderer
) : ViewModel() {
    private val _state = MutableStateFlow(CameraState())
    val state: StateFlow<CameraState> = _state.asStateFlow()

    private val _effects = Channel<CameraEffect>(Channel.BUFFERED)
    val effects: Flow<CameraEffect> = _effects.receiveAsFlow()

    fun handleIntent(intent: CameraIntent) {
        when (intent) {
            CameraIntent.OnEnterScreen -> checkPermissionAndContinue(requestIfUnknown = true)
            CameraIntent.OnResume -> checkPermissionAndContinue(requestIfUnknown = false)
            CameraIntent.OnRetryPreview -> checkPermissionAndContinue(requestIfUnknown = false)
            CameraIntent.OnClickPermissionPrimaryAction -> onClickPermissionPrimaryAction()
            is CameraIntent.OnPermissionResult -> onPermissionResult(intent)
        }
    }

    fun startPreview(lifecycleOwner: LifecycleOwner) {
        val current = _state.value
        if (current.previewState !is PreviewState.Loading) return

        viewModelScope.launch {
            filmRenderer.setFilm(current.selectedFilm)
            cameraController.startPreview(filmRenderer, lifecycleOwner)
                .onSuccess {
                    _state.update {
                        it.copy(
                            previewState = PreviewState.Ready,
                            isInitializing = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    val message = throwable.localizedMessage ?: "Camera preview failed."
                    _state.update {
                        it.copy(
                            previewState = PreviewState.Error(message),
                            isInitializing = false,
                            errorMessage = message
                        )
                    }
                    _effects.send(CameraEffect.ShowError(message))
                }
        }
    }

    override fun onCleared() {
        cameraController.stopPreview()
        super.onCleared()
    }

    private fun checkPermissionAndContinue(requestIfUnknown: Boolean) {
        if (!permissionChecker.hasCameraPermission()) {
            val permissionState = _state.value.permissionState
            val shouldRequest =
                requestIfUnknown && permissionState is CameraPermissionState.Unknown

            _state.update {
                it.copy(
                    previewState = PreviewState.PermissionRequired,
                    permissionState = when (permissionState) {
                        CameraPermissionState.Granted -> CameraPermissionState.Unknown
                        else -> permissionState
                    },
                    isInitializing = false,
                    errorMessage = null
                )
            }
            if (shouldRequest) {
                viewModelScope.launch {
                    _effects.send(CameraEffect.RequestCameraPermission)
                }
            }
            return
        }

        _state.update {
            it.copy(
                previewState = PreviewState.Loading,
                permissionState = CameraPermissionState.Granted,
                isInitializing = true,
                errorMessage = null
            )
        }
    }

    private fun onPermissionResult(intent: CameraIntent.OnPermissionResult) {
        if (intent.granted) {
            _state.update {
                it.copy(permissionState = CameraPermissionState.Granted)
            }
            checkPermissionAndContinue(requestIfUnknown = false)
            return
        }

        _state.update {
            it.copy(
                previewState = PreviewState.PermissionRequired,
                permissionState = CameraPermissionState.Denied(intent.canRequestAgain),
                isInitializing = false
            )
        }
    }

    private fun onClickPermissionPrimaryAction() {
        val permissionState = _state.value.permissionState
        viewModelScope.launch {
            if (permissionState is CameraPermissionState.Denied &&
                !permissionState.canRequestAgain
            ) {
                _effects.send(CameraEffect.OpenAppSettings)
            } else {
                _effects.send(CameraEffect.RequestCameraPermission)
            }
        }
    }
}
