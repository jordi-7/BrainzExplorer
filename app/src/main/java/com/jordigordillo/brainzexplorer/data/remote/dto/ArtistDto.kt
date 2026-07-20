package com.jordigordillo.brainzexplorer.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistDto(
    val id: String,
    val name: String,
    val disambiguation: String? = null,
    val country: String? = null,
    @SerialName("release-groups")
    val releaseGroups: List<ReleaseGroupDto>? = null,
)

@Serializable
data class ArtistSearchResponseDto(
    val artists: List<ArtistDto> = emptyList(),
)

@Serializable
data class ReleaseGroupDto(
    val id: String,
    val title: String,
    @SerialName("primary-type")
    val primaryType: String? = null,
    @SerialName("first-release-date")
    val firstReleaseDate: String? = null,
)
