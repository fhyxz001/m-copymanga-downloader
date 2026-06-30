package com.copymanga.downloader.domain.download

import com.copymanga.downloader.data.model.ChapterInfo
import com.copymanga.downloader.data.model.Comic
import com.copymanga.downloader.data.model.Config
import com.copymanga.downloader.data.model.DownloadTaskState
import com.copymanga.downloader.data.remote.CopyClient
import com.copymanga.downloader.data.store.FileManager
import com.copymanga.downloader.data.store.MetadataStore
import com.copymanga.downloader.domain.AccountPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import java.util.concurrent.atomic.AtomicLong

class DownloadManager(
    private val config: Config,
    private val accountPool: AccountPool,
    private val copyClientProvider: suspend () -> CopyClient,
    private val fileManager: FileManager,
    private val metadataStore: MetadataStore,
    private val eventHub: EventHub,
    private val parentScope: CoroutineScope,
) {
    private val chapterSemaphore = Semaphore(config.chapterConcurrency)
    private val imgSemaphore = Semaphore(config.imgConcurrency)

    private val _tasks = MutableStateFlow<Map<String, DownloadTaskState>>(emptyMap())
    val tasks: StateFlow<Map<String, DownloadTaskState>> = _tasks

    private val _progress = MutableStateFlow<Map<String, ProgressData>>(emptyMap())
    val progress: StateFlow<Map<String, ProgressData>> = _progress

    private val _speed = MutableStateFlow("0.00MB/s")
    val speed: StateFlow<String> = _speed

    private val bytePerSec = AtomicLong(0)
    private val runningTasks = mutableMapOf<String, DownloadTask>()

    init {
        startSpeedLoop()
        startEventCollector()
    }

    fun startDownload(comic: Comic, chapters: List<Pair<ChapterInfo, String>>) {
        chapters.forEach { (chapterInfo, groupName) ->
            val existing = runningTasks[chapterInfo.chapterUuid]
            if (existing != null && existing.state.value in setOf(
                    DownloadTaskState.Pending,
                    DownloadTaskState.Downloading,
                    DownloadTaskState.Paused,
                )
            ) {
                return@forEach
            }
            val task = DownloadTask(
                comic = comic,
                chapterInfo = chapterInfo,
                config = config,
                comicDetail = comic.comic,
                groupName = groupName,
                copyClientProvider = copyClientProvider,
                accountPool = accountPool,
                fileManager = fileManager,
                metadataStore = metadataStore,
                eventHub = eventHub,
                chapterSemaphore = chapterSemaphore,
                imgSemaphore = imgSemaphore,
                bytesCounter = bytePerSec,
                parentScope = parentScope,
            )
            runningTasks[chapterInfo.chapterUuid] = task
            _tasks.value += (chapterInfo.chapterUuid to DownloadTaskState.Pending)
            task.start()
        }
    }

    fun pause(chapterUuid: String) {
        runningTasks[chapterUuid]?.pause()
    }

    fun resume(chapterUuid: String) {
        runningTasks[chapterUuid]?.resume()
    }

    fun delete(chapterUuid: String) {
        runningTasks[chapterUuid]?.delete()
        runningTasks.remove(chapterUuid)
        _tasks.value -= chapterUuid
        _progress.value -= chapterUuid
    }

    private fun startSpeedLoop() {
        parentScope.launch {
            while (isActive) {
                delay(1000)
                val bytes = bytePerSec.getAndSet(0)
                val mbps = bytes / 1024.0 / 1024.0
                _speed.value = String.format("%.2fMB/s", mbps)
            }
        }
    }

    private fun startEventCollector() {
        parentScope.launch {
            eventHub.events.collect { event ->
                when (event) {
                    is DownloadEvent.TaskCreate -> {
                        _tasks.value += (event.chapterInfo.chapterUuid to event.state)
                        _progress.value += (event.chapterInfo.chapterUuid to ProgressData(
                            chapterUuid = event.chapterInfo.chapterUuid,
                            comicTitle = event.comic.comic.name,
                            chapterTitle = event.chapterInfo.chapterTitle,
                            state = event.state,
                            downloadedImgCount = event.downloadedImgCount,
                            totalImgCount = event.totalImgCount,
                            percentage = if (event.totalImgCount > 0) event.downloadedImgCount * 100 / event.totalImgCount else 0,
                            indicator = event.state.name,
                            retryAfter = 0,
                        ))
                    }
                    is DownloadEvent.TaskUpdate -> {
                        _tasks.value += (event.chapterUuid to event.state)
                        val current = _progress.value[event.chapterUuid]
                        _progress.value += (event.chapterUuid to (current?.copy(
                            state = event.state,
                            downloadedImgCount = event.downloadedImgCount,
                            totalImgCount = event.totalImgCount,
                            percentage = if (event.totalImgCount > 0) event.downloadedImgCount * 100 / event.totalImgCount else 0,
                        ) ?: ProgressData(
                            chapterUuid = event.chapterUuid,
                            comicTitle = "",
                            chapterTitle = "",
                            state = event.state,
                            downloadedImgCount = event.downloadedImgCount,
                            totalImgCount = event.totalImgCount,
                            percentage = if (event.totalImgCount > 0) event.downloadedImgCount * 100 / event.totalImgCount else 0,
                            indicator = event.state.name,
                            retryAfter = 0,
                        )))
                    }
                    is DownloadEvent.TaskDelete -> {
                        _tasks.value -= event.chapterUuid
                        _progress.value -= event.chapterUuid
                    }
                    is DownloadEvent.RiskControl -> {
                        val current = _progress.value[event.chapterUuid]
                        current?.let {
                            _progress.value += (event.chapterUuid to it.copy(
                                retryAfter = event.retryAfter,
                                indicator = "风控 ${event.retryAfter}s",
                            ))
                        }
                    }
                    is DownloadEvent.Sleeping -> {
                        val current = _progress.value[event.chapterUuid]
                        current?.let {
                            _progress.value += (event.chapterUuid to it.copy(
                                indicator = "冷却 ${event.remainingSec}s",
                            ))
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    companion object {
        const val IMG_PERMIT_TIMEOUT_MS: Long = 10_000
    }
}

data class ProgressData(
    val chapterUuid: String,
    val comicTitle: String,
    val chapterTitle: String,
    val state: DownloadTaskState,
    val downloadedImgCount: Int,
    val totalImgCount: Int,
    val percentage: Int,
    val indicator: String,
    val retryAfter: Int,
)
