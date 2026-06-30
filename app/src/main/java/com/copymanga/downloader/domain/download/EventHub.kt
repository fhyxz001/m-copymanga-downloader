package com.copymanga.downloader.domain.download

import com.copymanga.downloader.data.model.Comic
import com.copymanga.downloader.data.model.ChapterInfo
import com.copymanga.downloader.data.model.DownloadTaskState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class DownloadEvent {
    data class Speed(val speed: String) : DownloadEvent()
    data class RiskControl(val chapterUuid: String, val retryAfter: Int) : DownloadEvent()
    data class Sleeping(val chapterUuid: String, val remainingSec: Long) : DownloadEvent()
    data class TaskCreate(
        val state: DownloadTaskState,
        val comic: Comic,
        val chapterInfo: ChapterInfo,
        val downloadedImgCount: Int,
        val totalImgCount: Int,
    ) : DownloadEvent()
    data class TaskDelete(val chapterUuid: String) : DownloadEvent()
    data class TaskUpdate(
        val chapterUuid: String,
        val state: DownloadTaskState,
        val downloadedImgCount: Int,
        val totalImgCount: Int,
    ) : DownloadEvent()
}

class EventHub {
    private val _events = MutableSharedFlow<DownloadEvent>(extraBufferCapacity = 128)
    val events = _events.asSharedFlow()

    fun emit(event: DownloadEvent) {
        _events.tryEmit(event)
    }
}
