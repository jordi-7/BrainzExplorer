package com.jordigordillo.brainzexplorer.domain.repository

import com.jordigordillo.brainzexplorer.domain.model.ArtistDetail
import com.jordigordillo.brainzexplorer.domain.model.ArtistSummary
import com.jordigordillo.brainzexplorer.domain.model.ArtistType
import com.jordigordillo.brainzexplorer.domain.model.Recommendations

interface ArtistRepository {

    /**
     * Searches for artists matching [query].
     *
     * @param query free-text search query.
     * @param types when non-empty, restricts results to artists of any of these [ArtistType]s.
     * @return matching artists, or [Result.failure] if the search could not be completed.
     */
    suspend fun searchArtists(query: String, types: Set<ArtistType> = emptySet()): Result<List<ArtistSummary>>

    /**
     * Loads the curated home-screen sections (featured artists plus a few genre sections).
     *
     * @return the recommended sections, or [Result.failure] if they could not be loaded.
     */
    suspend fun getRecommendations(): Result<Recommendations>

    /**
     * Loads full details for a single artist.
     *
     * @param mbid the artist's MusicBrainz identifier.
     * @return the artist's details, or [Result.failure] if they could not be loaded.
     */
    suspend fun getArtistDetail(mbid: String): Result<ArtistDetail>
}
