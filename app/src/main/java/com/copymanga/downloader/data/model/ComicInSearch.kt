package com.copymanga.downloader.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ComicInSearch(
    val name: String = "",
    val alias: String? = null,
    @SerialName("pathWord") val pathWord: String = "",
    val cover: String = "",
    val ban: Long = 0,
    val author: List<Author> = emptyList(),
    val popular: Long = 0,
    @SerialName("isDownloaded") val isDownloaded: Boolean = false,
    @SerialName("comicDownloadDir") val comicDownloadDir: String = "",
)
