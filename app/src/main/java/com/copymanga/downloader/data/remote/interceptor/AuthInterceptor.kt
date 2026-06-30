package com.copymanga.downloader.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider() ?: return chain.proceed(chain.request())
        val request = chain.request().newBuilder()
            .header("authorization", "Token $token")
            .build()
        return chain.proceed(request)
    }
}
