package com.jordigordillo.brainzexplorer.data.repository

import com.jordigordillo.brainzexplorer.data.remote.MusicBrainzApi
import com.jordigordillo.brainzexplorer.data.remote.dto.ArtistDto
import com.jordigordillo.brainzexplorer.data.remote.dto.ArtistSearchResponseDto
import com.jordigordillo.brainzexplorer.data.remote.dto.ReleaseGroupDto
import com.jordigordillo.brainzexplorer.domain.model.ArtistType
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ArtistRepositoryImplTest {

    private val api: MusicBrainzApi = mockk()
    private lateinit var repository: ArtistRepositoryImpl

    @Before
    fun setUp() {
        repository = ArtistRepositoryImpl(api)
    }

    @Test
    fun `searchArtists maps dto list to domain summaries without images when no release groups`() = runTest {
        coEvery { api.searchArtists(query = "radiohead") } returns ArtistSearchResponseDto(
            artists = listOf(
                ArtistDto(id = "abc", name = "Radiohead", disambiguation = "UK rock band")
            )
        )
        coEvery { api.lookupArtist("abc") } returns ArtistDto(id = "abc", name = "Radiohead")

        val result = repository.searchArtists("radiohead")

        assertTrue(result.isSuccess)
        val summaries = result.getOrThrow()
        assertEquals(1, summaries.size)
        assertEquals("abc", summaries[0].id)
        assertEquals("Radiohead", summaries[0].name)
        assertEquals("UK rock band", summaries[0].disambiguation)
        assertEquals(null, summaries[0].imageUrl)
    }

    @Test
    fun `searchArtists resolves image url from a follow-up lookup per result`() = runTest {
        coEvery { api.searchArtists(query = "radiohead") } returns ArtistSearchResponseDto(
            artists = listOf(
                ArtistDto(id = "abc", name = "Radiohead")
            )
        )
        coEvery { api.lookupArtist("abc") } returns ArtistDto(
            id = "abc",
            name = "Radiohead",
            releaseGroups = listOf(
                ReleaseGroupDto(id = "rg1", title = "OK Computer", primaryType = "Album")
            )
        )

        val result = repository.searchArtists("radiohead")

        assertTrue(result.isSuccess)
        val summaries = result.getOrThrow()
        assertEquals(1, summaries.size)
        assertTrue(summaries[0].imageUrl?.contains("coverartarchive.org") == true)
    }

    @Test
    fun `searchArtists falls back to no image when the follow-up lookup fails`() = runTest {
        coEvery { api.searchArtists(query = "radiohead") } returns ArtistSearchResponseDto(
            artists = listOf(
                ArtistDto(id = "abc", name = "Radiohead")
            )
        )
        coEvery { api.lookupArtist("abc") } throws RuntimeException("network down")

        val result = repository.searchArtists("radiohead")

        assertTrue(result.isSuccess)
        val summaries = result.getOrThrow()
        assertEquals(1, summaries.size)
        assertEquals(null, summaries[0].imageUrl)
    }

    @Test
    fun `searchArtists propagates failure as Result failure`() = runTest {
        val exception = RuntimeException("network down")
        coEvery { api.searchArtists(query = "radiohead") } throws exception

        val result = repository.searchArtists("radiohead")

        assertTrue(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
    }

    @Test
    fun `searchArtists appends a type filter to the query when types are selected`() = runTest {
        coEvery {
            api.searchArtists(query = "radiohead AND (type:Group)")
        } returns ArtistSearchResponseDto(
            artists = listOf(ArtistDto(id = "abc", name = "Radiohead"))
        )
        coEvery { api.lookupArtist("abc") } returns ArtistDto(id = "abc", name = "Radiohead")

        val result = repository.searchArtists("radiohead", setOf(ArtistType.GROUP))

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
    }

    @Test
    fun `searchArtists ORs multiple selected types together in the query`() = runTest {
        coEvery {
            api.searchArtists(query = "queen AND (type:Person OR type:Group)")
        } returns ArtistSearchResponseDto(
            artists = listOf(ArtistDto(id = "abc", name = "Queen"))
        )
        coEvery { api.lookupArtist("abc") } returns ArtistDto(id = "abc", name = "Queen")

        val result = repository.searchArtists("queen", setOf(ArtistType.PERSON, ArtistType.GROUP))

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
    }

    @Test
    fun `getArtistDetail maps release groups from lookup response`() = runTest {
        coEvery { api.lookupArtist("abc") } returns ArtistDto(
            id = "abc",
            name = "Radiohead",
            country = "GB",
            releaseGroups = listOf(
                ReleaseGroupDto(id = "rg1", title = "OK Computer", primaryType = "Album", firstReleaseDate = "1997-05-21")
            )
        )

        val result = repository.getArtistDetail("abc")

        assertTrue(result.isSuccess)
        val detail = result.getOrThrow()
        assertEquals("Radiohead", detail.name)
        assertEquals("GB", detail.country)
        assertEquals(1, detail.releaseGroups.size)
        assertEquals("OK Computer", detail.releaseGroups[0].title)
    }

    @Test
    fun `getRecommendations resolves image url from representative release group`() = runTest {
        coEvery { api.lookupArtist(any()) } returns ArtistDto(
            id = "abc",
            name = "Radiohead",
            releaseGroups = listOf(
                ReleaseGroupDto(id = "rg1", title = "OK Computer", primaryType = "Album")
            )
        )
        coEvery { api.searchArtists(query = any()) } returns ArtistSearchResponseDto(
            artists = listOf(ArtistDto(id = "def", name = "Muse"))
        )

        val result = repository.getRecommendations()

        assertTrue(result.isSuccess)
        val recommendations = result.getOrThrow()
        assertTrue(recommendations.featured.isNotEmpty())
        assertTrue(recommendations.featured.all { it.imageUrl?.contains("coverartarchive.org") == true })
        assertTrue(recommendations.rock.isNotEmpty())
    }

    @Test
    fun `getRecommendations filters out Various Artists and unknown from genre sections`() = runTest {
        coEvery { api.lookupArtist(any()) } returns ArtistDto(id = "abc", name = "Radiohead")
        coEvery { api.searchArtists(query = any()) } returns ArtistSearchResponseDto(
            artists = listOf(
                ArtistDto(id = "89ad4ac3-39f7-470e-963a-56509c546377", name = "Various Artists"),
                ArtistDto(id = "125ec42a-7229-4250-afc5-e057484327fe", name = "[unknown]"),
                ArtistDto(id = "def", name = "Muse")
            )
        )

        val result = repository.getRecommendations()

        assertTrue(result.isSuccess)
        val recommendations = result.getOrThrow()
        assertEquals(1, recommendations.rock.size)
        assertEquals("def", recommendations.rock[0].id)
    }
}
