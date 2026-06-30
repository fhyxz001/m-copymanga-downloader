package com.copymanga.downloader.domain.export

import com.copymanga.downloader.data.model.ChapterInfo
import com.copymanga.downloader.data.model.Comic
import com.copymanga.downloader.data.model.Config
import com.copymanga.downloader.data.model.DownloadFormat
import com.copymanga.downloader.data.model.ExportSkipMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.ConcurrentHashMap

class ExportManager(
    private val config: Config,
    private val pathFormatter: PathFormatter,
    private val cbzExporter: CbzExporter,
    private val pdfExporter: PdfExporter,
) {
    private val locks = ConcurrentHashMap<String, Mutex>()

    suspend fun exportCbz(comic: Comic, chapters: List<ChapterInfo>) {
        withLock(comic.comic.pathWord) {
            chapters.forEach { chapter ->
                val target = pathFormatter.exportTargetPath(config, comic, chapter, DownloadFormat.Webp)
                cbzExporter.export(comic, chapter, target, config.exportSkipMode)
            }
        }
    }

    suspend fun exportPdf(comic: Comic, chapters: List<ChapterInfo>) {
        withLock(comic.comic.pathWord) {
            coroutineScope {
                val deferred = chapters.map { chapter ->
                    async(Dispatchers.Default) {
                        val target = pathFormatter.exportTargetPath(config, comic, chapter, DownloadFormat.Jpeg)
                        pdfExporter.export(comic, chapter, target, config.exportSkipMode)
                    }
                }
                deferred.awaitAll()
            }

            if (config.enableMergePdf && config.exportSkipMode != ExportSkipMode.SkipExported) {
                val grouped = chapters.groupBy { it.groupPathWord }
                grouped.forEach { (groupPathWord, groupChapters) ->
                    mergeGroupPdfs(comic, groupPathWord, groupChapters)
                }
            }
        }
    }

    private fun mergeGroupPdfs(comic: Comic, groupPathWord: String, chapters: List<ChapterInfo>) {
        val sorted = chapters.sortedBy { it.order }
        val pdfs = sorted.mapNotNull { chapter ->
            val target = pathFormatter.exportTargetPath(config, comic, chapter, DownloadFormat.Jpeg)
            target.takeIf { it.exists() }
        }
        if (pdfs.size < 2) return
        val groupName = sorted.firstOrNull()?.groupName ?: groupPathWord
        val mergePath = pathFormatter.mergePdfPath(config, comic, groupName)
        mergePath.parentFile?.mkdirs()
        val bookmarks = sorted.map { it.chapterTitle }
        pdfExporter.mergePdfs(pdfs, mergePath, bookmarks)
    }

    private suspend fun <T> withLock(pathWord: String, block: suspend () -> T): T {
        val mutex = locks.getOrPut(pathWord) { Mutex() }
        mutex.lock()
        try {
            return block()
        } finally {
            mutex.unlock()
        }
    }
}
