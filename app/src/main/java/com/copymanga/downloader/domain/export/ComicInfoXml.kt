package com.copymanga.downloader.domain.export

import com.copymanga.downloader.data.model.ComicInfo

fun ComicInfo.toXml(): String {
    val sb = StringBuilder()
    sb.appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    sb.appendLine("<ComicInfo>")
    sb.appendLine("  <Manga>${manga.xmlEscape()}</Manga>")
    sb.appendLine("  <Series>${series.xmlEscape()}</Series>")
    sb.appendLine("  <Publisher>${publisher.xmlEscape()}</Publisher>")
    sb.appendLine("  <Writer>${writer.xmlEscape()}</Writer>")
    sb.appendLine("  <Genre>${genre.xmlEscape()}</Genre>")
    sb.appendLine("  <Summary>${summary.xmlEscape()}</Summary>")
    sb.appendLine("  <Title>${title.xmlEscape()}</Title>")
    number?.let { sb.appendLine("  <Number>$it</Number>") }
    volume?.let { sb.appendLine("  <Volume>$it</Volume>") }
    format?.let { sb.appendLine("  <Format>${it.xmlEscape()}</Format>") }
    sb.appendLine("  <PageCount>$pageCount</PageCount>")
    sb.appendLine("  <Count>$count</Count>")
    sb.appendLine("</ComicInfo>")
    return sb.toString()
}

private fun String.xmlEscape(): String {
    return this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
