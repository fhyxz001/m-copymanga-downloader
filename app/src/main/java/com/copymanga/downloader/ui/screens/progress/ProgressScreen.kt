package com.copymanga.downloader.ui.screens.progress

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.copymanga.downloader.R
import com.copymanga.downloader.data.model.DownloadTaskState
import com.copymanga.downloader.di.AppContainer
import com.copymanga.downloader.domain.download.ProgressData

@Composable
fun ProgressScreen(container: AppContainer) {
    val viewModel: ProgressViewModel = viewModel(factory = ProgressViewModel.provideFactory(container))
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val speed by viewModel.speed.collectAsStateWithLifecycle()
    val taskList = tasks.values.toList()

    Column(modifier = Modifier.fillMaxSize()) {
        if (taskList.isNotEmpty()) {
            Text(
                text = speed,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                taskList.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.progress_empty),
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
                            items = taskList,
                            key = { it.chapterUuid },
                        ) { task ->
                            TaskCard(
                                task = task,
                                onPause = { viewModel.pause(task.chapterUuid) },
                                onResume = { viewModel.resume(task.chapterUuid) },
                                onDelete = { viewModel.delete(task.chapterUuid) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: ProgressData,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.comicTitle,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.chapterTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                when (task.state) {
                    DownloadTaskState.Pending,
                    DownloadTaskState.Downloading -> {
                        IconButton(onClick = onPause) {
                            Icon(Icons.Outlined.Pause, contentDescription = stringResource(R.string.pause))
                        }
                    }

                    DownloadTaskState.Paused,
                    DownloadTaskState.Failed -> {
                        IconButton(onClick = onResume) {
                            Icon(Icons.Outlined.PlayArrow, contentDescription = stringResource(R.string.resume))
                        }
                    }

                    DownloadTaskState.Completed -> {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { task.percentage / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(
                        R.string.progress_format,
                        task.downloadedImgCount,
                        task.totalImgCount,
                        task.percentage,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = task.indicator,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
