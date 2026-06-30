package com.copymanga.downloader.ui.nav

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.ui.graphics.vector.ImageVector
import com.copymanga.downloader.R

enum class Destination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Search("search", R.string.tab_search, Icons.Outlined.Search),
    Favorite("favorite", R.string.tab_favorite, Icons.Outlined.CollectionsBookmark),
    Downloaded("downloaded", R.string.tab_downloaded, Icons.Outlined.Download),
    Chapter("chapter/{pathWord}", R.string.tab_chapter, Icons.Outlined.CloudDownload),
    Progress("progress", R.string.tab_progress, Icons.Outlined.Speed);

    companion object {
        val startRoute: String = Search.route
    }
}
