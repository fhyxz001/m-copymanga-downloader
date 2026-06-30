package com.copymanga.downloader.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.math.pow

/**
 * 3 秒连接/读取超时，配合 OkHttp retryOnConnectionFailure 实现总时间约 5 秒的重试。
 */
class TimeoutInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val maxRetries = 3
        var lastException: IOException? = null

        for (attempt in 0..maxRetries) {
            try {
                return chain.proceed(request)
            } catch (e: SocketTimeoutException) {
                lastException = e
                if (attempt < maxRetries) {
                    val delayMs = (2.0.pow(attempt) * 200).toLong()
                    Thread.sleep(delayMs)
                }
            } catch (e: IOException) {
                lastException = e
                if (attempt < maxRetries) {
                    val delayMs = (2.0.pow(attempt) * 200).toLong()
                    Thread.sleep(delayMs)
                }
            }
        }
        throw lastException ?: IOException("请求重试后仍然失败")
    }
}
