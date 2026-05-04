package com.polaris.filmo.feature.filmselection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polaris.filmo.domain.model.FilmType
import com.polaris.filmo.domain.usecase.GetFilmListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class FilmSelectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getFilmList: GetFilmListUseCase
) : ViewModel() {
    private val selectedFilmId: String =
        savedStateHandle["selectedFilmId"] ?: FilmType.Portra400.id

    private val _state = MutableStateFlow(
        FilmSelectionState(
            films = getFilmList(),
            selectedFilmId = selectedFilmId
        )
    )
    val state: StateFlow<FilmSelectionState> = _state.asStateFlow()

    private val _effects = Channel<FilmSelectionEffect>(Channel.BUFFERED)
    val effects: Flow<FilmSelectionEffect> = _effects.receiveAsFlow()

    fun handleIntent(intent: FilmSelectionIntent) {
        when (intent) {
            FilmSelectionIntent.OnBackClick -> sendEffect(FilmSelectionEffect.NavigateBack)
            is FilmSelectionIntent.OnClickFilm -> {
                sendEffect(FilmSelectionEffect.SelectFilmAndNavigateBack(intent.filmId))
            }
        }
    }

    private fun sendEffect(effect: FilmSelectionEffect) {
        viewModelScope.launch {
            _effects.send(effect)
        }
    }
}
