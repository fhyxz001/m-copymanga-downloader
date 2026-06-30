package com.copymanga.downloader.domain.export

import com.copymanga.downloader.data.model.ChapterInfo
import com.copymanga.downloader.data.model.Comic
import com.copymanga.downloader.data.model.ComicInfo
import com.copymanga.downloader.data.model.ExportSkipMode
import com.copymanga.downloader.data.store.MetadataStore
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CbzExporter(private val metadataStore: MetadataStore) {

    fun export(
        comic: Comic,
        chapter: ChapterInfo,
        targetCbz: File,
        skipMode: ExportSkipMode,
    ): Boolean {
        val chapterDir = chapter.chapterDownloadDir?.let { File(it) }
            ?: return false

        val shouldSkip = when (skipMode) {
            ExportSkipMode.SkipExported -> chapter.isCbzExported
            ExportSkipMode.SkipExisting -> targetCbz.exists()
            ExportSkipMode.None -> false
        }
        if (shouldSkip) return true

        targetCbz.parentFile?.mkdirs()
        val imageFiles = chapterDir.listFiles()
            ?.filter { it.isFile && (it.extension == "webp" || it.extension == "jpg") }
            ?.sortedBy { it.name }
            ?: emptyList()

        ZipOutputStream(targetCbz.outputStream()).use { zip ->
            val comicInfo = ComicInfo.from(comic, chapter)
            zip.putNextEntry(ZipEntry("ComicInfo.xml"))
            zip.write(comicInfo.toXml().toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            imageFiles.forEach { img ->
                zip.putNextEntry(ZipEntry(img.name))
                img.inputStream().use { input -> input.copyTo(zip) }
                zip.closeEntry()
            }
        }

        // 更新章节导出状态
        val updated = chapter.copy(isCbzExported = true)
        metadataStore.saveChapterMetadata(chapterDir, updated)
        return true
    }
}
