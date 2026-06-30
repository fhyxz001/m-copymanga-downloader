package com.copymanga.downloader

import android.app.Application
import com.copymanga.downloader.di.AppContainer
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class CopyMangaApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        PDFBoxResourceLoader.init(applicationContext)
    }
}
