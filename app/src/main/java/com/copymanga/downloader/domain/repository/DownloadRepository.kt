package com.copymanga.downloader.domain.repository

import com.copymanga.downloader.data.model.ChapterInfo
import com.copymanga.downloader.data.model.Comic
import com.copymanga.downloader.data.model.ComicDetail
import com.copymanga.downloader.data.store.FileManager
import com.copymanga.downloader.data.store.MetadataStore
import com.copymanga.downloader.domain.download.DownloadManager
import com.copymanga.downloader.domain.download.ProgressData
import com.copymanga.downloader.domain.export.ExportManager
import kotlinx.coroutines.flow.StateFlow

class DownloadRepository(
    private val downloadManager: DownloadManager,
    private val exportManager: ExportManager,
    private val fileManager: FileManager,
    private val metadataStore: MetadataStore,
) {
    val tasks: StateFlow<Map<String, com.copymanga.downloader.data.model.DownloadTaskState>> = downloadManager.tasks
    val progress: StateFlow<Map<String, ProgressData>> = downloadManager.progress
    val speed: StateFlow<String> = downloadManager.speed

    fun startDownload(comic: Comic, chapters: List<Pair<ChapterInfo, String>>) {
        downloadManager.startDownload(comic, chapters)
    }

    fun pause(chapterUuid: String) = downloadManager.pause(chapterUuid)
    fun resume(chapterUuid: String) = downloadManager.resume(chapterUuid)
    fun delete(chapterUuid: String) = downloadManager.delete(chapterUuid)

    suspend fun exportCbz(comic: Comic, chapters: List<ChapterInfo>) {
        exportManager.exportCbz(comic, chapters)
    }

    suspend fun exportPdf(comic: Comic, chapters: List<ChapterInfo>) {
        exportManager.exportPdf(comic, chapters)
    }

    fun scanDownloadedComics(): List<ComicDetail> {
        val downloadDir = fileManager.ensureDownloadDir()
        return fileManager.scanComicMetadataFiles(downloadDir)
            .mapNotNull { metadataStore.loadComicMetadata(it)?.comic }
    }
}
