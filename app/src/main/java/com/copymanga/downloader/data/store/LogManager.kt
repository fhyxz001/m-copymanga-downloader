package com.copymanga.downloader.data.store

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel { DEBUG, INFO, WARN, ERROR }

class LogManager(context: Context) {

    private val logDir = File(context.getExternalFilesDir(null), "logs").apply { mkdirs() }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    @Volatile
    var enabled: Boolean = true

    fun log(level: LogLevel, tag: String, message: String) {
        if (!enabled) return
        val line = "${dateFormat.format(Date())} [${level.name}] $tag: $message\n"
        try {
            val logFile = File(logDir, "${fileDateFormat.format(Date())}.log")
            logFile.appendText(line, Charsets.UTF_8)
        } catch (_: Exception) {
            // 日志写入失败不应影响主流程
        }
    }

    fun readLogs(): String {
        return try {
            logDir.listFiles()
                ?.filter { it.isFile && it.name.endsWith(".log") }
                ?.sortedBy { it.name }
                ?.joinToString("") { it.readText(Charsets.UTF_8) }
                ?: ""
        } catch (e: Exception) {
            "读取日志失败: ${e.message}"
        }
    }

    fun clearLogs() {
        logDir.listFiles()?.filter { it.isFile && it.name.endsWith(".log") }?.forEach { it.delete() }
    }
}
