package com.copymanga.downloader.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 注入拷贝漫画 API 要求的公共请求头。
 */
class CommonHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", "COPY/3.0.0")
            .header("version", "2025.08.15")
            .header("platform", "1")
            .header("webp", "1")
            .header("region", "1")
            .build()
        return chain.proceed(request)
    }
}
