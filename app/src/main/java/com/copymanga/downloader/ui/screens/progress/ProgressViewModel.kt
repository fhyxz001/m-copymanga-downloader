package com.copymanga.downloader.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.copymanga.downloader.data.model.DownloadTaskState
import com.copymanga.downloader.di.AppContainer
import com.copymanga.downloader.domain.download.ProgressData
import com.copymanga.downloader.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ProgressViewModel(
    private val downloadRepository: DownloadRepository,
) : ViewModel() {

    val tasks: StateFlow<Map<String, ProgressData>> = downloadRepository.progress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap(),
        )

    val speed: StateFlow<String> = downloadRepository.speed
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "0.00MB/s",
        )

    fun pause(chapterUuid: String) = downloadRepository.pause(chapterUuid)

    fun resume(chapterUuid: String) = downloadRepository.resume(chapterUuid)

    fun delete(chapterUuid: String) = downloadRepository.delete(chapterUuid)

    fun clearCompleted() {
        val completed = tasks.value.filter { it.value.state == DownloadTaskState.Completed }.keys
        completed.forEach { delete(it) }
    }

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProgressViewModel(container.downloadRepository) as T
                }
            }
    }
}
