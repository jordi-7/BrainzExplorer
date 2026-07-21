package com.jordigordillo.brainzexplorer.ui.home

import com.jordigordillo.brainzexplorer.domain.model.ArtistSummary
import com.jordigordillo.brainzexplorer.domain.model.ArtistType
import com.jordigordillo.brainzexplorer.domain.model.Recommendations

data class HomeUiState(
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val selectedArtistTypes: Set<ArtistType> = emptySet(),
    val searchState: SearchState = SearchState.Idle,
    val recommendationsState: RecommendationsState = RecommendationsState.Loading
)

sealed interface SearchState {
    data object Idle : SearchState
    data object Loading : SearchState
    data class Success(val results: List<ArtistSummary>) : SearchState
    data object Empty : SearchState
    data object Error : SearchState
}

sealed interface RecommendationsState {
    data object Loading : RecommendationsState
    data class Success(val recommendations: Recommendations) : RecommendationsState
    data object Error : RecommendationsState
}
