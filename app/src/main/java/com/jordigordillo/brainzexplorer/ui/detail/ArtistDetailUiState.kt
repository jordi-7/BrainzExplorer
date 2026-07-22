package com.jordigordillo.brainzexplorer.ui.detail

import com.jordigordillo.brainzexplorer.domain.model.ArtistDetail
import com.jordigordillo.brainzexplorer.domain.model.ReleaseGroupSortOrder

data class ArtistDetailUiState(
    val artistName: String?,
    val detailState: ArtistDetailState = ArtistDetailState.Loading,
    val selectedTypes: Set<String> = emptySet(),
    val sortOrder: ReleaseGroupSortOrder = ReleaseGroupSortOrder.NEWEST_FIRST
)

sealed interface ArtistDetailState {
    data object Loading : ArtistDetailState
    data class Success(val detail: ArtistDetail) : ArtistDetailState
    data object Error : ArtistDetailState
}
