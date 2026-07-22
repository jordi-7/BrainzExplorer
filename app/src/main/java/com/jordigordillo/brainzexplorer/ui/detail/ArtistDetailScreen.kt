package com.jordigordillo.brainzexplorer.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jordigordillo.brainzexplorer.R
import com.jordigordillo.brainzexplorer.domain.model.ArtistDetail
import com.jordigordillo.brainzexplorer.domain.model.ReleaseGroup
import com.jordigordillo.brainzexplorer.domain.model.ReleaseGroupSortOrder
import com.jordigordillo.brainzexplorer.domain.model.availableTypes
import com.jordigordillo.brainzexplorer.domain.model.filterByTypes
import com.jordigordillo.brainzexplorer.domain.model.sortedByDate
import com.jordigordillo.brainzexplorer.ui.components.MessageState
import com.jordigordillo.brainzexplorer.ui.components.ReleaseGroupGridItem
import com.jordigordillo.brainzexplorer.ui.components.SkeletonBox

@Composable
fun ArtistDetailScreen(
    onBack: () -> Unit,
    viewModel: ArtistDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ArtistDetailScreenContent(
        uiState = uiState,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onTypeToggle = viewModel::onTypeToggle,
        onSortOrderSelected = viewModel::onSortOrderSelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArtistDetailScreenContent(
    uiState: ArtistDetailUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onTypeToggle: (String) -> Unit,
    onSortOrderSelected: (ReleaseGroupSortOrder) -> Unit
) {
    var showSortSheet by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(uiState.artistName ?: stringResource(R.string.detail_title_placeholder)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back_content_description)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSortSheet = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.detail_sort_content_description)
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
            isRefreshing = uiState.detailState == ArtistDetailState.Loading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState.detailState) {
                is ArtistDetailState.Loading -> ArtistDetailSkeleton()
                is ArtistDetailState.Error -> MessageState(
                    icon = Icons.Filled.ErrorOutline,
                    title = stringResource(R.string.load_error_title),
                    subtitle = stringResource(R.string.load_error_subtitle)
                )
                is ArtistDetailState.Success -> ArtistDetailGrid(
                    detail = state.detail,
                    selectedTypes = uiState.selectedTypes,
                    sortOrder = uiState.sortOrder,
                    onTypeToggle = onTypeToggle
                )
            }
        }

        if (showSortSheet) {
            SortOptionsBottomSheet(
                currentOrder = uiState.sortOrder,
                onOrderSelected = {
                    onSortOrderSelected(it)
                    showSortSheet = false
                },
                onDismiss = { showSortSheet = false }
            )
        }
    }
}

@Composable
private fun ArtistDetailGrid(
    detail: ArtistDetail,
    selectedTypes: Set<String>,
    sortOrder: ReleaseGroupSortOrder,
    onTypeToggle: (String) -> Unit
) {
    val visibleReleaseGroups = remember(detail.releaseGroups, selectedTypes, sortOrder) {
        detail.releaseGroups.filterByTypes(selectedTypes).sortedByDate(sortOrder)
    }
    val availableTypes = remember(detail.releaseGroups) {
        detail.releaseGroups.availableTypes()
    }
    val gridState = rememberLazyStaggeredGridState()
    LaunchedEffect(sortOrder) {
        gridState.scrollToItem(0)
    }

    Column(Modifier.fillMaxSize()) {
        if (availableTypes.isNotEmpty()) {
            ReleaseGroupTypeFilterRow(
                availableTypes = availableTypes,
                selectedTypes = selectedTypes,
                onTypeToggle = onTypeToggle
            )
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            state = gridState,
            contentPadding = PaddingValues(
                top = 4.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                start = 16.dp,
                end = 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalItemSpacing = 16.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            if (visibleReleaseGroups.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    MessageState(
                        icon = Icons.Filled.ErrorOutline,
                        title = stringResource(R.string.release_groups_empty_title),
                        subtitle = stringResource(R.string.release_groups_empty_subtitle)
                    )
                }
            } else {
                items(
                    items = visibleReleaseGroups,
                    key = { it.id }
                ) { releaseGroup ->
                    ReleaseGroupGridItem(releaseGroup)
                }
            }
        }
    }
}

@Composable
private fun ReleaseGroupTypeFilterRow(
    availableTypes: List<String>,
    selectedTypes: Set<String>,
    onTypeToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(availableTypes) { type ->
            FilterChip(
                selected = type in selectedTypes,
                onClick = { onTypeToggle(type) },
                label = { Text(stringResource(type.labelRes())) },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

private fun String.labelRes(): Int = when (this) {
    "Album" -> R.string.filter_type_album
    "Single" -> R.string.filter_type_single
    "EP" -> R.string.filter_type_ep
    "Broadcast" -> R.string.filter_type_broadcast
    "Other" -> R.string.filter_type_other
    "Audio drama" -> R.string.filter_type_audio_drama
    "Audiobook" -> R.string.filter_type_audiobook
    "Compilation" -> R.string.filter_type_compilation
    "Demo" -> R.string.filter_type_demo
    "DJ-mix" -> R.string.filter_type_dj_mix
    "Field recording" -> R.string.filter_type_field_recording
    "Interview" -> R.string.filter_type_interview
    "Live" -> R.string.filter_type_live
    "Mixtape/Street" -> R.string.filter_type_mixtape_street
    "Remix" -> R.string.filter_type_remix
    "Soundtrack" -> R.string.filter_type_soundtrack
    "Spokenword" -> R.string.filter_type_spokenword
    else -> R.string.filter_type_other
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortOptionsBottomSheet(
    currentOrder: ReleaseGroupSortOrder,
    onOrderSelected: (ReleaseGroupSortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(Modifier.padding(bottom = 16.dp)) {
            SortOptionRow(
                label = stringResource(R.string.sort_newest_first),
                selected = currentOrder == ReleaseGroupSortOrder.NEWEST_FIRST,
                onClick = { onOrderSelected(ReleaseGroupSortOrder.NEWEST_FIRST) }
            )
            SortOptionRow(
                label = stringResource(R.string.sort_oldest_first),
                selected = currentOrder == ReleaseGroupSortOrder.OLDEST_FIRST,
                onClick = { onOrderSelected(ReleaseGroupSortOrder.OLDEST_FIRST) }
            )
        }
    }
}

@Composable
private fun SortOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun ArtistDetailSkeleton() {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(
            top = 4.dp,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            start = 16.dp,
            end = 16.dp
        ),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        items(20) {
            SkeletonBox(Modifier.fillMaxWidth().aspectRatio(1f))
        }
    }
}

private val previewReleaseGroups = listOf(
    ReleaseGroup(
        id = "1",
        title = "Antimatter",
        primaryType = "Single",
        firstReleaseDate = "2023"
    ),
    ReleaseGroup(
        id = "2",
        title = "When The End Began",
        primaryType = "Album",
        firstReleaseDate = "2018"
    ),
    ReleaseGroup(
        id = "3",
        title = "Everything Was Sound",
        primaryType = "Album",
        firstReleaseDate = "2016"
    )
)

@Preview(showBackground = true)
@Composable
private fun ArtistDetailScreenLoadedPreview() {
    ArtistDetailScreenContent(
        uiState = ArtistDetailUiState(
            artistName = "Silent Planet",
            detailState = ArtistDetailState.Success(
                ArtistDetail(
                    id = "1",
                    name = "Silent Planet",
                    disambiguation = "",
                    country = "",
                    releaseGroups = previewReleaseGroups
                )
            )
        ),
        onBack = {},
        onRefresh = {},
        onTypeToggle = {},
        onSortOrderSelected = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ArtistDetailScreenLoadingPreview() {
    ArtistDetailScreenContent(
        uiState = ArtistDetailUiState(artistName = "Architects"),
        onBack = {},
        onRefresh = {},
        onTypeToggle = {},
        onSortOrderSelected = {}
    )
}
