package com.copymanga.downloader.ui.screens.downloaded

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.copymanga.downloader.data.model.ComicDetail
import com.copymanga.downloader.data.store.ConfigStore
import com.copymanga.downloader.data.store.FileManager
import com.copymanga.downloader.di.AppContainer
import com.copymanga.downloader.domain.repository.DownloadRepository
import kotlinx.coroutines.launch
import java.io.File

class DownloadedViewModel(
    private val downloadRepository: DownloadRepository,
    private val configStore: ConfigStore,
    private val fileManager: FileManager,
) : ViewModel() {

    var comics by mutableStateOf<List<ComicDetail>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            runCatching {
                downloadRepository.scanDownloadedComics()
            }.onSuccess {
                comics = it
            }.onFailure { e ->
                errorMessage = e.message ?: ""
            }
            isLoading = false
        }
    }

    fun dismissError() {
        errorMessage = null
    }

    fun getComicMetadataFile(comic: ComicDetail): File {
        val config = configStore.load()
        val comicDir = fileManager.comicDir(config, comic)
        return File(comicDir, "元数据.json")
    }

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DownloadedViewModel(
                        container.downloadRepository,
                        container.configStore,
                        container.fileManager,
                    ) as T
                }
            }
    }
}
