package com.copymanga.downloader.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteItem(
    val uuid: Long = 0,
    @SerialName("bFolder") val bFolder: Boolean = false,
    val comic: ComicInFavorite = ComicInFavorite(),
)

@Serializable
data class ComicInFavorite(
    val uuid: String = "",
    @SerialName("bDisplay") val bDisplay: Boolean = false,
    val name: String = "",
    @SerialName("pathWord") val pathWord: String = "",
    val author: List<Author> = emptyList(),
    val cover: String = "",
    val status: Long = 0,
    val popular: Long = 0,
    @SerialName("datetimeUpdated") val datetimeUpdated: String = "",
    @SerialName("lastChapterId") val lastChapterId: String = "",
    @SerialName("lastChapterName") val lastChapterName: String = "",
    @SerialName("isDownloaded") val isDownloaded: Boolean = false,
    @SerialName("comicDownloadDir") val comicDownloadDir: String = "",
)
