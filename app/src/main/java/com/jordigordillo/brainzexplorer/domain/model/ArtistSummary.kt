package com.jordigordillo.brainzexplorer.domain.model

data class ArtistSummary(
    val id: String,
    val name: String,
    val disambiguation: String?,
    val imageUrl: String?,
)
