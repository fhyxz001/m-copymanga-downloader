package com.copymanga.downloader.domain

import com.copymanga.downloader.data.model.Account
import com.copymanga.downloader.data.remote.CopyClient
import com.copymanga.downloader.data.remote.TokenExpiredException
import com.copymanga.downloader.data.store.AccountStore
import com.copymanga.downloader.util.randomPassword
import com.copymanga.downloader.util.randomUsername
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AccountPool(
    private val copyClient: CopyClient,
    private val accountStore: AccountStore,
) {
    private val accounts = accountStore.load().associateBy { it.username }.toMutableMap()
    private val registerLock = Mutex()

    /**
     * 获取一个可用账号。
     * 如果池中没有，则自动注册新账号。
     */
    suspend fun acquireAccount(): Account {
        findAvailableAccount()?.let { return it }

        registerLock.withLock {
            findAvailableAccount()?.let { return it }

            val username = randomUsername()
            val password = randomPassword()
            copyClient.register(username, password)
            val login = copyClient.login(username, password)
            val account = Account(
                username = username,
                password = password,
                token = login.token,
                limitedAt = 0,
                lastCheckTokenAt = System.currentTimeMillis() / 1000,
            )
            accounts[username] = account
            save()
            return account
        }
    }

    /**
     * 标记账号被限流（60 秒内不可用）。
     */
    suspend fun markLimited(account: Account) {
        val updated = account.copy(limitedAt = System.currentTimeMillis() / 1000)
        accounts[account.username] = updated
        save()
    }

    /**
     * 更新账号 token。
     */
    suspend fun updateToken(account: Account, token: String) {
        val updated = account.copy(
            token = token,
            lastCheckTokenAt = System.currentTimeMillis() / 1000,
        )
        accounts[account.username] = updated
        save()
    }

    /**
     * 准备账号：如果超过 24h 未检查，校验 token；过期则重新登录。
     * 返回准备后的账号（可能已修改 token/检查时间）。
     */
    suspend fun prepare(account: Account): Account? {
        return account.prepareLock.withLock {
            if (account.isLimited()) return@withLock null
            if (!account.shouldCheckToken()) return@withLock account

            val prepared = try {
                copyClient.getUserProfile(account.token)
                account.copy(lastCheckTokenAt = System.currentTimeMillis() / 1000)
            } catch (_: TokenExpiredException) {
                val login = copyClient.login(account.username, account.password)
                account.copy(token = login.token, lastCheckTokenAt = System.currentTimeMillis() / 1000)
            } catch (_: Exception) {
                return@withLock null
            }
            accounts[account.username] = prepared
            save()
            prepared
        }
    }

    private fun findAvailableAccount(): Account? {
        val now = System.currentTimeMillis() / 1000
        return accounts.values
            .filter { !it.isLimited(now) }
            .minByOrNull { it.lastCheckTokenAt }
    }

    private fun save() {
        accountStore.save(accounts.values.toList())
    }
}
