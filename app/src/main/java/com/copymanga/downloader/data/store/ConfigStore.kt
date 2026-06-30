package com.copymanga.downloader.data.store

import android.content.Context
import com.copymanga.downloader.data.model.Config
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private const val CONFIG_FILE = "config.json"

class ConfigStore(context: Context) {

    private val configFile = File(context.getExternalFilesDir(null), CONFIG_FILE)

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        prettyPrint = true
        encodeDefaults = true
    }

    fun load(): Config {
        if (!configFile.exists()) {
            save(Config())
            return Config()
        }
        val text = configFile.readText(Charsets.UTF_8)
        val loaded = try {
            json.decodeFromString(Config.serializer(), text)
        } catch (e: Exception) {
            Config()
        }
        // 合并默认值：用默认 Config 覆盖缺失字段
        val merged = Config(
            token = loaded.token.ifEmpty { Config().token },
            downloadDir = loaded.downloadDir.ifEmpty { defaultDownloadDir() },
            exportDir = loaded.exportDir.ifEmpty { defaultExportDir() },
            apiDomainMode = loaded.apiDomainMode,
            customApiDomain = loaded.customApiDomain.ifEmpty { Config().customApiDomain },
            downloadFormat = loaded.downloadFormat,
            enableFileLogger = loaded.enableFileLogger,
            chapterConcurrency = if (loaded.chapterConcurrency > 0) loaded.chapterConcurrency else Config().chapterConcurrency,
            chapterDownloadIntervalSec = loaded.chapterDownloadIntervalSec,
            imgConcurrency = if (loaded.imgConcurrency > 0) loaded.imgConcurrency else Config().imgConcurrency,
            imgDownloadIntervalSec = loaded.imgDownloadIntervalSec,
            updateDownloadedComicsIntervalSec = loaded.updateDownloadedComicsIntervalSec,
            comicDirFmt = loaded.comicDirFmt.ifEmpty { Config().comicDirFmt },
            chapterDirFmt = loaded.chapterDirFmt.ifEmpty { Config().chapterDirFmt },
            exportDirFmt = loaded.exportDirFmt.ifEmpty { Config().exportDirFmt },
            mergePdfFmt = loaded.mergePdfFmt.ifEmpty { Config().mergePdfFmt },
            createPdfConcurrency = if (loaded.createPdfConcurrency > 0) loaded.createPdfConcurrency else Config().createPdfConcurrency,
            enableMergePdf = loaded.enableMergePdf,
            exportSkipMode = loaded.exportSkipMode,
        )
        if (loaded != merged) save(merged)
        return merged
    }

    fun save(config: Config) {
        configFile.parentFile?.mkdirs()
        configFile.writeText(json.encodeToString(config), Charsets.UTF_8)
    }

    fun update(block: (Config) -> Config): Config {
        val updated = block(load())
        save(updated)
        return updated
    }

    private fun defaultDownloadDir(): String {
        return File(configFile.parentFile, "downloads").absolutePath
    }

    private fun defaultExportDir(): String {
        return File(configFile.parentFile, "exports").absolutePath
    }
}
