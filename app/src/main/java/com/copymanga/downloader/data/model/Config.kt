package com.copymanga.downloader.data.model

import kotlinx.serialization.Serializable

const val DEFAULT_API_DOMAIN = "api.2025copy.com"

@Serializable
data class Config(
    val token: String = "",
    val downloadDir: String = "",
    val exportDir: String = "",
    val apiDomainMode: ApiDomainMode = ApiDomainMode.Default,
    val customApiDomain: String = DEFAULT_API_DOMAIN,
    val downloadFormat: DownloadFormat = DownloadFormat.Webp,
    val enableFileLogger: Boolean = true,
    val chapterConcurrency: Int = 3,
    val chapterDownloadIntervalSec: Long = 0,
    val imgConcurrency: Int = 30,
    val imgDownloadIntervalSec: Long = 0,
    val updateDownloadedComicsIntervalSec: Long = 0,
    val comicDirFmt: String = "{comic_title}",
    val chapterDirFmt: String = "{group_title}/{order} {chapter_title}",
    val exportDirFmt: String = "{comic_title}/{export_format}/{group_title}/{order} {chapter_title}",
    val mergePdfFmt: String = "{comic_title}/pdf/{group_title}",
    val createPdfConcurrency: Int = Runtime.getRuntime().availableProcessors(),
    val enableMergePdf: Boolean = true,
    val exportSkipMode: ExportSkipMode = ExportSkipMode.None,
) {
    val authorization: String
        get() = "Token $token"

    fun apiDomain(): String = when (apiDomainMode) {
        ApiDomainMode.Custom -> customApiDomain
        ApiDomainMode.Default -> DEFAULT_API_DOMAIN
    }
}
