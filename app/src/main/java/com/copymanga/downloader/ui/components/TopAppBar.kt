package com.copymanga.downloader.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.copymanga.downloader.R

sealed class TopBarMenuAction {
    data object Login : TopBarMenuAction()
    data object Settings : TopBarMenuAction()
    data object Logs : TopBarMenuAction()
    data object About : TopBarMenuAction()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyMangaTopAppBar(
    title: String,
    onMenuAction: (TopBarMenuAction) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(title) },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.menu))
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.login)) },
                    onClick = {
                        expanded = false
                        onMenuAction(TopBarMenuAction.Login)
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.settings)) },
                    onClick = {
                        expanded = false
                        onMenuAction(TopBarMenuAction.Settings)
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.logs)) },
                    onClick = {
                        expanded = false
                        onMenuAction(TopBarMenuAction.Logs)
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.about)) },
                    onClick = {
                        expanded = false
                        onMenuAction(TopBarMenuAction.About)
                    },
                )
            }
        },
    )
}
