package com.copymanga.downloader.domain.export

import com.copymanga.downloader.data.model.ChapterInfo
import com.copymanga.downloader.data.model.Comic
import com.copymanga.downloader.data.model.Config
import com.copymanga.downloader.data.model.DownloadFormat
import com.copymanga.downloader.util.formatTemplate
import java.io.File

class PathFormatter {

    fun exportTargetPath(
        config: Config,
        comic: Comic,
        chapter: ChapterInfo,
        format: DownloadFormat,
    ): File {
        val author = comic.comic.author.joinToString(", ") { it.name }.ifEmpty { "未知作者" }
        val relative = formatTemplate(
            config.exportDirFmt,
            comicUuid = comic.comic.uuid,
            comicPathWord = comic.comic.pathWord,
            comicTitle = comic.comic.name,
            author = author,
            groupPathWord = chapter.groupPathWord,
            groupTitle = chapter.groupName,
            chapterUuid = chapter.chapterUuid,
            chapterTitle = chapter.chapterTitle,
            order = chapter.order,
            exportFormat = format.extension,
        )
        val dirParts = relative.split("/")
        if (dirParts.size < 2) {
            throw IllegalArgumentException("导出目录格式至少需要 2 个层级")
        }
        val filename = dirParts.last()
        val parent = dirParts.dropLast(1).joinToString("/")
        return File(File(config.exportDir, parent), "$filename.${format.extension}")
    }

    fun mergePdfPath(config: Config, comic: Comic, groupName: String): File {
        val author = comic.comic.author.joinToString(", ") { it.name }.ifEmpty { "未知作者" }
        val relative = formatTemplate(
            config.mergePdfFmt,
            comicUuid = comic.comic.uuid,
            comicPathWord = comic.comic.pathWord,
            comicTitle = comic.comic.name,
            author = author,
            groupPathWord = "",
            groupTitle = groupName,
        )
        return File(config.exportDir, "$relative.pdf")
    }
}
