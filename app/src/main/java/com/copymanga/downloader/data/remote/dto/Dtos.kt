package com.copymanga.downloader.data.remote.dto

import com.copymanga.downloader.data.model.Author
import com.copymanga.downloader.data.model.ChapterInfo
import com.copymanga.downloader.data.model.ComicDetail
import com.copymanga.downloader.data.model.ComicInFavorite
import com.copymanga.downloader.data.model.ComicInSearch
import com.copymanga.downloader.data.model.ComicStatus
import com.copymanga.downloader.data.model.FavoriteItem
import com.copymanga.downloader.data.model.Group
import com.copymanga.downloader.data.model.LastChapter
import com.copymanga.downloader.data.model.LabeledValue
import com.copymanga.downloader.data.model.Theme
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Pagination<T>(
    val list: List<T> = emptyList(),
    val total: Long = 0,
    val limit: Long = 0,
    val offset: Long = 0,
)

// Rust 中是 newtype wrapper: SearchRespData(pub Pagination<ComicInSearchRespData>)
// JSON 实际形状与 Pagination 一致，因此这里直接用 typealias 保持结构一致。
typealias SearchRespData = Pagination<ComicInSearchRespData>
typealias GetChaptersRespData = Pagination<ChapterInGetChaptersRespData>
typealias GetFavoriteRespData = Pagination<FavoriteItemRespData>

@Serializable
data class ComicInSearchRespData(
    val name: String = "",
    val alias: String? = null,
    @SerialName("path_word") val pathWord: String = "",
    val cover: String = "",
    val ban: Long = 0,
    val author: List<AuthorRespData> = emptyList(),
    val popular: Long = 0,
) {
    fun toModel(): ComicInSearch = ComicInSearch(
        name = name,
        alias = alias,
        pathWord = pathWord,
        cover = cover,
        ban = ban,
        author = author.map { it.toModel() },
        popular = popular,
    )
}

@Serializable
data class AuthorRespData(
    val name: String = "",
    val alias: String? = null,
    @SerialName("path_word") val pathWord: String = "",
) {
    fun toModel(): Author = Author(
        name = name,
        alias = alias,
        pathWord = pathWord,
    )
}

@Serializable
data class LoginRespData(
    val token: String = "",
    @SerialName("user_id") val userId: String = "",
    val username: String = "",
    val nickname: String = "",
    val avatar: String = "",
    @SerialName("datetime_created") val datetimeCreated: String = "",
    val ticket: Double = 0.0,
    @SerialName("reward_ticket") val rewardTicket: Double = 0.0,
    val downloads: Long = 0,
    @SerialName("vip_downloads") val vipDownloads: Long = 0,
    @SerialName("reward_downloads") val rewardDownloads: Long = 0,
    @SerialName("scy_answer") val scyAnswer: Boolean = false,
)

@Serializable
data class UserProfileRespData(
    @SerialName("user_id") val userId: String = "",
    val username: String = "",
    val nickname: String = "",
    val avatar: String = "",
    @SerialName("datetime_created") val datetimeCreated: String = "",
    val ticket: Double = 0.0,
    @SerialName("reward_ticket") val rewardTicket: Double = 0.0,
    val downloads: Long = 0,
    @SerialName("vip_downloads") val vipDownloads: Long = 0,
    @SerialName("reward_downloads") val rewardDownloads: Long = 0,
    @SerialName("scy_answer") val scyAnswer: Boolean = false,
    @SerialName("day_downloads_refresh") val dayDownloadsRefresh: String = "",
    @SerialName("day_downloads") val dayDownloads: Long = 0,
)

@Serializable
data class GetComicRespData(
    @SerialName("is_banned") val isBanned: Boolean = false,
    @SerialName("is_lock") val isLock: Boolean = false,
    @SerialName("is_login") val isLogin: Boolean = false,
    @SerialName("is_mobile_bind") val isMobileBind: Boolean = false,
    @SerialName("is_vip") val isVip: Boolean = false,
    val comic: ComicInGetComicRespData = ComicInGetComicRespData(),
    val popular: Long = 0,
    val groups: Map<String, GroupRespData> = emptyMap(),
)

@Serializable
data class ComicInGetComicRespData(
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
    @SerialName("free_type") val freeType: LabeledValueRespData = LabeledValueRespData(),
    val restrict: LabeledValueRespData = LabeledValueRespData(),
    val reclass: LabeledValueRespData = LabeledValueRespData(),
    @SerialName("seo_baidu") val seoBaidu: String? = null,
    val region: LabeledValueRespData = LabeledValueRespData(),
    val status: LabeledValueRespData = LabeledValueRespData(),
    val author: List<AuthorRespData> = emptyList(),
    val theme: List<ThemeRespData> = emptyList(),
    val brief: String = "",
    @SerialName("datetime_updated") val datetimeUpdated: String = "",
    val cover: String = "",
    @SerialName("last_chapter") val lastChapter: LastChapterRespData = LastChapterRespData(),
    val popular: Long = 0,
) {
    fun toModel(
        groupsChapters: Map<String, List<ChapterInGetChaptersRespData>> = emptyMap(),
        groups: Map<String, GroupRespData> = emptyMap(),
    ): ComicDetail {
        val comicStatus = if (status.value == 0L) ComicStatus.Ongoing else ComicStatus.Completed
        return ComicDetail(
            uuid = uuid,
            b404 = b404,
            bHidden = bHidden,
            ban = ban,
            banIp = banIp,
            name = name,
            alias = alias,
            pathWord = pathWord,
            closeComment = closeComment,
            closeRoast = closeRoast,
            freeType = freeType.toModel(),
            restrict = restrict.toModel(),
            reclass = reclass.toModel(),
            seoBaidu = seoBaidu,
            region = region.toModel(),
            status = status.toModel(),
            author = author.map { it.toModel() },
            theme = theme.map { it.toModel() },
            brief = brief,
            datetimeUpdated = datetimeUpdated,
            cover = cover,
            lastChapter = lastChapter.toModel(),
            popular = popular,
            groups = buildGroupsMap(groupsChapters, groups, comicStatus),
        )
    }

    private fun buildGroupsMap(
        groupsChapters: Map<String, List<ChapterInGetChaptersRespData>>,
        groups: Map<String, GroupRespData>,
        comicStatus: ComicStatus,
    ): Map<String, List<ChapterInfo>> {
        return groups.map { (groupPathWord, groupResp) ->
            val chapters = groupsChapters[groupPathWord].orEmpty()
            val chapterInfos = chapters.map { chapter ->
                chapter.toModel(
                    comicUuid = uuid,
                    comicTitle = name,
                    comicPathWord = pathWord,
                    groupPathWord = groupPathWord,
                    groupName = groupResp.name,
                    groupSize = chapter.count,
                    comicStatus = comicStatus,
                )
            }
            groupPathWord to chapterInfos
        }.toMap()
    }
}

@Serializable
data class LabeledValueRespData(
    val value: Long = 0,
    val display: String = "",
) {
    fun toModel(): LabeledValue = LabeledValue(value = value, display = display)
}

@Serializable
data class ThemeRespData(
    val name: String = "",
    @SerialName("path_word") val pathWord: String = "",
) {
    fun toModel(): Theme = Theme(name = name, pathWord = pathWord)
}

@Serializable
data class LastChapterRespData(
    val uuid: String = "",
    val name: String = "",
) {
    fun toModel(): LastChapter = LastChapter(uuid = uuid, name = name)
}

@Serializable
data class GroupRespData(
    @SerialName("path_word") val pathWord: String = "",
    val count: Int = 0,
    val name: String = "",
) {
    fun toModel(): Group = Group(pathWord = pathWord, count = count, name = name)
}

@Serializable
data class ChapterInGetChaptersRespData(
    val index: Long = 0,
    val uuid: String = "",
    val count: Long = 0,
    val ordered: Long = 0,
    val size: Long = 0,
    val name: String = "",
    @SerialName("comic_id") val comicId: String = "",
    @SerialName("comic_path_word") val comicPathWord: String = "",
    @SerialName("group_id") val groupId: String? = null,
    @SerialName("group_path_word") val groupPathWord: String = "",
    @SerialName("type") val typeField: Long = 0,
    val news: String = "",
    @SerialName("datetime_created") val datetimeCreated: String = "",
    val prev: String? = null,
    val next: String? = null,
) {
    fun toModel(
        comicUuid: String = "",
        comicTitle: String = "",
        comicPathWord: String = "",
        groupPathWord: String = "",
        groupName: String = "",
        groupSize: Long = 0,
        comicStatus: ComicStatus = ComicStatus.Ongoing,
    ): ChapterInfo = ChapterInfo(
        chapterUuid = uuid,
        chapterTitle = name,
        chapterSize = size,
        comicUuid = comicUuid,
        comicTitle = comicTitle,
        comicPathWord = comicPathWord,
        groupPathWord = groupPathWord,
        groupName = groupName,
        groupSize = groupSize,
        order = ordered / 10.0,
        comicStatus = comicStatus,
    )
}

@Serializable
data class GetChapterRespData(
    @SerialName("is_banned") val isBanned: Boolean = false,
    @SerialName("show_app") val showApp: Boolean = false,
    @SerialName("is_lock") val isLock: Boolean = false,
    @SerialName("is_login") val isLogin: Boolean = false,
    @SerialName("is_mobile_bind") val isMobileBind: Boolean = false,
    @SerialName("is_vip") val isVip: Boolean = false,
    val comic: ComicInGetChapterRespData = ComicInGetChapterRespData(),
    val chapter: ChapterInGetChapterRespData = ChapterInGetChapterRespData(),
)

@Serializable
data class ComicInGetChapterRespData(
    val name: String = "",
    val uuid: String = "",
    @SerialName("path_word") val pathWord: String = "",
    val restrict: RestrictRespData = RestrictRespData(),
)

@Serializable
data class RestrictRespData(
    val value: Long = 0,
    val display: String = "",
)

@Serializable
data class ChapterInGetChapterRespData(
    val index: Long = 0,
    val uuid: String = "",
    val count: Long = 0,
    val ordered: Long = 0,
    val size: Long = 0,
    val name: String = "",
    @SerialName("comic_id") val comicId: String = "",
    @SerialName("comic_path_word") val comicPathWord: String = "",
    @SerialName("group_id") val groupId: String? = null,
    @SerialName("group_path_word") val groupPathWord: String = "",
    @SerialName("type") val typeField: Long = 0,
    val news: String = "",
    @SerialName("datetime_created") val datetimeCreated: String = "",
    val prev: String? = null,
    val next: String? = null,
    val contents: List<ContentRespData> = emptyList(),
    val words: List<Long> = emptyList(),
    @SerialName("is_long") val isLong: Boolean = false,
)

@Serializable
data class ContentRespData(
    val url: String = "",
)

@Serializable
data class FavoriteItemRespData(
    val uuid: Long = 0,
    @SerialName("b_folder") val bFolder: Boolean = false,
    val comic: ComicInGetFavoriteRespData = ComicInGetFavoriteRespData(),
) {
    fun toModel(): FavoriteItem = FavoriteItem(
        uuid = uuid,
        bFolder = bFolder,
        comic = comic.toModel(),
    )
}

@Serializable
data class ComicInGetFavoriteRespData(
    val uuid: String = "",
    @SerialName("b_display") val bDisplay: Boolean = false,
    val name: String = "",
    @SerialName("path_word") val pathWord: String = "",
    val author: List<AuthorRespData> = emptyList(),
    val cover: String = "",
    val status: Long = 0,
    val popular: Long = 0,
    @SerialName("datetime_updated") val datetimeUpdated: String = "",
    @SerialName("last_chapter_id") val lastChapterId: String = "",
    @SerialName("last_chapter_name") val lastChapterName: String = "",
) {
    fun toModel(): ComicInFavorite = ComicInFavorite(
        uuid = uuid,
        bDisplay = bDisplay,
        name = name,
        pathWord = pathWord,
        author = author.map { it.toModel() },
        cover = cover,
        status = status,
        popular = popular,
        datetimeUpdated = datetimeUpdated,
        lastChapterId = lastChapterId,
        lastChapterName = lastChapterName,
    )
}
