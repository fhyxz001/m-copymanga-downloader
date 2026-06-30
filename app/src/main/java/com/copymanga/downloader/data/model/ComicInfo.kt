package com.copymanga.downloader.data.model

/**
 * CBZ `ComicInfo.xml` (Kavita schema). Mirrors Rust `ComicInfo`.
 * Serialized to XML by the CBZ exporter.
 */
data class ComicInfo(
    val manga: String = "Yes",
    val series: String,
    val publisher: String = "拷贝漫画",
    val writer: String,
    val genre: String,
    val summary: String,
    val title: String,
    val number: String? = null,
    val volume: String? = null,
    val format: String? = null,
    val pageCount: Long,
    val count: Long,
) {
    companion object {
        fun from(comic: Comic, chapter: ChapterInfo): ComicInfo {
            val orderStr = chapter.order.toString()
            val (number, volume, format) = when (chapter.groupPathWord) {
                "default" -> Triple(orderStr, null, null)
                "tankobon" -> Triple(null, orderStr, null)
                else -> Triple(orderStr, null, "Special")
            }
            val count = when (chapter.comicStatus) {
                ComicStatus.Ongoing -> 0L
                ComicStatus.Completed -> chapter.groupSize
            }
            return ComicInfo(
                series = chapter.comicTitle,
                writer = comic.comic.author.joinToString(", ") { it.name },
                genre = comic.comic.theme.joinToString(", ") { it.name },
                summary = comic.comic.brief,
                title = chapter.chapterTitle,
                number = number,
                volume = volume,
                format = format,
                pageCount = chapter.chapterSize,
                count = count,
            )
        }
    }
}
