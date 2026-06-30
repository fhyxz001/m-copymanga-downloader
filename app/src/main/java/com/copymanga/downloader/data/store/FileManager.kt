package com.copymanga.downloader.data.store

import android.content.Context
import com.copymanga.downloader.data.model.ChapterInfo
import com.copymanga.downloader.data.model.ComicDetail
import com.copymanga.downloader.data.model.Config
import com.copymanga.downloader.util.formatTemplate
import com.copymanga.downloader.util.sanitize
import java.io.File

class FileManager(
    context: Context,
    private val configStore: ConfigStore,
) {

    private val baseDir: File = context.getExternalFilesDir(null) ?: context.filesDir

    fun ensureDownloadDir(): File {
        val config = configStore.load()
        val dir = File(config.downloadDir)
        dir.mkdirs()
        return dir
    }

    fun ensureConfigDefaults(config: Config): Config {
        val downloadDir = config.downloadDir.ifEmpty { File(baseDir, "downloads").absolutePath }
        val exportDir = config.exportDir.ifEmpty { File(baseDir, "exports").absolutePath }
        return if (downloadDir != config.downloadDir || exportDir != config.exportDir) {
            config.copy(downloadDir = downloadDir, exportDir = exportDir)
        } else config
    }

    fun comicDir(config: Config, comic: ComicDetail): File {
        val author = comic.author.joinToString(", ") { it.name }.ifEmpty { "未知作者" }
        val relative = formatTemplate(
            config.comicDirFmt,
            comicUuid = comic.uuid,
            comicPathWord = comic.pathWord,
            comicTitle = comic.name,
            author = author,
        )
        return File(config.downloadDir, relative)
    }

    fun chapterDir(
        config: Config,
        comic: ComicDetail,
        chapter: ChapterInfo,
        groupName: String,
    ): File {
        val comicDir = comicDir(config, comic)
        val author = comic.author.joinToString(", ") { it.name }.ifEmpty { "未知作者" }
        val relative = formatTemplate(
            config.chapterDirFmt,
            comicUuid = comic.uuid,
            comicPathWord = comic.pathWord,
            comicTitle = comic.name,
            author = author,
            groupPathWord = chapter.groupPathWord,
            groupTitle = groupName,
            chapterUuid = chapter.chapterUuid,
            chapterTitle = chapter.chapterTitle,
            order = chapter.order,
        )
        return File(comicDir, relative)
    }

    fun exportDir(
        config: Config,
        comic: ComicDetail,
        chapter: ChapterInfo,
        groupName: String,
        exportFormat: String,
    ): File {
        val author = comic.author.joinToString(", ") { it.name }.ifEmpty { "未知作者" }
        val relative = formatTemplate(
            config.exportDirFmt,
            comicUuid = comic.uuid,
            comicPathWord = comic.pathWord,
            comicTitle = comic.name,
            author = author,
            groupPathWord = chapter.groupPathWord,
            groupTitle = groupName,
            chapterUuid = chapter.chapterUuid,
            chapterTitle = chapter.chapterTitle,
            order = chapter.order,
            exportFormat = exportFormat,
        )
        return File(config.exportDir, relative)
    }

    fun mergePdfDir(
        config: Config,
        comic: ComicDetail,
        groupName: String,
    ): File {
        val author = comic.author.joinToString(", ") { it.name }.ifEmpty { "未知作者" }
        val relative = formatTemplate(
            config.mergePdfFmt,
            comicUuid = comic.uuid,
            comicPathWord = comic.pathWord,
            comicTitle = comic.name,
            author = author,
            groupPathWord = "",
            groupTitle = groupName,
        )
        return File(config.exportDir, relative)
    }

    fun tempChapterDir(chapterDir: File): File {
        val parent = chapterDir.parentFile ?: chapterDir
        return File(parent, ".下载中-${chapterDir.name}")
    }

    fun atomicRename(temp: File, target: File): Boolean {
        if (!temp.exists()) return false
        if (target.exists()) target.deleteRecursively()
        return temp.renameTo(target)
    }

    fun scanComicMetadataFiles(downloadDir: File): List<File> {
        if (!downloadDir.exists()) return emptyList()
        return downloadDir.walkTopDown()
            .filter { it.isFile && it.name == "元数据.json" }
            .toList()
    }

    fun sanitizeFileName(name: String): String = sanitize(name)
}
