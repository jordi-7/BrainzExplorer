package com.jordigordillo.brainzexplorer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jordigordillo.brainzexplorer.domain.model.ArtistType
import com.jordigordillo.brainzexplorer.domain.repository.ArtistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ArtistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecommendations()
    }

    fun onSearchActiveChanged(active: Boolean) {
        _uiState.update {
            if (active) {
                it.copy(isSearchActive = true)
            } else {
                it.copy(
                    isSearchActive = false,
                    searchQuery = "",
                    selectedArtistTypes = emptySet(),
                    searchState = SearchState.Idle
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onArtistTypeFilterToggled(type: ArtistType) {
        _uiState.update { current ->
            val types = current.selectedArtistTypes
            current.copy(
                selectedArtistTypes = if (type in types) types - type else types + type
            )
        }
    }

    fun onSearchSubmit() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) return

        val types = _uiState.value.selectedArtistTypes
        Timber.d("Search submitted for query=%s, types=%s", query, types)

        viewModelScope.launch {
            _uiState.update { it.copy(searchState = SearchState.Loading) }

            val result = repository.searchArtists(query, types)
            _uiState.update { current ->
                current.copy(
                    searchState = result.fold(
                        onSuccess = { results ->
                            if (results.isEmpty()) SearchState.Empty else SearchState.Success(results)
                        },
                        onFailure = { e ->
                            Timber.e(e, "Search failed for query=%s", query)
                            SearchState.Error
                        }
                    )
                )
            }
        }
    }

    fun onRefresh() {
        if (_uiState.value.isSearchActive) {
            onSearchSubmit()
        } else {
            loadRecommendations()
        }
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            _uiState.update { it.copy(recommendationsState = RecommendationsState.Loading) }

            val result = repository.getRecommendations()
            _uiState.update { current ->
                current.copy(
                    recommendationsState = result.fold(
                        onSuccess = { RecommendationsState.Success(it) },
                        onFailure = { e ->
                            Timber.e(e, "Failed to load recommendations")
                            RecommendationsState.Error
                        }
                    )
                )
            }
        }
    }
}
