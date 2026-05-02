package com.polaris.filmo.feature.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polaris.filmo.domain.model.CapturedPhoto
import com.polaris.filmo.domain.usecase.GetCapturedPhotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val getCapturedPhotos: GetCapturedPhotosUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(GalleryState())
    val state: StateFlow<GalleryState> = _state.asStateFlow()

    private val _effects = Channel<GalleryEffect>(Channel.BUFFERED)
    val effects: Flow<GalleryEffect> = _effects.receiveAsFlow()
    private var loadJob: Job? = null
    private var hasLoadedOnce = false

    fun handleIntent(intent: GalleryIntent) {
        when (intent) {
            GalleryIntent.OnEnterScreen -> {
                if (!hasLoadedOnce) {
                    loadPhotos(showLoading = _state.value.photos.isEmpty())
                }
            }

            GalleryIntent.OnRefresh -> loadPhotos(showLoading = false)

            is GalleryIntent.OnClickPhoto -> {
                viewModelScope.launch {
                    _effects.send(GalleryEffect.NavigateToPhotoDetail(intent.photoId))
                }
            }
        }
    }

    private fun loadPhotos(showLoading: Boolean) {
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = showLoading,
                    errorMessage = null
                )
            }
            getCapturedPhotos()
                .onSuccess { photos ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            photos = photos.map { photo -> photo.toUiModel() },
                            errorMessage = null
                        )
                    }
                    hasLoadedOnce = true
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = if (it.photos.isEmpty()) {
                                throwable.localizedMessage ?: "Photos could not be loaded."
                            } else {
                                null
                            }
                        )
                    }
                    hasLoadedOnce = true
                }
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
