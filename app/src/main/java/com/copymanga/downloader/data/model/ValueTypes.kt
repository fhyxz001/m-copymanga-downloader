package com.copymanga.downloader.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Author(
    val name: String = "",
    val alias: String? = null,
    @SerialName("path_word") val pathWord: String = "",
)

@Serializable
data class Theme(
    val name: String = "",
    @SerialName("path_word") val pathWord: String = "",
)

@Serializable
data class LabeledValue(
    val value: Long = 0,
    val display: String = "",
)

@Serializable
data class LastChapter(
    val uuid: String = "",
    val name: String = "",
)

@Serializable
data class Group(
    @SerialName("path_word") val pathWord: String = "",
    val count: Int = 0,
    val name: String = "",
)
