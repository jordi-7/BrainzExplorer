package com.jordigordillo.brainzexplorer.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jordigordillo.brainzexplorer.data.local.ReleaseGroupSortPreferences
import com.jordigordillo.brainzexplorer.domain.model.ReleaseGroupSortOrder
import com.jordigordillo.brainzexplorer.domain.repository.ArtistRepository
import com.jordigordillo.brainzexplorer.navigation.ArtistDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ArtistRepository,
    private val sortPreferences: ReleaseGroupSortPreferences
) : ViewModel() {

    private val route: ArtistDetailRoute = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(ArtistDetailUiState(artistName = route.artistName))
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            sortPreferences.sortOrder.collect { order ->
                _uiState.update { it.copy(sortOrder = order) }
            }
        }
    }

    fun refresh() {
        load()
    }

    fun onTypeToggle(type: String) {
        _uiState.update { current ->
            val types = current.selectedTypes
            current.copy(selectedTypes = if (type in types) types - type else types + type)
        }
    }

    fun onSortOrderSelected(order: ReleaseGroupSortOrder) {
        _uiState.update { it.copy(sortOrder = order) }
        viewModelScope.launch { sortPreferences.setSortOrder(order) }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(detailState = ArtistDetailState.Loading) }

            val result = repository.getArtistDetail(route.artistId)
            _uiState.update { current ->
                current.copy(
                    detailState = result.fold(
                        onSuccess = { ArtistDetailState.Success(it) },
                        onFailure = { e ->
                            Timber.e(e, "Failed to load artist detail for mbid=%s", route.artistId)
                            ArtistDetailState.Error
                        }
                    )
                )
            }
        }
    }
}
