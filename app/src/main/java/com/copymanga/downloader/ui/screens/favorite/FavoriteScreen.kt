package com.copymanga.downloader.ui.screens.favorite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.copymanga.downloader.R
import com.copymanga.downloader.data.model.FavoriteItem
import com.copymanga.downloader.data.model.GetFavoriteOrdering
import com.copymanga.downloader.di.AppContainer

@Composable
fun FavoriteScreen(
    container: AppContainer,
    onComicClick: (String) -> Unit,
) {
    val viewModel: FavoriteViewModel = viewModel(factory = FavoriteViewModel.provideFactory(container))
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisible >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMore()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OrderingSelector(
            selected = viewModel.ordering,
            onSelect = viewModel::updateOrdering,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                viewModel.isLoading && viewModel.results.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                viewModel.results.isEmpty() && !viewModel.isLoading -> {
                    Text(
                        text = viewModel.errorMessage?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.favorite_empty),
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            items = viewModel.results,
                            key = { it.comic.pathWord },
                        ) { item ->
                            FavoriteCard(
                                item = item,
                                onClick = { onComicClick(item.comic.pathWord) },
                            )
                        }
                        if (viewModel.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }

            viewModel.errorMessage?.let { msg ->
                if (viewModel.results.isNotEmpty()) {
                    Card(
                        onClick = viewModel::dismissError,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth(),
                    ) {
                        Text(
                            text = msg.takeIf { it.isNotBlank() } ?: stringResource(R.string.favorite_load_failed),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderingSelector(
    selected: GetFavoriteOrdering,
    onSelect: (GetFavoriteOrdering) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GetFavoriteOrdering.entries.forEach { ordering ->
            FilterChip(
                selected = selected == ordering,
                onClick = { onSelect(ordering) },
                label = { Text(ordering.label()) },
            )
        }
    }
}

@Composable
private fun GetFavoriteOrdering.label(): String = when (this) {
    GetFavoriteOrdering.Added -> stringResource(R.string.favorite_ordering_added_label)
    GetFavoriteOrdering.Updated -> stringResource(R.string.favorite_ordering_updated_label)
    GetFavoriteOrdering.Read -> stringResource(R.string.favorite_ordering_read_label)
}

@Composable
private fun FavoriteCard(
    item: FavoriteItem,
    onClick: () -> Unit,
) {
    val comic = item.comic
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(comic.cover)
                    .crossfade(true)
                    .build(),
                contentDescription = comic.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 80.dp, height = 110.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = comic.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comic.author.joinToString(", ") { it.name }
                        .ifEmpty { stringResource(R.string.unknown_author) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.last_chapter, comic.lastChapterName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
