package com.copymanga.downloader.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.copymanga.downloader.R
import com.copymanga.downloader.data.model.ApiDomainMode
import com.copymanga.downloader.data.model.DownloadFormat
import com.copymanga.downloader.data.model.ExportSkipMode
import com.copymanga.downloader.di.AppContainer
import kotlinx.coroutines.launch

@Composable
fun SettingsDialog(
    container: AppContainer,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val configStore = container.configStore
    val initialConfig = remember { configStore.load() }

    var config by remember { mutableStateOf(initialConfig) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                SectionTitle(stringResource(R.string.settings_storage))
                PathTextField(
                    value = config.downloadDir,
                    onValueChange = { config = config.copy(downloadDir = it) },
                    label = stringResource(R.string.download_dir),
                )
                Spacer(modifier = Modifier.height(8.dp))
                PathTextField(
                    value = config.exportDir,
                    onValueChange = { config = config.copy(exportDir = it) },
                    label = stringResource(R.string.export_dir),
                )

                SectionTitle(stringResource(R.string.settings_network))
                EnumSelector(
                    label = stringResource(R.string.api_domain_mode),
                    values = ApiDomainMode.entries,
                    selected = config.apiDomainMode,
                    onSelect = { config = config.copy(apiDomainMode = it) },
                    labelMapper = {
                        if (it == ApiDomainMode.Default) {
                            stringResource(R.string.api_domain_default)
                        } else {
                            stringResource(R.string.api_domain_custom)
                        }
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = config.customApiDomain,
                    onValueChange = { config = config.copy(customApiDomain = it) },
                    label = { Text(stringResource(R.string.custom_api_domain)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                SectionTitle(stringResource(R.string.settings_download))
                EnumSelector(
                    label = stringResource(R.string.download_format),
                    values = DownloadFormat.entries,
                    selected = config.downloadFormat,
                    onSelect = { config = config.copy(downloadFormat = it) },
                    labelMapper = { it.name },
                )
                IntTextField(
                    value = config.chapterConcurrency,
                    onValueChange = { config = config.copy(chapterConcurrency = it.coerceAtLeast(1)) },
                    label = stringResource(R.string.chapter_concurrency),
                )
                Spacer(modifier = Modifier.height(8.dp))
                LongTextField(
                    value = config.chapterDownloadIntervalSec,
                    onValueChange = { config = config.copy(chapterDownloadIntervalSec = it.coerceAtLeast(0)) },
                    label = stringResource(R.string.chapter_interval),
                )
                Spacer(modifier = Modifier.height(8.dp))
                IntTextField(
                    value = config.imgConcurrency,
                    onValueChange = { config = config.copy(imgConcurrency = it.coerceAtLeast(1)) },
                    label = stringResource(R.string.img_concurrency),
                )
                Spacer(modifier = Modifier.height(8.dp))
                LongTextField(
                    value = config.imgDownloadIntervalSec,
                    onValueChange = { config = config.copy(imgDownloadIntervalSec = it.coerceAtLeast(0)) },
                    label = stringResource(R.string.img_interval),
                )
                Spacer(modifier = Modifier.height(8.dp))
                LongTextField(
                    value = config.updateDownloadedComicsIntervalSec,
                    onValueChange = { config = config.copy(updateDownloadedComicsIntervalSec = it.coerceAtLeast(0)) },
                    label = stringResource(R.string.update_downloaded_interval),
                )

                SectionTitle(stringResource(R.string.settings_dir_template))
                OutlinedTextField(
                    value = config.comicDirFmt,
                    onValueChange = { config = config.copy(comicDirFmt = it) },
                    label = { Text(stringResource(R.string.comic_dir_fmt)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = config.chapterDirFmt,
                    onValueChange = { config = config.copy(chapterDirFmt = it) },
                    label = { Text(stringResource(R.string.chapter_dir_fmt)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = config.exportDirFmt,
                    onValueChange = { config = config.copy(exportDirFmt = it) },
                    label = { Text(stringResource(R.string.export_dir_fmt)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = config.mergePdfFmt,
                    onValueChange = { config = config.copy(mergePdfFmt = it) },
                    label = { Text(stringResource(R.string.merge_pdf_fmt)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                SectionTitle(stringResource(R.string.settings_export))
                RowSwitch(
                    label = stringResource(R.string.enable_merge_pdf),
                    checked = config.enableMergePdf,
                    onCheckedChange = { config = config.copy(enableMergePdf = it) },
                )
                Spacer(modifier = Modifier.height(8.dp))
                IntTextField(
                    value = config.createPdfConcurrency,
                    onValueChange = { config = config.copy(createPdfConcurrency = it.coerceAtLeast(1)) },
                    label = stringResource(R.string.create_pdf_concurrency),
                )
                Spacer(modifier = Modifier.height(8.dp))
                EnumSelector(
                    label = stringResource(R.string.export_skip_mode),
                    values = ExportSkipMode.entries,
                    selected = config.exportSkipMode,
                    onSelect = { config = config.copy(exportSkipMode = it) },
                    labelMapper = {
                        when (it) {
                            ExportSkipMode.None -> stringResource(R.string.export_skip_none)
                            ExportSkipMode.SkipExisting -> stringResource(R.string.export_skip_existing)
                            ExportSkipMode.SkipExported -> stringResource(R.string.export_skip_exported)
                        }
                    },
                )

                SectionTitle(stringResource(R.string.settings_other))
                RowSwitch(
                    label = stringResource(R.string.enable_file_logger),
                    checked = config.enableFileLogger,
                    onCheckedChange = { config = config.copy(enableFileLogger = it) },
                )

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        configStore.save(config)
                        onDismiss()
                    }
                },
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun SectionTitle(title: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(4.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun PathTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun IntTextField(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = {
            onValueChange(it.toIntOrNull() ?: value)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun LongTextField(
    value: Long,
    onValueChange: (Long) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = {
            onValueChange(it.toLongOrNull() ?: value)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun <T> EnumSelector(
    label: String,
    values: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    labelMapper: (T) -> String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        RowSelector(
            values = values,
            selected = selected,
            onSelect = onSelect,
            labelMapper = labelMapper,
        )
    }
}

@Composable
private fun <T> RowSelector(
    values: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    labelMapper: (T) -> String,
) {
    Column {
        values.forEach { value ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(value) }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = selected == value,
                    onClick = { onSelect(value) },
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = labelMapper(value),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun RowSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
