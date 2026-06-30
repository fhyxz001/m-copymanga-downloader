package com.copymanga.downloader.data.remote

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class RiskControl(val body: String) : ApiResult<Nothing>()
    data object TokenExpired : ApiResult<Nothing>()
    data class HttpError(val status: Int, val body: String) : ApiResult<Nothing>()
    data class NetworkError(val cause: Throwable) : ApiResult<Nothing>()
    data class DecodeError(val cause: Throwable) : ApiResult<Nothing>()
}

class RiskControlException(val body: String) : Exception("触发风控: $body")
class TokenExpiredException : Exception("token 错误或已过期")
