package com.copymanga.downloader.ui.screens.chapter

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.copymanga.downloader.data.model.ChapterInfo
import com.copymanga.downloader.data.model.Comic
import com.copymanga.downloader.data.model.DownloadFormat
import com.copymanga.downloader.data.model.Group
import com.copymanga.downloader.di.AppContainer

@Composable
fun ChapterScreen(
    container: AppContainer,
    pathWord: String,
) {
    val viewModel: ChapterViewModel = viewModel(factory = ChapterViewModel.provideFactory(container))

    LaunchedEffect(pathWord) {
        viewModel.load(pathWord)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            pathWord.isBlank() -> {
                EmptyState(stringResource(R.string.chapter_select_comic_hint))
            }

            viewModel.isLoading && viewModel.comic == null -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            viewModel.comic == null -> {
                EmptyState(viewModel.errorMessage ?: stringResource(R.string.chapter_load_failed))
            }

            else -> {
                ChapterContent(viewModel = viewModel)
            }
        }

        viewModel.errorMessage?.let { msg ->
            Card(
                onClick = viewModel::dismissError,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = msg.takeIf { it.isNotBlank() } ?: stringResource(R.string.chapter_load_failed),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ChapterContent(viewModel: ChapterViewModel) {
    val comic = viewModel.comic ?: return
    val groups = viewModel.currentGroups()
    val chapters = viewModel.currentChapters()

    Column(modifier = Modifier.fillMaxSize()) {
        ComicHeader(comic = comic)

        if (groups.size > 1) {
            GroupTabs(
                groups = groups,
                selectedGroup = viewModel.selectedGroup,
                onSelect = viewModel::selectGroup,
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(
                items = chapters,
                key = { it.chapterUuid },
            ) { chapter ->
                ChapterRow(
                    chapter = chapter,
                    isSelected = chapter.chapterUuid in viewModel.selectedUuids,
                    isSelectionMode = viewModel.isSelectionMode,
                    onClick = {
                        if (viewModel.isSelectionMode) {
                            viewModel.toggleSelection(chapter.chapterUuid)
                        }
                    },
                    onLongClick = {
                        viewModel.enterSelection(chapter.chapterUuid)
                    },
                )
            }
        }

        if (viewModel.isSelectionMode) {
            SelectionBottomBar(
                selectedCount = viewModel.selectedUuids.size,
                onSelectAll = viewModel::selectAll,
                onInvert = viewModel::invertSelection,
                onClear = viewModel::clearSelection,
                onDownload = viewModel::downloadSelected,
                onExportCbz = { viewModel.exportSelected(DownloadFormat.Webp) },
                onExportPdf = { viewModel.exportSelected(DownloadFormat.Jpeg) },
            )
        }
    }
}

@Composable
private fun ComicHeader(comic: Comic) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(comic.comic.cover)
                    .crossfade(true)
                    .build(),
                contentDescription = comic.comic.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 100.dp, height = 140.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = comic.comic.name,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comic.comic.author.joinToString(", ") { it.name }
                        .ifEmpty { stringResource(R.string.unknown_author) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = comic.comic.brief,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun GroupTabs(
    groups: List<Pair<String, Group>>,
    selectedGroup: String?,
    onSelect: (String) -> Unit,
) {
    val selectedIndex = groups.indexOfFirst { it.first == selectedGroup }.coerceAtLeast(0)
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 16.dp,
    ) {
        groups.forEach { (pathWord, group) ->
            Tab(
                selected = selectedGroup == pathWord,
                onClick = { onSelect(pathWord) },
                text = { Text(group.name) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChapterRow(
    chapter: ChapterInfo,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.chapterTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (chapter.isDownloaded == true) {
                    Text(
                        text = stringResource(R.string.chapter_downloaded),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionBottomBar(
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onInvert: () -> Unit,
    onClear: () -> Unit,
    onDownload: () -> Unit,
    onExportCbz: () -> Unit,
    onExportPdf: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.selected_count, selectedCount),
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = false,
                onClick = onSelectAll,
                label = { Text(stringResource(R.string.select_all)) },
            )
            FilterChip(
                selected = false,
                onClick = onInvert,
                label = { Text(stringResource(R.string.invert_selection)) },
            )
            FilterChip(
                selected = false,
                onClick = onClear,
                label = { Text(stringResource(R.string.deselect)) },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTonalButton(
                onClick = onDownload,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Outlined.Book, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.download))
            }
            FilledTonalButton(
                onClick = onExportCbz,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.export_cbz_short))
            }
            FilledTonalButton(
                onClick = onExportPdf,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.export_pdf_short))
            }
        }
    }
}
