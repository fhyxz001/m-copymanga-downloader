package com.copymanga.downloader.data.store

import android.content.Context
import com.copymanga.downloader.data.model.Account
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private const val ACCOUNT_FILE = "account.json"

class AccountStore(context: Context) {

    private val accountFile = File(context.getExternalFilesDir(null), ACCOUNT_FILE)

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        prettyPrint = true
    }

    fun load(): List<Account> {
        if (!accountFile.exists()) {
            accountFile.writeText("[]", Charsets.UTF_8)
            return emptyList()
        }
        val text = accountFile.readText(Charsets.UTF_8)
        return try {
            json.decodeFromString<List<Account>>(text)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(accounts: List<Account>) {
        accountFile.parentFile?.mkdirs()
        accountFile.writeText(json.encodeToString(accounts), Charsets.UTF_8)
    }
}
