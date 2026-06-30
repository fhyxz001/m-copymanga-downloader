package com.copymanga.downloader.domain.download

import com.copymanga.downloader.data.model.ChapterInfo
import com.copymanga.downloader.data.model.Comic
import com.copymanga.downloader.data.model.Config
import com.copymanga.downloader.data.model.ComicDetail
import com.copymanga.downloader.data.model.DownloadTaskState
import com.copymanga.downloader.data.remote.CopyClient
import com.copymanga.downloader.data.remote.RiskControlException
import com.copymanga.downloader.data.store.FileManager
import com.copymanga.downloader.data.store.MetadataStore
import com.copymanga.downloader.util.convertToFormat
import kotlinx.coroutines.CancellationException
import com.copymanga.downloader.domain.AccountPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.coroutineContext
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class DownloadTask(
    private val comic: Comic,
    private val chapterInfo: ChapterInfo,
    private val config: Config,
    private val comicDetail: ComicDetail,
    private val groupName: String,
    private val copyClientProvider: suspend () -> CopyClient,
    private val accountPool: AccountPool,
    private val fileManager: FileManager,
    private val metadataStore: MetadataStore,
    private val eventHub: EventHub,
    private val chapterSemaphore: Semaphore,
    private val imgSemaphore: Semaphore,
    private val bytesCounter: AtomicLong,
    private val parentScope: CoroutineScope,
) {
    private val _state = MutableStateFlow(DownloadTaskState.Pending)
    val state: StateFlow<DownloadTaskState> = _state

    private val _downloaded = AtomicInteger(0)
    private val _total = AtomicInteger(0)

    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        _state.value = DownloadTaskState.Pending
        job = parentScope.launch { process() }
    }

    fun pause() {
        if (_state.value == DownloadTaskState.Downloading || _state.value == DownloadTaskState.Pending) {
            _state.value = DownloadTaskState.Paused
            job?.cancel()
        }
    }

    fun resume() {
        if (_state.value == DownloadTaskState.Paused || _state.value == DownloadTaskState.Failed) {
            _state.value = DownloadTaskState.Pending
            job = parentScope.launch { process() }
        }
    }

    fun delete() {
        job?.cancel()
        job = null
        val chapterDir = fileManager.chapterDir(config, comicDetail, chapterInfo, groupName)
        fileManager.tempChapterDir(chapterDir).deleteRecursively()
        eventHub.emit(DownloadEvent.TaskDelete(chapterInfo.chapterUuid))
    }

    private suspend fun process() {
        chapterSemaphore.acquire()
        try {
            _downloaded.set(0)
            _total.set(0)
            emitCreate()
            _state.value = DownloadTaskState.Downloading
            val chapterDir = fileManager.chapterDir(config, comicDetail, chapterInfo, groupName)
            val tempDir = fileManager.tempChapterDir(chapterDir)

            try {
                metadataStore.saveComicMetadata(
                    fileManager.comicDir(config, comicDetail),
                    comic
                )

                val pairs = getUrlAndIndexPairs() ?: run {
                    fail()
                    return
                }
                _total.set(pairs.size)
                emitUpdate()

                tempDir.mkdirs()
                cleanTempDir(tempDir)

                coroutineScope {
                    pairs.map { (url, index) ->
                        async {
                            downloadImg(url, index, tempDir)
                        }
                    }.awaitAll()
                }

                if (!coroutineContext.isActive) return

                if (_downloaded.get() != _total.get()) {
                    fail()
                    return
                }

                if (!fileManager.atomicRename(tempDir, chapterDir)) {
                    fail()
                    return
                }

                val updatedChapter = chapterInfo.copy(
                    chapterDownloadDir = chapterDir.absolutePath,
                    isDownloaded = true,
                )
                metadataStore.saveChapterMetadata(chapterDir, updatedChapter)

                sleepBetweenChapter()
                _state.value = DownloadTaskState.Completed
                emitUpdate()
            } catch (_: CancellationException) {
                // 暂停或删除时取消协程，不清理临时目录以保留下载进度
            } catch (e: Exception) {
                fail(e.message ?: "未知错误")
            }
        } finally {
            chapterSemaphore.release()
        }
    }

    private suspend fun getUrlAndIndexPairs(): List<Pair<String, Long>>? {
        val chapterResp = try {
            getChapterWithRetry()
        } catch (e: Exception) {
            emitUpdate()
            return null
        }
        val urls = chapterResp.chapter.contents.map { it.url.replace(".c800x.", ".c1500x.") }
        val words = chapterResp.chapter.words
        return urls.mapIndexed { i, url ->
            val index = words.getOrElse(i) { i.toLong() }
            url to index
        }
    }

    private suspend fun getChapterWithRetry(): com.copymanga.downloader.data.remote.dto.GetChapterRespData {
        while (true) {
            try {
                return copyClientProvider().getChapter(
                    chapterInfo.comicPathWord,
                    chapterInfo.groupPathWord,
                    chapterInfo.chapterUuid,
                )
            } catch (e: RiskControlException) {
                handleRiskControl()
            }
        }
    }

    private suspend fun downloadImg(url: String, index: Long, tempDir: File) {
        if (!coroutineContext.isActive) return
        val ext = config.downloadFormat.extension
        val savePath = File(tempDir, String.format("%03d.%s", index + 1, ext))
        if (savePath.exists()) {
            _downloaded.incrementAndGet()
            emitUpdate()
            return
        }

        val acquired = withTimeoutOrNull(DownloadManager.IMG_PERMIT_TIMEOUT_MS) {
            imgSemaphore.acquire()
        } != null
        if (!acquired) return

        try {
            val maxRetries = 5
            var retries = 0
            while (coroutineContext.isActive) {
                try {
                    val (bytes, srcFormat) = copyClientProvider().getImgData(url)
                    val converted = convertToFormat(bytes, srcFormat, config.downloadFormat)
                    savePath.writeBytes(converted)
                    bytesCounter.addAndGet(bytes.size.toLong())
                    _downloaded.incrementAndGet()
                    emitUpdate()
                    break
                } catch (e: RiskControlException) {
                    handleRiskControl()
                } catch (e: Exception) {
                    retries++
                    if (retries >= maxRetries) return
                    delay(3_000)
                }
            }
            delay(config.imgDownloadIntervalSec * 1000)
        } finally {
            imgSemaphore.release()
        }
    }

    private suspend fun handleRiskControl() {
        accountPool.acquireAccount().let { account ->
            accountPool.markLimited(account)
        }
        val retryAfter = 60
        repeat(retryAfter) { i ->
            eventHub.emit(DownloadEvent.RiskControl(chapterInfo.chapterUuid, retryAfter - i))
            delay(1000)
        }
    }

    private fun cleanTempDir(tempDir: File) {
        val ext = config.downloadFormat.extension
        tempDir.listFiles()?.forEach { file ->
            if (file.extension != ext) file.delete()
        }
    }

    private suspend fun sleepBetweenChapter() {
        var remaining = config.chapterDownloadIntervalSec
        while (remaining > 0 && coroutineContext.isActive) {
            eventHub.emit(DownloadEvent.Sleeping(chapterInfo.chapterUuid, remaining))
            delay(1000)
            remaining--
        }
    }

    private fun fail(message: String = "下载失败") {
        _state.value = DownloadTaskState.Failed
        emitUpdate()
    }

    private fun emitCreate() {
        eventHub.emit(
            DownloadEvent.TaskCreate(
                state = _state.value,
                comic = comic,
                chapterInfo = chapterInfo,
                downloadedImgCount = _downloaded.get(),
                totalImgCount = _total.get(),
            )
        )
    }

    private fun emitUpdate() {
        eventHub.emit(
            DownloadEvent.TaskUpdate(
                chapterUuid = chapterInfo.chapterUuid,
                state = _state.value,
                downloadedImgCount = _downloaded.get(),
                totalImgCount = _total.get(),
            )
        )
    }
}
