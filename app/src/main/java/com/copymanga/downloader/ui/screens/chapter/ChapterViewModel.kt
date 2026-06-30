package com.copymanga.downloader.ui.screens.chapter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.copymanga.downloader.data.model.ChapterInfo
import com.copymanga.downloader.data.model.Comic
import com.copymanga.downloader.data.model.DownloadFormat
import com.copymanga.downloader.data.model.Group
import com.copymanga.downloader.di.AppContainer
import com.copymanga.downloader.domain.repository.CopyRepository
import com.copymanga.downloader.domain.repository.DownloadRepository
import kotlinx.coroutines.launch

class ChapterViewModel(
    private val copyRepository: CopyRepository,
    private val downloadRepository: DownloadRepository,
) : ViewModel() {

    var comic by mutableStateOf<Comic?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var selectedGroup by mutableStateOf<String?>(null)
        private set

    var selectedUuids by mutableStateOf<Set<String>>(emptySet())
        private set

    var isSelectionMode by mutableStateOf(false)
        private set

    private var loadedPathWord: String = ""

    fun load(pathWord: String) {
        if (pathWord.isBlank() || pathWord == loadedPathWord) return
        loadedPathWord = pathWord
        isLoading = true
        errorMessage = null
        comic = null
        selectedGroup = null
        clearSelection()
        viewModelScope.launch {
            copyRepository.getComic(pathWord)
                .onSuccess {
                    comic = it
                    selectedGroup = it.groups.keys.firstOrNull()
                }
                .onFailure { e ->
                    errorMessage = e.message ?: ""
                }
            isLoading = false
        }
    }

    fun selectGroup(groupPathWord: String) {
        selectedGroup = groupPathWord
    }

    fun enterSelection(uuid: String) {
        isSelectionMode = true
        toggleSelection(uuid)
    }

    fun toggleSelection(uuid: String) {
        selectedUuids = if (uuid in selectedUuids) {
            selectedUuids - uuid
        } else {
            selectedUuids + uuid
        }
        if (selectedUuids.isEmpty()) {
            isSelectionMode = false
        }
    }

    fun selectAll() {
        val groupChapters = currentChapters()
        selectedUuids = groupChapters.map { it.chapterUuid }.toSet()
    }

    fun invertSelection() {
        val groupChapters = currentChapters()
        val allUuids = groupChapters.map { it.chapterUuid }.toSet()
        selectedUuids = allUuids - selectedUuids
        if (selectedUuids.isEmpty()) {
            isSelectionMode = false
        }
    }

    fun clearSelection() {
        selectedUuids = emptySet()
        isSelectionMode = false
    }

    fun downloadSelected() {
        val currentComic = comic ?: return
        val chapters = selectedChapters()
        if (chapters.isEmpty()) return
        downloadRepository.startDownload(
            currentComic,
            chapters.map { it to it.groupName },
        )
        clearSelection()
    }

    fun exportSelected(format: DownloadFormat) {
        val currentComic = comic ?: return
        val chapters = selectedChapters()
        if (chapters.isEmpty()) return
        viewModelScope.launch {
            runCatching {
                when (format) {
                    DownloadFormat.Webp -> downloadRepository.exportCbz(currentComic, chapters)
                    DownloadFormat.Jpeg -> downloadRepository.exportPdf(currentComic, chapters)
                }
            }.onFailure { e ->
                errorMessage = e.message ?: ""
            }
        }
        clearSelection()
    }

    fun dismissError() {
        errorMessage = null
    }

    fun currentGroups(): List<Pair<String, Group>> {
        return comic?.groups?.toList() ?: emptyList()
    }

    fun currentChapters(): List<ChapterInfo> {
        val group = selectedGroup ?: return emptyList()
        return comic?.comic?.groups?.get(group) ?: emptyList()
    }

    private fun selectedChapters(): List<ChapterInfo> {
        return currentChapters().filter { it.chapterUuid in selectedUuids }
    }

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChapterViewModel(
                        container.copyRepository,
                        container.downloadRepository,
                    ) as T
                }
            }
    }
}
