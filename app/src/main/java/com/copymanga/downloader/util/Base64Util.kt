package com.copymanga.downloader.util

import android.util.Base64

private const val SALT = 1729

fun encodePassword(password: String): String {
    return Base64.encodeToString(
        "$password-$SALT".toByteArray(Charsets.UTF_8),
        Base64.NO_WRAP
    )
}
