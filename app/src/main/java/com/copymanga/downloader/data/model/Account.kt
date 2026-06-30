package com.copymanga.downloader.data.model

import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Account(
    val username: String = "",
    val password: String = "",
    val token: String = "",
    val limitedAt: Long = 0,
    val lastCheckTokenAt: Long = 0,
) {
    @Transient
    val prepareLock: Mutex = Mutex()

    fun isLimited(now: Long = System.currentTimeMillis() / 1000): Boolean =
        now - limitedAt <= 60

    fun shouldCheckToken(now: Long = System.currentTimeMillis() / 1000): Boolean =
        now - lastCheckTokenAt > 24 * 60 * 60
}
