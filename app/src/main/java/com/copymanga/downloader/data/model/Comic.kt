package com.copymanga.downloader.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Comic(
    @SerialName("is_banned") val isBanned: Boolean = false,
    @SerialName("is_lock") val isLock: Boolean = false,
    @SerialName("is_login") val isLogin: Boolean = false,
    @SerialName("is_mobile_bind") val isMobileBind: Boolean = false,
    @SerialName("is_vip") val isVip: Boolean = false,
    val comic: ComicDetail = ComicDetail(),
    val popular: Long = 0,
    val groups: Map<String, Group> = emptyMap(),
    @SerialName("isDownloaded") val isDownloaded: Boolean? = null,
    @SerialName("comicDownloadDir") val comicDownloadDir: String? = null,
)

@Serializable
data class ComicDetail(
    val uuid: String = "",
    @SerialName("b_404") val b404: Boolean = false,
    @SerialName("b_hidden") val bHidden: Boolean = false,
    val ban: Long = 0,
    @SerialName("ban_ip") val banIp: Boolean? = null,
    val name: String = "",
    val alias: String? = null,
    @SerialName("path_word") val pathWord: String = "",
    @SerialName("close_comment") val closeComment: Boolean = false,
    @SerialName("close_roast") val closeRoast: Boolean = false,
    @SerialName("free_type") val freeType: LabeledValue = LabeledValue(),
    val restrict: LabeledValue = LabeledValue(),
    val reclass: LabeledValue = LabeledValue(),
    @SerialName("seo_baidu") val seoBaidu: String? = null,
    val region: LabeledValue = LabeledValue(),
    val status: LabeledValue = LabeledValue(),
    val author: List<Author> = emptyList(),
    val theme: List<Theme> = emptyList(),
    val brief: String = "",
    @SerialName("datetime_updated") val datetimeUpdated: String = "",
    val cover: String = "",
    @SerialName("last_chapter") val lastChapter: LastChapter = LastChapter(),
    val popular: Long = 0,
    /** groupPathWord -> chapter infos */
    val groups: Map<String, List<ChapterInfo>> = emptyMap(),
)
