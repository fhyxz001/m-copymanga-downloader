package com.copymanga.downloader.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.Serializable

const val COPY_CODE_SUCCESS = 200L

/**
 * API 统一响应外壳。
 * `results` 为原始 JsonElement，由调用方按具体 DTO 解码。
 */
@Serializable
data class CopyResp(
    val code: Long,
    val message: String,
    val results: JsonElement,
)

inline fun <reified T> CopyResp.decodeResults(json: Json = CopyJson): T =
    json.decodeFromJsonElement(results)

val CopyJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
    prettyPrint = false
}
