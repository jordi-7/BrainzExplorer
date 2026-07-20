package com.jordigordillo.brainzexplorer.data.mapper

import com.jordigordillo.brainzexplorer.data.remote.dto.ArtistDto
import com.jordigordillo.brainzexplorer.data.remote.dto.ReleaseGroupDto
import com.jordigordillo.brainzexplorer.domain.model.ArtistDetail
import com.jordigordillo.brainzexplorer.domain.model.ArtistSummary
import com.jordigordillo.brainzexplorer.domain.model.ReleaseGroup

private const val COVER_ART_ARCHIVE_BASE_URL = "https://coverartarchive.org/release-group/"

fun coverArtUrl(releaseGroupId: String): String = "$COVER_ART_ARCHIVE_BASE_URL$releaseGroupId/front-250"

/** Picks a representative release group (preferring an official Album) to source cover art from. */
fun ArtistDto.representativeImageUrl(): String? =
    releaseGroups
        ?.firstOrNull { it.primaryType == "Album" }
        ?.let { coverArtUrl(it.id) }

fun ArtistDto.toSummary(imageUrl: String? = representativeImageUrl()): ArtistSummary =
    ArtistSummary(
        id = id,
        name = name,
        disambiguation = disambiguation,
        imageUrl = imageUrl,
    )

fun ArtistDto.toDetail(): ArtistDetail =
    ArtistDetail(
        id = id,
        name = name,
        disambiguation = disambiguation,
        country = country,
        releaseGroups = releaseGroups?.map { it.toDomain() } ?: emptyList(),
    )

fun ReleaseGroupDto.toDomain(): ReleaseGroup =
    ReleaseGroup(
        id = id,
        title = title,
        primaryType = primaryType,
        firstReleaseDate = firstReleaseDate,
    )
