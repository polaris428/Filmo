package com.polaris.filmo.feature.gallery.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polaris.filmo.domain.model.CapturedPhoto
import com.polaris.filmo.domain.usecase.DeletePhotoUseCase
import com.polaris.filmo.domain.usecase.GetCapturedPhotosUseCase
import com.polaris.filmo.feature.gallery.PhotoUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PhotoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCapturedPhotos: GetCapturedPhotosUseCase,
    private val deletePhoto: DeletePhotoUseCase
) : ViewModel() {
    private val photoId: String = savedStateHandle["photoId"] ?: ""

    private val _state = MutableStateFlow(PhotoDetailState())
    val state: StateFlow<PhotoDetailState> = _state.asStateFlow()

    private val _effects = Channel<PhotoDetailEffect>(Channel.BUFFERED)
    val effects: Flow<PhotoDetailEffect> = _effects.receiveAsFlow()

    fun handleIntent(intent: PhotoDetailIntent) {
        when (intent) {
            PhotoDetailIntent.OnEnterScreen -> loadPhoto()
            PhotoDetailIntent.OnBackClick -> sendEffect(PhotoDetailEffect.NavigateBack)
            PhotoDetailIntent.OnDeleteClick -> {
                _state.update {
                    it.copy(isDeleteDialogVisible = true, errorMessage = null)
                }
            }

            PhotoDetailIntent.OnCancelDelete -> {
                if (!_state.value.isDeleting) {
                    _state.update {
                        it.copy(isDeleteDialogVisible = false, errorMessage = null)
                    }
                }
            }

            PhotoDetailIntent.OnConfirmDelete -> confirmDelete()
        }
    }

    private fun loadPhoto() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            getCapturedPhotos()
                .onSuccess { photos ->
                    val photo = photos.firstOrNull { it.id == photoId }
                    _state.update {
                        if (photo == null) {
                            it.copy(
                                isLoading = false,
                                photo = null,
                                errorMessage = "Photo could not be found."
                            )
                        } else {
                            it.copy(
                                isLoading = false,
                                photo = photo.toUiModel(),
                                errorMessage = null
                            )
                        }
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.localizedMessage
                                ?: "Photo could not be loaded."
                        )
                    }
                }
        }
    }

    private fun confirmDelete() {
        if (_state.value.isDeleting) return

        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true, errorMessage = null) }
            deletePhoto(photoId)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isDeleting = false,
                            isDeleteDialogVisible = false,
                            errorMessage = null
                        )
                    }
                    _effects.send(PhotoDetailEffect.NavigateBackWithRefresh)
                }
                .onFailure { throwable ->
                    val message = throwable.localizedMessage ?: "Photo could not be deleted."
                    _state.update {
                        it.copy(
                            isDeleting = false,
                            errorMessage = message
                        )
                    }
                    _effects.send(PhotoDetailEffect.ShowMessage(message))
                }
        }
    }

    private fun sendEffect(effect: PhotoDetailEffect) {
        viewModelScope.launch {
            _effects.send(effect)
        }
    }

    private fun CapturedPhoto.toUiModel(): PhotoUiModel =
        PhotoUiModel(
            id = id,
            uri = uri,
            createdAtText = dateFormat.format(Date(createdAt))
        )

    private companion object {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
    }
}
