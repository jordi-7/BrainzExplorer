package com.jordigordillo.brainzexplorer.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.jordigordillo.brainzexplorer.data.local.ReleaseGroupSortPreferences
import com.jordigordillo.brainzexplorer.domain.model.ArtistDetail
import com.jordigordillo.brainzexplorer.domain.model.ReleaseGroupSortOrder
import com.jordigordillo.brainzexplorer.domain.repository.ArtistRepository
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Runs under Robolectric because [ArtistDetailViewModel] decodes its route via
 * [androidx.navigation.SavedStateHandle.toRoute], which needs a real android.os.Bundle
 * (unavailable in plain JVM unit tests).
 */
@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class ArtistDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository: ArtistRepository = mockk()
    private val sortPreferences: ReleaseGroupSortPreferences = mockk()

    private val artistDetail = ArtistDetail(
        id = "abc",
        name = "Radiohead",
        disambiguation = null,
        country = "GB",
        releaseGroups = emptyList()
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { sortPreferences.sortOrder } returns flowOf(ReleaseGroupSortOrder.NEWEST_FIRST)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun savedStateHandle(artistId: String = "abc", artistName: String? = "Radiohead") =
        SavedStateHandle(mapOf("artistId" to artistId, "artistName" to artistName))

    private fun viewModel() = ArtistDetailViewModel(savedStateHandle(), repository, sortPreferences)

    @Test
    fun `loads artist detail successfully on init`() = runTest(dispatcher) {
        coEvery { repository.getArtistDetail("abc") } returns Result.success(artistDetail)

        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(ArtistDetailState.Loading, awaitItem().detailState)
            val success = awaitItem().detailState
            assertTrue(success is ArtistDetailState.Success)
            assertEquals(artistDetail, (success as ArtistDetailState.Success).detail)
        }
    }

    @Test
    fun `load failure surfaces Error state`() = runTest(dispatcher) {
        coEvery { repository.getArtistDetail("abc") } returns Result.failure(RuntimeException("boom"))

        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            assertEquals(ArtistDetailState.Error, awaitItem().detailState)
        }
    }

    @Test
    fun `refresh reloads the artist detail`() = runTest(dispatcher) {
        coEvery { repository.getArtistDetail("abc") } returns Result.success(artistDetail)

        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success

            viewModel.refresh()
            assertEquals(ArtistDetailState.Loading, awaitItem().detailState)
            val success = awaitItem().detailState
            assertTrue(success is ArtistDetailState.Success)
        }
    }

    @Test
    fun `toggling a release group type adds and removes it from selection`() = runTest(dispatcher) {
        coEvery { repository.getArtistDetail("abc") } returns Result.success(artistDetail)

        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success

            viewModel.onTypeToggle("Album")
            assertEquals(setOf("Album"), awaitItem().selectedTypes)

            viewModel.onTypeToggle("EP")
            assertEquals(setOf("Album", "EP"), awaitItem().selectedTypes)

            viewModel.onTypeToggle("Album")
            assertEquals(setOf("EP"), awaitItem().selectedTypes)
        }
    }

    @Test
    fun `selecting a sort order updates state immediately and persists it`() = runTest(dispatcher) {
        coEvery { repository.getArtistDetail("abc") } returns Result.success(artistDetail)
        coEvery { sortPreferences.setSortOrder(ReleaseGroupSortOrder.OLDEST_FIRST) } returns Unit

        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success

            viewModel.onSortOrderSelected(ReleaseGroupSortOrder.OLDEST_FIRST)
            assertEquals(ReleaseGroupSortOrder.OLDEST_FIRST, awaitItem().sortOrder)
        }
        dispatcher.scheduler.advanceUntilIdle()
        coVerify { sortPreferences.setSortOrder(ReleaseGroupSortOrder.OLDEST_FIRST) }
    }

    @Test
    fun `sort order preference is reflected in ui state on init`() = runTest(dispatcher) {
        val sortOrderFlow = MutableStateFlow(ReleaseGroupSortOrder.NEWEST_FIRST)
        every { sortPreferences.sortOrder } returns sortOrderFlow
        coEvery { repository.getArtistDetail("abc") } returns Result.success(artistDetail)

        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success

            sortOrderFlow.value = ReleaseGroupSortOrder.OLDEST_FIRST
            assertEquals(ReleaseGroupSortOrder.OLDEST_FIRST, awaitItem().sortOrder)
        }
    }
}
