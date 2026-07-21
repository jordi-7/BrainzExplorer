package com.jordigordillo.brainzexplorer.data.repository

import com.jordigordillo.brainzexplorer.data.mapper.representativeImageUrl
import com.jordigordillo.brainzexplorer.data.mapper.toDetail
import com.jordigordillo.brainzexplorer.data.mapper.toSummary
import com.jordigordillo.brainzexplorer.data.remote.MusicBrainzApi
import com.jordigordillo.brainzexplorer.domain.model.ArtistDetail
import com.jordigordillo.brainzexplorer.domain.model.ArtistSummary
import com.jordigordillo.brainzexplorer.domain.model.Recommendations
import com.jordigordillo.brainzexplorer.domain.repository.ArtistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject

/**
 * MusicBrainz artists have no "image" of their own, so carousel artwork is resolved via a
 * representative release group's Cover Art Archive front image. Featured artists are looked up
 * directly with release-groups included (one call each); genre sections and user search results
 * need one search call plus one follow-up lookup per artist shown, to source that artist's image.
 */
class ArtistRepositoryImpl @Inject constructor(
    private val api: MusicBrainzApi,
) : ArtistRepository {

    // A failed image lookup keeps the artist but falls back to no image, same as loadGenre.
    override suspend fun searchArtists(query: String): Result<List<ArtistSummary>> = runCatching {
        coroutineScope {
            api.searchArtists(query = query).artists.map { artistDto ->
                async {
                    val imageUrl = runCatching { api.lookupArtist(artistDto.id) }
                        .onFailure { Timber.w(it, "Failed to load image for artist mbid=%s", artistDto.id) }
                        .getOrNull()
                        ?.representativeImageUrl()
                    artistDto.toSummary(imageUrl)
                }
            }.awaitAll()
        }
    }.onFailure { Timber.e(it, "Failed to search artists for query=%s", query) }

    override suspend fun getRecommendations(): Result<Recommendations> = runCatching {
        // Load all four sections concurrently rather than sequentially.
        coroutineScope {
            val featuredDeferred = async { loadFeatured() }
            val rockDeferred = async { loadGenre("rock") }
            val popDeferred = async { loadGenre("pop") }
            val electronicDeferred = async { loadGenre("electronic") }
            Recommendations(
                featured = featuredDeferred.await(),
                rock = rockDeferred.await(),
                pop = popDeferred.await(),
                electronic = electronicDeferred.await(),
            )
        }
    }.onFailure { Timber.e(it, "Failed to load recommendations") }

    override suspend fun getArtistDetail(mbid: String): Result<ArtistDetail> = runCatching {
        api.lookupArtist(mbid).toDetail()
    }.onFailure { Timber.e(it, "Failed to load artist detail for mbid=%s", mbid) }

    // A failed lookup drops that one artist (logged as a warning) instead of failing the whole carousel.
    private suspend fun loadFeatured(): List<ArtistSummary> = coroutineScope {
        FEATURED_ARTIST_IDS.map { mbid ->
            async {
                runCatching { api.lookupArtist(mbid) }
                    .onFailure { Timber.w(it, "Dropping featured artist mbid=%s: lookup failed", mbid) }
                    .getOrNull()
                    ?.toSummary()
            }
        }.awaitAll().filterNotNull()
    }

    // Unlike loadFeatured, a failed image lookup keeps the artist but falls back to no image.
    private suspend fun loadGenre(tag: String): List<ArtistSummary> = coroutineScope {
        val artists = runCatching { api.searchArtists(query = "tag:$tag") }
            .onFailure { Timber.w(it, "Failed to search genre tag=%s", tag) }
            .getOrNull()
            ?.artists
            ?.take(GENRE_SECTION_SIZE)
            .orEmpty()

        artists.map { artistDto ->
            async {
                val imageUrl = runCatching { api.lookupArtist(artistDto.id) }
                    .onFailure { Timber.w(it, "Failed to load image for artist mbid=%s", artistDto.id) }
                    .getOrNull()
                    ?.representativeImageUrl()
                artistDto.toSummary(imageUrl)
            }
        }.awaitAll()
    }

    private companion object {
        const val GENRE_SECTION_SIZE = 6

        // Curated well-known artists shown in the "Featured" carousel.
        val FEATURED_ARTIST_IDS = listOf(
            "a74b1b7f-71a5-4011-9441-d0b5e4122711", // Radiohead
            "056e4f3e-d505-4dad-8ec1-d04f521cbb56", // Daft Punk
            "b95ce3ff-3d05-4e87-9e01-c97b66af13d4", // Eminem
            "b10bbbfc-cf9e-42e0-be17-e2c3e1d2600d", // The Beatles
            "5b11f4ce-a62d-471e-81fc-a69a8278c7da"  // Nirvana
        )
    }
}
