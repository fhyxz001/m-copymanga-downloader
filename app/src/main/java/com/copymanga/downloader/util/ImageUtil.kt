package com.copymanga.downloader.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.copymanga.downloader.data.model.DownloadFormat

fun convertToFormat(bytes: ByteArray, srcFormat: String, targetFormat: DownloadFormat): ByteArray {
    val sourceIsTarget = when (targetFormat) {
        DownloadFormat.Webp -> srcFormat.equals("webp", ignoreCase = true)
        DownloadFormat.Jpeg -> srcFormat.equals("jpeg", ignoreCase = true) || srcFormat.equals("jpg", ignoreCase = true)
    }
    if (sourceIsTarget) return bytes

    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: throw IllegalStateException("无法解码图片")

    return try {
        val out = java.io.ByteArrayOutputStream()
        when (targetFormat) {
            DownloadFormat.Webp -> {
                @Suppress("DEPRECATION")
                bitmap.compress(Bitmap.CompressFormat.WEBP, 100, out)
            }
            DownloadFormat.Jpeg -> bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        out.toByteArray()
    } finally {
        bitmap.recycle()
    }
}
