package com.jordigordillo.brainzexplorer.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jordigordillo.brainzexplorer.R
import com.jordigordillo.brainzexplorer.domain.model.ArtistSummary
import com.jordigordillo.brainzexplorer.domain.model.ArtistType
import com.jordigordillo.brainzexplorer.ui.components.ArtistCarouselItem
import com.jordigordillo.brainzexplorer.ui.components.ArtistCarouselItemSkeleton
import com.jordigordillo.brainzexplorer.ui.components.ArtistResultRow
import com.jordigordillo.brainzexplorer.ui.components.ArtistResultRowSkeleton
import com.jordigordillo.brainzexplorer.ui.components.MessageState
import com.jordigordillo.brainzexplorer.ui.components.SkeletonBox
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jordigordillo.brainzexplorer.domain.model.Recommendations
import com.jordigordillo.brainzexplorer.ui.components.CollapsibleSearchField

@Composable
fun HomeScreen(
    onArtistClick: (String, String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        onSearchActiveChanged = viewModel::onSearchActiveChanged,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSearchSubmit = viewModel::onSearchSubmit,
        onArtistTypeFilterToggled = viewModel::onArtistTypeFilterToggled,
        onRefresh = viewModel::onRefresh,
        onArtistClick = onArtistClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onSearchActiveChanged: (Boolean) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onArtistTypeFilterToggled: (ArtistType) -> Unit,
    onRefresh: () -> Unit,
    onArtistClick: (String, String) -> Unit
) {
    BackHandler(enabled = uiState.isSearchActive) {
        onSearchActiveChanged(false)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AnimatedVisibility(
                            visible = !uiState.isSearchActive,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        CollapsibleSearchField(
                            expanded = uiState.isSearchActive,
                            query = uiState.searchQuery,
                            onQueryChange = onSearchQueryChanged,
                            onExpandedChange = onSearchActiveChanged,
                            onSearch = onSearchSubmit,
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.recommendationsState == RecommendationsState.Loading
                    || uiState.searchState == SearchState.Loading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isSearchActive) {
                SearchBody(
                    searchState = uiState.searchState,
                    selectedTypes = uiState.selectedArtistTypes,
                    onTypeToggle = onArtistTypeFilterToggled,
                    onArtistClick = onArtistClick
                )
            } else {
                RecommendationsBody(
                    recommendationsState = uiState.recommendationsState,
                    onArtistClick = onArtistClick
                )
            }
        }
    }
}

@Composable
private fun RecommendationsBody(
    recommendationsState: RecommendationsState,
    onArtistClick: (String, String) -> Unit,
) {
    when (recommendationsState) {
        is RecommendationsState.Loading -> {
            LazyColumn(
                userScrollEnabled = false,
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(40.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(4) {
                    CarouselSectionSkeleton()
                }
            }
        }
        is RecommendationsState.Error -> {
            MessageState(
                icon = Icons.Filled.ErrorOutline,
                title = stringResource(R.string.load_error_title),
                subtitle = stringResource(R.string.load_error_subtitle)
            )
        }
        is RecommendationsState.Success -> {
            val recommendations = recommendationsState.recommendations
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(40.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    CarouselSection(
                        title = stringResource(R.string.carousel_section_title_featured),
                        artists = recommendations.featured,
                        onArtistClick = onArtistClick
                    )
                }
                item {
                    CarouselSection(
                        title = stringResource(R.string.carousel_section_title_rock),
                        artists = recommendations.rock,
                        onArtistClick = onArtistClick
                    )
                }
                item {
                    CarouselSection(
                        title = stringResource(R.string.carousel_section_title_pop),
                        artists = recommendations.pop,
                        onArtistClick = onArtistClick
                    )
                }
                item {
                    CarouselSection(
                        title = stringResource(R.string.carousel_section_title_electronic),
                        artists = recommendations.electronic,
                        onArtistClick = onArtistClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBody(
    searchState: SearchState,
    selectedTypes: Set<ArtistType>,
    onTypeToggle: (ArtistType) -> Unit,
    onArtistClick: (String, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ArtistTypeFilterRow(
            selectedTypes = selectedTypes,
            onTypeToggle = onTypeToggle,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            when (searchState) {
                is SearchState.Idle -> {
                    MessageState(
                        icon = Icons.Filled.Search,
                        title = stringResource(R.string.search_idle_title),
                        subtitle = stringResource(R.string.search_idle_subtitle)
                    )
                }
                is SearchState.Loading -> {
                    LazyColumn(
                        userScrollEnabled = false,
                        contentPadding = PaddingValues(
                            top = 4.dp,
                            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(20) { ArtistResultRowSkeleton() }
                    }
                }
                is SearchState.Empty -> {
                    MessageState(
                        icon = Icons.Filled.SearchOff,
                        title = stringResource(R.string.search_empty_title),
                        subtitle = stringResource(R.string.search_empty_subtitle)
                    )
                }
                is SearchState.Error -> {
                    MessageState(
                        icon = Icons.Filled.ErrorOutline,
                        title = stringResource(R.string.load_error_title),
                        subtitle = stringResource(R.string.load_error_subtitle)
                    )
                }
                is SearchState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 4.dp,
                            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = searchState.results,
                            key = { it.id }
                        ) { artist ->
                            ArtistResultRow(
                                artist = artist,
                                onClick = { onArtistClick(artist.id, artist.name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistTypeFilterRow(
    selectedTypes: Set<ArtistType>,
    onTypeToggle: (ArtistType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ArtistType.entries.forEach { type ->
            FilterChip(
                selected = type in selectedTypes,
                onClick = { onTypeToggle(type) },
                label = { Text(stringResource(type.labelRes())) },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

private fun ArtistType.labelRes(): Int = when (this) {
    ArtistType.PERSON -> R.string.filter_type_person
    ArtistType.GROUP -> R.string.filter_type_group
    ArtistType.ORCHESTRA -> R.string.filter_type_orchestra
    ArtistType.CHOIR -> R.string.filter_type_choir
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarouselSection(
    title: String,
    artists: List<ArtistSummary>,
    onArtistClick: (String, String) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )
        if (artists.isEmpty()) {
            Text(
                text = "No artists found for this section", // todo replace with better placeholder
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            return
        }
        val carouselState = rememberCarouselState { artists.size }
        HorizontalMultiBrowseCarousel(
            state = carouselState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            preferredItemWidth = 150.dp,
            itemSpacing = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) { index ->
            val artist = artists[index]
            ArtistCarouselItem(
                artist = artist,
                onClick = { onArtistClick(artist.id, artist.name) },
                modifier = Modifier.maskClip(RoundedCornerShape(24.dp))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarouselSectionSkeleton() {
    Column {
        SkeletonBox(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
                .height(40.dp)
                .fillMaxWidth(0.3f),
            shape = RoundedCornerShape(24.dp)
        )
        val carouselState = rememberCarouselState { 6 }
        HorizontalMultiBrowseCarousel(
            userScrollEnabled = false,
            state = carouselState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            preferredItemWidth = 150.dp,
            itemSpacing = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            ArtistCarouselItemSkeleton(Modifier.maskClip(RoundedCornerShape(24.dp)))
        }
    }
}

private val previewArtists = listOf(
    ArtistSummary(id = "1", name = "Architects", disambiguation = "British metal band", imageUrl = null),
    ArtistSummary(id = "2", name = "Silent Planet", disambiguation = null, imageUrl = null),
    ArtistSummary(id = "3", name = "Bad Omens", disambiguation = null, imageUrl = null),
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenRecommendationsLoadedPreview() {
    HomeScreenContent(
        uiState = HomeUiState(
            recommendationsState = RecommendationsState.Success(
                Recommendations(
                    featured = previewArtists,
                    rock = previewArtists,
                    pop = previewArtists,
                    electronic = previewArtists
                )
            )
        ),
        onSearchActiveChanged = {},
        onSearchQueryChanged = {},
        onSearchSubmit = {},
        onArtistTypeFilterToggled = {},
        onRefresh = {},
        onArtistClick = { _, _ -> }
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenRecommendationsLoadingPreview() {
    HomeScreenContent(
        uiState = HomeUiState(recommendationsState = RecommendationsState.Loading),
        onSearchActiveChanged = {},
        onSearchQueryChanged = {},
        onSearchSubmit = {},
        onArtistTypeFilterToggled = {},
        onRefresh = {},
        onArtistClick = { _, _ -> }
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenSearchLoadedPreview() {
    HomeScreenContent(
        uiState = HomeUiState(
            isSearchActive = true,
            searchQuery = "silent",
            searchState = SearchState.Success(previewArtists)
        ),
        onSearchActiveChanged = {},
        onSearchQueryChanged = {},
        onSearchSubmit = {},
        onArtistTypeFilterToggled = {},
        onRefresh = {},
        onArtistClick = { _, _ -> }
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenSearchLoadingPreview() {
    HomeScreenContent(
        uiState = HomeUiState(
            isSearchActive = true,
            searchQuery = "silent",
            searchState = SearchState.Loading
        ),
        onSearchActiveChanged = {},
        onSearchQueryChanged = {},
        onSearchSubmit = {},
        onArtistTypeFilterToggled = {},
        onRefresh = {},
        onArtistClick = { _, _ -> }
    )
}
