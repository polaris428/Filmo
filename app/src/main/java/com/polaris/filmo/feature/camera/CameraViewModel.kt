package com.polaris.filmo.feature.camera

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.geometry.Offset
import com.polaris.filmo.core.camera.CameraController
import com.polaris.filmo.core.camera.CameraPermissionChecker
import com.polaris.filmo.core.camera.LensFacing
import com.polaris.filmo.core.renderer.CapturedFrame
import com.polaris.filmo.core.renderer.FilmRenderer
import com.polaris.filmo.domain.model.FilmAdjustment
import com.polaris.filmo.domain.model.FilmAdjustmentParameter
import com.polaris.filmo.domain.model.FilmType
import com.polaris.filmo.domain.usecase.SavePhotoUseCase
import com.polaris.filmo.domain.usecase.GetFilmListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val permissionChecker: CameraPermissionChecker,
    private val cameraController: CameraController,
    private val savePhoto: SavePhotoUseCase,
    getFilmList: GetFilmListUseCase,
    val filmRenderer: FilmRenderer
) : ViewModel() {
    private val _state = MutableStateFlow(CameraState(films = getFilmList()))
    val state: StateFlow<CameraState> = _state.asStateFlow()

    private val _effects = Channel<CameraEffect>(Channel.BUFFERED)
    val effects: Flow<CameraEffect> = _effects.receiveAsFlow()
    private var filmSelectionJob: Job? = null
    private var focusRingJob: Job? = null
    private var saveStatusJob: Job? = null
    private var lastCapturedFrame: CapturedFrame? = null
    private var lastCapturedFilm: FilmType? = null

    fun handleIntent(intent: CameraIntent) {
        when (intent) {
            CameraIntent.OnEnterScreen -> checkPermissionAndContinue(requestIfUnknown = true)
            CameraIntent.OnResume -> onResume()
            CameraIntent.OnPreviewViewCreated -> Unit
            CameraIntent.OnPreviewViewDisposed -> onPreviewViewDisposed()
            CameraIntent.OnRetryPreview -> checkPermissionAndContinue(requestIfUnknown = false)
            CameraIntent.OnClickPermissionPrimaryAction -> onClickPermissionPrimaryAction()
            CameraIntent.OnSwitchCamera -> onSwitchCamera()
            CameraIntent.OnClickShutter -> onClickShutter()
            CameraIntent.OnRetrySave -> onRetrySave()
            CameraIntent.OnCaptureFeedbackFinished -> onCaptureFeedbackFinished()
            CameraIntent.OnOpenFilmSettings -> onOpenFilmSettings()
            CameraIntent.OnCloseFilmSettings -> onCloseFilmSettings()
            CameraIntent.OnResetFilmAdjustment -> onResetFilmAdjustment()
            is CameraIntent.OnTapPreview -> onTapPreview(intent.x, intent.y)
            is CameraIntent.OnSelectFilm -> onSelectFilm(intent.filmId)
            is CameraIntent.OnChangeFilmAdjustment -> {
                onChangeFilmAdjustment(intent.parameter, intent.value)
            }
            is CameraIntent.OnPermissionResult -> onPermissionResult(intent)
        }
    }

    fun startPreview(
        lifecycleOwner: LifecycleOwner,
        force: Boolean = false
    ) {
        val current = _state.value
        if (!force && current.previewState !is PreviewState.Loading) return
        if (!permissionChecker.hasCameraPermission()) return

        viewModelScope.launch {
            filmRenderer.setFilm(current.selectedFilm)
            filmRenderer.setFilmAdjustment(current.filmAdjustment)
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

    private fun onPreviewViewDisposed() {
        cameraController.stopPreview()
        _state.update {
            when (it.previewState) {
                PreviewState.Loading -> it.copy(
                    previewState = PreviewState.Idle,
                    isInitializing = false
                )

                else -> it
            }
        }
    }

    private fun onResume() {
        val current = _state.value
        if (current.previewState is PreviewState.Ready &&
            permissionChecker.hasCameraPermission()
        ) {
            return
        }
        checkPermissionAndContinue(requestIfUnknown = false)
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

    private fun onSelectFilm(filmId: String) {
        val nextFilm = _state.value.films.firstOrNull { it.id == filmId } ?: return
        if (_state.value.selectedFilm == nextFilm) return

        filmSelectionJob?.cancel()
        filmSelectionJob = viewModelScope.launch {
            val previous = _state.value.selectedFilm
            val previousAdjustment = _state.value.filmAdjustment
            val nextAdjustment = nextFilm.defaultAdjustment
            _state.update {
                it.copy(
                    selectedFilm = nextFilm,
                    selectedFilmId = nextFilm.id,
                    filmAdjustment = nextAdjustment,
                    isApplyingFilm = true
                )
            }

            filmRenderer.setFilm(nextFilm)
            filmRenderer.setFilmAdjustment(nextAdjustment)
                .onSuccess {
                    _state.update { it.copy(isApplyingFilm = false) }
                }
                .onFailure { throwable ->
                    val message = throwable.localizedMessage ?: "Film could not be applied."
                    _state.update {
                        it.copy(
                            selectedFilm = previous,
                            selectedFilmId = previous.id,
                            filmAdjustment = previousAdjustment,
                            isApplyingFilm = false,
                            errorMessage = message
                        )
                    }
                    filmRenderer.setFilmAdjustment(previousAdjustment)
                    _effects.send(CameraEffect.ShowError(message))
                }
        }
    }

    private fun onOpenFilmSettings() {
        _state.update { it.copy(isFilmSettingsVisible = !it.isFilmSettingsVisible) }
    }

    private fun onCloseFilmSettings() {
        _state.update { it.copy(isFilmSettingsVisible = false) }
    }

    private fun onResetFilmAdjustment() {
        applyFilmAdjustment(_state.value.selectedFilm.defaultAdjustment)
    }

    private fun onChangeFilmAdjustment(parameter: FilmAdjustmentParameter, value: Float) {
        val nextAdjustment = _state.value.filmAdjustment.withParameter(parameter, value)
        applyFilmAdjustment(nextAdjustment)
    }

    private fun applyFilmAdjustment(adjustment: FilmAdjustment) {
        _state.update { it.copy(filmAdjustment = adjustment) }
        filmRenderer.setFilmAdjustment(adjustment)
            .onFailure { throwable ->
                viewModelScope.launch {
                    val message = throwable.localizedMessage ?: "Film settings could not be applied."
                    _effects.send(CameraEffect.ShowError(message))
                }
            }
    }

    private fun onSwitchCamera() {
        val current = _state.value
        if (current.isSwitchingCamera || current.previewState !is PreviewState.Ready) return

        val target = when (current.lensFacing) {
            LensFacing.Back -> LensFacing.Front
            LensFacing.Front -> LensFacing.Back
        }
        viewModelScope.launch {
            _state.update { it.copy(isSwitchingCamera = true) }
            cameraController.switchCamera(target)
                .onSuccess {
                    filmRenderer.setFilm(_state.value.selectedFilm)
                    filmRenderer.setFilmAdjustment(_state.value.filmAdjustment)
                    _state.update {
                        it.copy(
                            lensFacing = target,
                            isSwitchingCamera = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    val message = throwable.localizedMessage ?: "Camera could not be switched."
                    _state.update {
                        it.copy(
                            isSwitchingCamera = false,
                            errorMessage = message
                        )
                    }
                    _effects.send(CameraEffect.ShowError(message))
                }
        }
    }

    private fun onTapPreview(x: Float, y: Float) {
        if (_state.value.previewState !is PreviewState.Ready) return

        focusRingJob?.cancel()
        focusRingJob = viewModelScope.launch {
            val focusPoint = Offset(x, y)
            _state.update { it.copy(focusPoint = focusPoint) }

            cameraController.focusAt(x, y)
                .onFailure {
                    _effects.send(CameraEffect.ShowFocusError)
                }

            delay(FOCUS_RING_DURATION_MILLIS)
            _state.update {
                if (it.focusPoint == focusPoint) {
                    it.copy(focusPoint = null)
                } else {
                    it
                }
            }
        }
    }

    private companion object {
        const val FOCUS_RING_DURATION_MILLIS = 650L
        const val SAVE_STATUS_DURATION_MILLIS = 2_000L
    }

    private fun onClickShutter() {
        val current = _state.value
        if (current.isCapturing ||
            current.previewState !is PreviewState.Ready ||
            !permissionChecker.hasCameraPermission()
        ) {
            return
        }

        viewModelScope.launch {
            val film = _state.value.selectedFilm
            _state.update {
                it.copy(
                    isCapturing = true,
                    captureFeedbackState = CaptureFeedbackState.Flash,
                    saveStatus = SaveStatus.Saving,
                    errorMessage = null
                )
            }
            _effects.send(CameraEffect.PlayShutterFeedback)

            captureCurrentFrame()
                .onSuccess { frame ->
                    lastCapturedFrame = frame
                    lastCapturedFilm = film
                    saveCapturedFrame(frame, film)
                }
                .onFailure { throwable ->
                    val message = throwable.localizedMessage ?: "Photo could not be captured."
                    lastCapturedFrame = null
                    lastCapturedFilm = null
                    _state.update {
                        it.copy(
                            isCapturing = false,
                            saveStatus = SaveStatus.Failure(message, canRetry = false),
                            errorMessage = message
                        )
                    }
                    _effects.send(CameraEffect.ShowMessage(message))
                }
        }
    }

    private fun onRetrySave() {
        val frame = lastCapturedFrame ?: return
        val film = lastCapturedFilm ?: _state.value.selectedFilm
        if (_state.value.isCapturing) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isCapturing = true,
                    saveStatus = SaveStatus.Saving,
                    errorMessage = null
                )
            }
            saveCapturedFrame(frame, film)
        }
    }

    private fun onCaptureFeedbackFinished() {
        _state.update { it.copy(captureFeedbackState = CaptureFeedbackState.Idle) }
    }

    private suspend fun saveCapturedFrame(
        frame: CapturedFrame,
        film: FilmType
    ) {
        savePhoto(frame, film)
            .onSuccess { photo ->
                lastCapturedFrame = null
                lastCapturedFilm = null
                _state.update {
                    it.copy(
                        isCapturing = false,
                        saveStatus = SaveStatus.Success(photo),
                        lastSavedPhoto = photo,
                        errorMessage = null
                    )
                }
                _effects.send(CameraEffect.ShowMessage("Photo saved."))
                clearSaveStatusLater()
            }
            .onFailure { throwable ->
                val message = throwable.localizedMessage ?: "Photo could not be saved."
                _state.update {
                    it.copy(
                        isCapturing = false,
                        saveStatus = SaveStatus.Failure(message, canRetry = true),
                        errorMessage = message
                    )
                }
                _effects.send(CameraEffect.ShowMessage(message))
            }
    }

    private fun clearSaveStatusLater() {
        saveStatusJob?.cancel()
        saveStatusJob = viewModelScope.launch {
            delay(SAVE_STATUS_DURATION_MILLIS)
            _state.update {
                if (it.saveStatus is SaveStatus.Success) {
                    it.copy(saveStatus = null)
                } else {
                    it
                }
            }
        }
    }

    private suspend fun captureCurrentFrame(): Result<CapturedFrame> =
        suspendCancellableCoroutine { continuation ->
            filmRenderer.captureCurrentFrame { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
        }
}
