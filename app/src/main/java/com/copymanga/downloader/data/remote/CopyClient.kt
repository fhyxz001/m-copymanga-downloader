package com.copymanga.downloader.data.remote

import android.util.Base64
import com.copymanga.downloader.BuildConfig
import com.copymanga.downloader.data.remote.dto.ChapterInGetChaptersRespData
import com.copymanga.downloader.data.remote.dto.GetChapterRespData
import com.copymanga.downloader.data.remote.dto.GetChaptersRespData
import com.copymanga.downloader.data.remote.dto.GetComicRespData
import com.copymanga.downloader.data.remote.dto.GetFavoriteRespData
import com.copymanga.downloader.data.remote.dto.LoginRespData
import com.copymanga.downloader.data.remote.dto.SearchRespData
import com.copymanga.downloader.data.remote.dto.UserProfileRespData
import com.copymanga.downloader.data.remote.interceptor.AuthInterceptor
import com.copymanga.downloader.data.remote.interceptor.CommonHeaderInterceptor
import com.copymanga.downloader.data.remote.interceptor.TimeoutInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class CopyClient(
    apiDomain: String,
    tokenProvider: () -> String? = { null },
) {

    private val apiClient: CopyApi
    private val imgClient: CopyApi

    init {
        val apiUrl = "https://$apiDomain"
        apiClient = createRetrofit(apiUrl, tokenProvider, image = false)
        imgClient = createRetrofit(apiUrl, tokenProvider, image = true)
    }

    private fun createRetrofit(baseUrl: String, tokenProvider: () -> String?, image: Boolean): CopyApi {
        val builder = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(CommonHeaderInterceptor())
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(TimeoutInterceptor())
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            )
        }
        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(builder.build())
            .addConverterFactory(CopyJson.asConverterFactory(contentType))
            .build()
        return retrofit.create(CopyApi::class.java)
    }

    suspend fun register(username: String, password: String) {
        val resp = apiClient.register(username, password)
        handleEmptyBody(resp, "注册")
    }

    suspend fun login(username: String, password: String): LoginRespData {
        val encoded = Base64.encodeToString(
            "$password-1729".toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )
        val resp = apiClient.login(username, encoded)
        return parseCopyResp(resp, "登录")
    }

    suspend fun getUserProfile(token: String): UserProfileRespData {
        val resp = apiClient.getUserProfile("Token $token")
        return parseCopyResp(resp, "获取用户信息")
    }

    suspend fun search(keyword: String, pageNum: Int): SearchRespData {
        val limit = 20
        val offset = (pageNum - 1) * limit
        val params = mapOf(
            "limit" to limit.toString(),
            "offset" to offset.toString(),
            "q" to keyword,
            "q_type" to "",
            "platform" to "1",
        )
        val resp = apiClient.search(params)
        return parseCopyResp(resp, "搜索漫画")
    }

    suspend fun getComic(pathWord: String): GetComicRespData {
        val params = mapOf("platform" to "1")
        val resp = apiClient.getComic(pathWord, params)
        return parseCopyResp(resp, "获取漫画")
    }

    suspend fun getGroupChapters(pathWord: String, groupPathWord: String): List<ChapterInGetChaptersRespData> {
        val limit = 100
        val first = getChapters(pathWord, groupPathWord, limit, 0)
        val chapters = first.list.toMutableList()
        val totalPages = ((first.total + limit - 1) / limit).toInt()
        if (totalPages <= 1) return chapters

        coroutineScope {
            val deferred = (2..totalPages).map { page ->
                async {
                    val offset = (page - 1) * limit
                    getChapters(pathWord, groupPathWord, limit, offset)
                }
            }
            val remaining = deferred.awaitAll()
            remaining.forEach { chapters.addAll(it.list) }
        }
        return chapters
    }

    suspend fun getChapters(
        pathWord: String,
        groupPathWord: String,
        limit: Int,
        offset: Long,
    ): GetChaptersRespData {
        val params = mapOf(
            "limit" to limit.toString(),
            "offset" to offset.toString(),
            "platform" to "1",
        )
        val resp = apiClient.getChapters(pathWord, groupPathWord, params)
        return parseCopyResp(resp, "获取章节列表")
    }

    suspend fun getChapter(
        pathWord: String,
        groupPathWord: String,
        chapterUuid: String,
    ): GetChapterRespData {
        val params = mapOf("platform" to "1")
        val resp = apiClient.getChapter(pathWord, groupPathWord, chapterUuid, params)
        return parseCopyResp(resp, "获取章节内容")
    }

    suspend fun getFavorite(token: String, ordering: String, pageNum: Int): GetFavoriteRespData {
        val limit = 12
        val offset = (pageNum - 1) * limit
        val params = mapOf(
            "limit" to limit.toString(),
            "offset" to offset.toString(),
            "ordering" to ordering,
            "platform" to "1",
            "is_free" to "1",
        )
        val resp = apiClient.getFavorite("Token $token", params)
        return parseCopyResp(resp, "获取收藏")
    }

    suspend fun getImgData(url: String): Pair<ByteArray, String> {
        val upgraded = url.replace(".c800x.", ".c1500x.")
        val resp = imgClient.downloadImage(upgraded)
        val body = checkHttpStatus(resp, "下载图片")
        val bytes = body.bytes()
        val contentType = resp.headers["Content-Type"] ?: ""
        val format = when {
            contentType.contains("webp", ignoreCase = true) -> "webp"
            contentType.contains("jpeg", ignoreCase = true) || contentType.contains("jpg", ignoreCase = true) -> "jpeg"
            else -> guessFormatFromUrl(upgraded)
        }
        return bytes to format
    }

    private fun guessFormatFromUrl(url: String): String {
        return when {
            url.endsWith(".webp", ignoreCase = true) -> "webp"
            url.endsWith(".jpg", ignoreCase = true) || url.endsWith(".jpeg", ignoreCase = true) -> "jpeg"
            else -> "webp"
        }
    }

    private inline fun <reified T> parseCopyResp(response: Response<ResponseBody>, action: String): T {
        val body = checkHttpStatus(response, action)
        val text = body.string()
        val copyResp = try {
            CopyJson.decodeFromString(CopyResp.serializer(), text)
        } catch (e: Exception) {
            throw IllegalStateException("$action 失败，解析 CopyResp 失败: $text", e)
        }
        if (copyResp.code != COPY_CODE_SUCCESS) {
            throw IllegalStateException("$action 失败，code=${copyResp.code}: ${copyResp.message}")
        }
        return try {
            copyResp.decodeResults<T>(CopyJson)
        } catch (e: Exception) {
            throw IllegalStateException("$action 失败，解析 results 失败: ${copyResp.results}", e)
        }
    }

    private fun handleEmptyBody(response: Response<ResponseBody>, action: String) {
        checkHttpStatus(response, action)
    }

    private fun checkHttpStatus(response: Response<ResponseBody>, action: String): ResponseBody {
        val status = response.code()
        if (status == 210) {
            val text = response.body()?.string() ?: ""
            throw RiskControlException(text)
        }
        if (status == 401) throw TokenExpiredException()
        if (!response.isSuccessful) {
            val text = response.errorBody()?.string() ?: ""
            throw IllegalStateException("$action 失败，状态码($status): $text")
        }
        return response.body() ?: throw IllegalStateException("$action 失败，响应体为空")
    }
}
