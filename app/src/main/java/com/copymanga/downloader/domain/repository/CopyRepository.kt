package com.copymanga.downloader.domain.repository

import com.copymanga.downloader.data.model.Comic
import com.copymanga.downloader.data.model.ComicInSearch
import com.copymanga.downloader.data.model.Config
import com.copymanga.downloader.data.model.FavoriteItem
import com.copymanga.downloader.data.model.GetFavoriteOrdering
import com.copymanga.downloader.data.remote.CopyClient
import com.copymanga.downloader.data.store.ConfigStore
import com.copymanga.downloader.domain.AccountPool
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class CopyRepository(
    private val copyClientProvider: suspend () -> CopyClient,
    private val accountPool: AccountPool,
    private val configStore: ConfigStore,
) {
    private val config: Config get() = configStore.load()

    suspend fun login(username: String, password: String): Result<String> = runCatching {
        val client = CopyClient(config.apiDomain())
        val resp = client.login(username, password)
        configStore.update { it.copy(token = resp.token) }
        resp.token
    }

    suspend fun getUserProfile(): Result<com.copymanga.downloader.data.remote.dto.UserProfileRespData> = runCatching {
        val client = CopyClient(config.apiDomain()) { config.token }
        client.getUserProfile(config.token)
    }

    suspend fun search(keyword: String, pageNum: Int): Result<List<ComicInSearch>> = runCatching {
        val client = copyClientProvider()
        val resp = client.search(keyword, pageNum)
        resp.list.map { it.toModel() }
    }

    suspend fun getComic(pathWord: String): Result<Comic> = runCatching {
        val client = copyClientProvider()
        val comicResp = client.getComic(pathWord)
        val groupsChapters = coroutineScope {
            comicResp.groups.map { (groupPathWord, _) ->
                async {
                    groupPathWord to client.getGroupChapters(pathWord, groupPathWord)
                }
            }.awaitAll().toMap()
        }
        Comic(
            isBanned = comicResp.isBanned,
            isLock = comicResp.isLock,
            isLogin = comicResp.isLogin,
            isMobileBind = comicResp.isMobileBind,
            isVip = comicResp.isVip,
            comic = comicResp.comic.toModel(groupsChapters, comicResp.groups),
            popular = comicResp.popular,
            groups = comicResp.groups.mapValues { it.value.toModel() },
        )
    }

    suspend fun getFavorite(ordering: GetFavoriteOrdering, pageNum: Int): Result<List<FavoriteItem>> = runCatching {
        val client = CopyClient(config.apiDomain()) { config.token }
        val resp = client.getFavorite(config.token, ordering.param, pageNum)
        resp.list.map { it.toModel() }
    }

    suspend fun refreshAccountToken(): Result<Unit> = runCatching {
        val account = accountPool.acquireAccount()
        accountPool.prepare(account)
        Unit
    }
}
