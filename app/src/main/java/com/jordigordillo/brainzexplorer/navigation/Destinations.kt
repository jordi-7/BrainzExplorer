package com.jordigordillo.brainzexplorer.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Home

@Serializable
data class ArtistDetailRoute(
    val artistId: String,
    val artistName: String? = null,
)
