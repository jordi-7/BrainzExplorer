package com.jordigordillo.brainzexplorer.domain.model

data class ArtistDetail(
    val id: String,
    val name: String,
    val disambiguation: String?,
    val country: String?,
    val releaseGroups: List<ReleaseGroup>,
)
