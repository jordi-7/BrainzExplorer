package com.jordigordillo.brainzexplorer.ui.home

import app.cash.turbine.test
import com.jordigordillo.brainzexplorer.domain.model.ArtistSummary
import com.jordigordillo.brainzexplorer.domain.model.ArtistType
import com.jordigordillo.brainzexplorer.domain.model.Recommendations
import com.jordigordillo.brainzexplorer.domain.repository.ArtistRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository: ArtistRepository = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val emptyRecommendations = Recommendations(
        featured = emptyList(),
        rock = emptyList(),
        pop = emptyList(),
        electronic = emptyList(),
    )

    @Test
    fun `loads recommendations successfully on init`() = runTest(dispatcher) {
        coEvery { repository.getRecommendations() } returns Result.success(emptyRecommendations)

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            assertEquals(RecommendationsState.Loading, awaitItem().recommendationsState)
            val success = awaitItem().recommendationsState
            assertTrue(success is RecommendationsState.Success)
        }
    }

    @Test
    fun `recommendations failure surfaces error state`() = runTest(dispatcher) {
        coEvery { repository.getRecommendations() } returns Result.failure(RuntimeException("boom"))

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // Loading
            val errorState = awaitItem().recommendationsState
            assertTrue(errorState is RecommendationsState.Error)
        }
    }

    @Test
    fun `search submit transitions Idle to Loading to Success`() = runTest(dispatcher) {
        coEvery { repository.getRecommendations() } returns Result.success(emptyRecommendations)
        coEvery { repository.searchArtists("radiohead") } returns Result.success(
            listOf(ArtistSummary(id = "abc", name = "Radiohead", disambiguation = null, imageUrl = null)),
        )

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // initial Loading recommendations
            awaitItem() // recommendations Success

            viewModel.onSearchActiveChanged(true)
            assertTrue(awaitItem().isSearchActive)

            viewModel.onSearchQueryChanged("radiohead")
            awaitItem() // query updated

            viewModel.onSearchSubmit()
            assertEquals(SearchState.Loading, awaitItem().searchState)
            val successState = awaitItem().searchState
            assertTrue(successState is SearchState.Success)
            assertEquals(1, (successState as SearchState.Success).results.size)
        }
    }

    @Test
    fun `toggling an artist type filter adds and removes it from selection`() = runTest(dispatcher) {
        coEvery { repository.getRecommendations() } returns Result.success(emptyRecommendations)

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // recommendations Loading
            awaitItem() // recommendations Success

            viewModel.onArtistTypeFilterToggled(ArtistType.GROUP)
            assertEquals(setOf(ArtistType.GROUP), awaitItem().selectedArtistTypes)

            viewModel.onArtistTypeFilterToggled(ArtistType.PERSON)
            assertEquals(setOf(ArtistType.GROUP, ArtistType.PERSON), awaitItem().selectedArtistTypes)

            viewModel.onArtistTypeFilterToggled(ArtistType.GROUP)
            assertEquals(setOf(ArtistType.PERSON), awaitItem().selectedArtistTypes)
        }
    }

    @Test
    fun `search submit passes selected artist type filters to the repository`() = runTest(dispatcher) {
        coEvery { repository.getRecommendations() } returns Result.success(emptyRecommendations)
        coEvery {
            repository.searchArtists("radiohead", setOf(ArtistType.GROUP))
        } returns Result.success(
            listOf(ArtistSummary(id = "abc", name = "Radiohead", disambiguation = null, imageUrl = null))
        )

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // recommendations Loading
            awaitItem() // recommendations Success

            viewModel.onSearchActiveChanged(true)
            awaitItem()

            viewModel.onArtistTypeFilterToggled(ArtistType.GROUP)
            awaitItem()

            viewModel.onSearchQueryChanged("radiohead")
            awaitItem()

            viewModel.onSearchSubmit()
            assertEquals(SearchState.Loading, awaitItem().searchState)
            val successState = awaitItem().searchState
            assertTrue(successState is SearchState.Success)
        }
    }

    @Test
    fun `collapsing search resets selected artist type filters`() = runTest(dispatcher) {
        coEvery { repository.getRecommendations() } returns Result.success(emptyRecommendations)

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // recommendations Loading
            awaitItem() // recommendations Success

            viewModel.onSearchActiveChanged(true)
            awaitItem()

            viewModel.onArtistTypeFilterToggled(ArtistType.GROUP)
            awaitItem()

            viewModel.onSearchActiveChanged(false)
            assertEquals(emptySet<ArtistType>(), awaitItem().selectedArtistTypes)
        }
    }

    @Test
    fun `search submit with no results yields Empty state`() = runTest(dispatcher) {
        coEvery { repository.getRecommendations() } returns Result.success(emptyRecommendations)
        coEvery { repository.searchArtists("zzz") } returns Result.success(emptyList())

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // recommendations Success

            viewModel.onSearchActiveChanged(true)
            awaitItem()
            viewModel.onSearchQueryChanged("zzz")
            awaitItem()
            viewModel.onSearchSubmit()
            awaitItem() // Loading
            assertEquals(SearchState.Empty, awaitItem().searchState)
        }
    }
}
