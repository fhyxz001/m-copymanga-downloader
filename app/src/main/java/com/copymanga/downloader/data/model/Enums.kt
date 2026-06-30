package com.copymanga.downloader.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ComicStatus { Ongoing, Completed }

@Serializable
enum class DownloadFormat {
    Webp,
    Jpeg;

    val extension: String
        get() = when (this) {
            Webp -> "webp"
            Jpeg -> "jpg"
        }
}

@Serializable
enum class ApiDomainMode { Default, Custom }

@Serializable
enum class ExportSkipMode { None, SkipExisting, SkipExported }

@Serializable
enum class GetFavoriteOrdering {
    Added,
    Updated,
    Read;

    val param: String
        get() = when (this) {
            Added -> "-datetime_modifier"
            Updated -> "-datetime_updated"
            Read -> "-datetime_browse"
        }
}

@Serializable
enum class DownloadTaskState { Pending, Downloading, Paused, Completed, Failed }
