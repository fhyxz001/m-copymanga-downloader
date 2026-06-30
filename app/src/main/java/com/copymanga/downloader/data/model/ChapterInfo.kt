package com.copymanga.downloader.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors Rust `ChapterInfo`. `isDownloaded` and `chapterDownloadDir` are skipped
 * from JSON when null (handled by Json `explicitNulls = false`), matching the
 * `skip_serializing_if = Option::is_none` behavior used in metadata files.
 */
@Serializable
data class ChapterInfo(
    @SerialName("chapterUuid") val chapterUuid: String = "",
    @SerialName("chapterTitle") val chapterTitle: String = "",
    @SerialName("chapterSize") val chapterSize: Long = 0,
    @SerialName("comicUuid") val comicUuid: String = "",
    @SerialName("comicTitle") val comicTitle: String = "",
    @SerialName("comicPathWord") val comicPathWord: String = "",
    @SerialName("groupPathWord") val groupPathWord: String = "",
    @SerialName("groupName") val groupName: String = "",
    @SerialName("groupSize") val groupSize: Long = 0,
    @SerialName("order") val order: Double = 0.0,
    @SerialName("comicStatus") val comicStatus: ComicStatus = ComicStatus.Ongoing,
    @SerialName("isPdfExported") val isPdfExported: Boolean = false,
    @SerialName("isCbzExported") val isCbzExported: Boolean = false,
    @SerialName("isDownloaded") val isDownloaded: Boolean? = null,
    @SerialName("chapterDownloadDir") val chapterDownloadDir: String? = null,
)
