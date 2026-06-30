package com.copymanga.downloader.ui.screens.downloaded

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.copymanga.downloader.R
import com.copymanga.downloader.data.model.ComicDetail
import com.copymanga.downloader.di.AppContainer

@Composable
fun DownloadedScreen(
    container: AppContainer,
    onComicClick: (String) -> Unit,
) {
    val viewModel: DownloadedViewModel = viewModel(factory = DownloadedViewModel.provideFactory(container))
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            viewModel.isLoading && viewModel.comics.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            viewModel.comics.isEmpty() && !viewModel.isLoading -> {
                Text(
                    text = viewModel.errorMessage?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.downloaded_empty),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        items = viewModel.comics,
                        key = { it.pathWord },
                    ) { comic ->
                        DownloadedComicCard(
                            comic = comic,
                            onClick = { onComicClick(comic.pathWord) },
                            onShare = {
                                shareComicMetadata(context, viewModel.getComicMetadataFile(comic))
                            },
                        )
                    }
                }
            }
        }

        viewModel.errorMessage?.let { msg ->
            if (viewModel.comics.isNotEmpty()) {
                Card(
                    onClick = viewModel::dismissError,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = msg.takeIf { it.isNotBlank() } ?: stringResource(R.string.downloaded_scan_failed),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadedComicCard(
    comic: ComicDetail,
    onClick: () -> Unit,
    onShare: () -> Unit,
) {
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
                    text = stringResource(
                        R.string.downloaded_chapter_count,
                        comic.groups.values.sumOf { it.size },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.share))
            }
        }
    }
}

private fun shareComicMetadata(context: android.content.Context, file: java.io.File) {
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_metadata)))
}
