package com.copymanga.downloader.data.store

import com.copymanga.downloader.data.model.ChapterInfo
import com.copymanga.downloader.data.model.Comic
import com.copymanga.downloader.data.remote.CopyJson
import kotlinx.serialization.encodeToString
import java.io.File

class MetadataStore {

    private val json = CopyJson

    fun saveComicMetadata(dir: File, comic: Comic) {
        dir.mkdirs()
        val metadataFile = File(dir, "元数据.json")
        // 写入前清除下载标记字段，使其在序列化时被跳过
        val clean = comic.copy(
            isDownloaded = null,
            comicDownloadDir = null,
            comic = comic.comic.copy(
                groups = comic.comic.groups.mapValues { (_, chapters) ->
                    chapters.map { it.copy(isDownloaded = null, chapterDownloadDir = null) }
                }
            )
        )
        metadataFile.writeText(json.encodeToString(clean), Charsets.UTF_8)
    }

    fun saveChapterMetadata(dir: File, chapter: ChapterInfo) {
        dir.mkdirs()
        val metadataFile = File(dir, "章节元数据.json")
        val clean = chapter.copy(isDownloaded = null, chapterDownloadDir = null)
        metadataFile.writeText(json.encodeToString(clean), Charsets.UTF_8)
    }

    fun loadComicMetadata(file: File): Comic? {
        return try {
            json.decodeFromString(Comic.serializer(), file.readText(Charsets.UTF_8))
        } catch (e: Exception) {
            null
        }
    }

    fun loadChapterMetadata(file: File): ChapterInfo? {
        return try {
            json.decodeFromString(ChapterInfo.serializer(), file.readText(Charsets.UTF_8))
        } catch (e: Exception) {
            null
        }
    }
}
