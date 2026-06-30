package com.copymanga.downloader.di

import android.content.Context
import com.copymanga.downloader.data.remote.CopyClient
import com.copymanga.downloader.data.store.AccountStore
import com.copymanga.downloader.data.store.ConfigStore
import com.copymanga.downloader.data.store.FileManager
import com.copymanga.downloader.data.store.LogManager
import com.copymanga.downloader.data.store.MetadataStore
import com.copymanga.downloader.domain.AccountPool
import com.copymanga.downloader.domain.download.DownloadManager
import com.copymanga.downloader.domain.download.EventHub
import com.copymanga.downloader.domain.export.CbzExporter
import com.copymanga.downloader.domain.export.ExportManager
import com.copymanga.downloader.domain.export.PathFormatter
import com.copymanga.downloader.domain.export.PdfExporter
import com.copymanga.downloader.domain.repository.CopyRepository
import com.copymanga.downloader.domain.repository.DownloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Manual service-locator container. Grows as layers are added in later phases.
 * Owned by [com.copymanga.downloader.CopyMangaApp]; ViewModels obtain it via AndroidViewModel.
 */
class AppContainer(val appContext: Context) {

    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val logManager: LogManager by lazy { LogManager(appContext) }
    val configStore: ConfigStore by lazy { ConfigStore(appContext) }
    val accountStore: AccountStore by lazy { AccountStore(appContext) }
    val metadataStore: MetadataStore by lazy { MetadataStore() }
    val fileManager: FileManager by lazy { FileManager(appContext, configStore) }

    val eventHub: EventHub by lazy { EventHub() }

    private val config by lazy {
        val loaded = configStore.load()
        fileManager.ensureConfigDefaults(loaded).also { configStore.save(it) }
    }

    private val accountPoolInternal: AccountPool by lazy {
        AccountPool(copyClientProviderInternal(), accountStore)
    }

    private fun copyClientProviderInternal(): suspend () -> CopyClient = {
        val account = accountPoolInternal.acquireAccount()
        val prepared = accountPoolInternal.prepare(account) ?: account
        CopyClient(config.apiDomain()) { prepared.token }
    }

    val copyClientProvider: suspend () -> CopyClient
        get() = copyClientProviderInternal()

    val downloadManager: DownloadManager by lazy {
        DownloadManager(
            config = config,
            accountPool = accountPoolInternal,
            copyClientProvider = copyClientProviderInternal(),
            fileManager = fileManager,
            metadataStore = metadataStore,
            eventHub = eventHub,
            parentScope = applicationScope,
        )
    }

    private val pathFormatter: PathFormatter by lazy { PathFormatter() }
    private val cbzExporter: CbzExporter by lazy { CbzExporter(metadataStore) }
    private val pdfExporter: PdfExporter by lazy { PdfExporter(metadataStore) }
    val exportManager: ExportManager by lazy {
        ExportManager(config, pathFormatter, cbzExporter, pdfExporter)
    }

    val copyRepository: CopyRepository by lazy {
        CopyRepository(
            copyClientProvider = copyClientProviderInternal(),
            accountPool = accountPoolInternal,
            configStore = configStore,
        )
    }

    val downloadRepository: DownloadRepository by lazy {
        DownloadRepository(
            downloadManager = downloadManager,
            exportManager = exportManager,
            fileManager = fileManager,
            metadataStore = metadataStore,
        )
    }

    fun refreshConfig() = config
}
