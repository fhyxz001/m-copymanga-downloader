package com.copymanga.downloader.util

import java.util.regex.Pattern

private val ORDER_PLACEHOLDER = Pattern.compile("\\{order(?::(.*?))?\\}")

fun sanitize(name: String): String {
    return name.map { c ->
        when (c) {
            '\\', '/' -> ' '
            ':' -> '：'
            '*' -> '⭐'
            '?' -> '？'
            '"' -> '\''
            '<' -> '《'
            '>' -> '》'
            '|' -> '丨'
            else -> c
        }
    }.joinToString("").trim()
}

fun formatTemplate(
    template: String,
    comicUuid: String = "",
    comicPathWord: String = "",
    comicTitle: String = "",
    author: String = "",
    groupPathWord: String = "",
    groupTitle: String = "",
    chapterUuid: String = "",
    chapterTitle: String = "",
    order: Double = 0.0,
    exportFormat: String = "",
): String {
    // 先处理 {order:xxx} 占位符（仅对整数部分补零，小数部分追加）
    val orderProcessed = preprocessOrderPlaceholder(template, order)

    val sanitizedComicTitle = sanitize(comicTitle)
    val sanitizedGroupTitle = sanitize(groupTitle)
    val sanitizedChapterTitle = sanitize(chapterTitle)

    return orderProcessed
        .replace("{comic_uuid}", sanitize(comicUuid))
        .replace("{comic_path_word}", sanitize(comicPathWord))
        .replace("{comic_title}", sanitizedComicTitle)
        .replace("{author}", sanitize(author))
        .replace("{group_path_word}", sanitize(groupPathWord))
        .replace("{group_title}", sanitizedGroupTitle)
        .replace("{chapter_uuid}", sanitize(chapterUuid))
        .replace("{chapter_title}", sanitizedChapterTitle)
        .replace("{order}", order.toString())
        .replace("{export_format}", sanitize(exportFormat))
}

private fun preprocessOrderPlaceholder(template: String, order: Double): String {
    val orderStr = order.toString()
    val dotIndex = orderStr.indexOf('.')
    val intPart = if (dotIndex >= 0) orderStr.substring(0, dotIndex) else orderStr
    val fracPart = if (dotIndex >= 0) orderStr.substring(dotIndex + 1) else ""
    val hasFrac = fracPart.isNotEmpty() && fracPart != "0"

    val matcher = ORDER_PLACEHOLDER.matcher(template)
    val sb = StringBuffer()
    while (matcher.find()) {
        val fmtSpec = matcher.group(1) ?: ""
        val formattedInt = if (fmtSpec.isNotEmpty()) {
            formatIntWithSpec(intPart, fmtSpec)
        } else {
            intPart
        }
        val replacement = if (hasFrac) "$formattedInt.$fracPart" else formattedInt
        matcher.appendReplacement(sb, replacement)
    }
    matcher.appendTail(sb)
    return sb.toString()
}

private fun formatIntWithSpec(intPart: String, spec: String): String {
    // 只支持 "0>N" 形式：整数部分补零到 N 位
    val trimmed = spec.trim()
    return when {
        trimmed.startsWith("0>") -> {
            val width = trimmed.substring(2).toIntOrNull() ?: return intPart
            intPart.padStart(width, '0')
        }
        trimmed.startsWith("0<") -> {
            val width = trimmed.substring(2).toIntOrNull() ?: return intPart
            intPart.padEnd(width, '0')
        }
        trimmed.startsWith(">") -> {
            val width = trimmed.substring(1).toIntOrNull() ?: return intPart
            intPart.padStart(width, ' ')
        }
        trimmed.startsWith("<") -> {
            val width = trimmed.substring(1).toIntOrNull() ?: return intPart
            intPart.padEnd(width, ' ')
        }
        else -> intPart
    }
}
