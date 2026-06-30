package com.copymanga.downloader.domain.export

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.copymanga.downloader.data.model.ChapterInfo
import com.copymanga.downloader.data.model.Comic
import com.copymanga.downloader.data.store.MetadataStore
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import java.io.File
import java.io.FileInputStream

class PdfExporter(private val metadataStore: MetadataStore) {

    fun export(
        comic: Comic,
        chapter: ChapterInfo,
        targetPdf: File,
        skipMode: com.copymanga.downloader.data.model.ExportSkipMode,
    ): Boolean {
        val chapterDir = chapter.chapterDownloadDir?.let { File(it) }
            ?: return false

        val shouldSkip = when (skipMode) {
            com.copymanga.downloader.data.model.ExportSkipMode.SkipExported -> chapter.isPdfExported
            com.copymanga.downloader.data.model.ExportSkipMode.SkipExisting -> targetPdf.exists()
            com.copymanga.downloader.data.model.ExportSkipMode.None -> false
        }
        if (shouldSkip) return true

        targetPdf.parentFile?.mkdirs()
        val imageFiles = chapterDir.listFiles()
            ?.filter { it.isFile && (it.extension == "webp" || it.extension == "jpg") }
            ?.sortedBy { it.name }
            ?: emptyList()

        createPdf(imageFiles, targetPdf)

        val updated = chapter.copy(isPdfExported = true)
        metadataStore.saveChapterMetadata(chapterDir, updated)
        return true
    }

    fun createPdf(images: List<File>, targetPdf: File) {
        PDDocument().use { doc ->
            images.forEach { img ->
                val bitmap = decodeBitmap(img) ?: return@forEach
                val page = PDPage(PDRectangle(bitmap.width.toFloat(), bitmap.height.toFloat()))
                doc.addPage(page)
                PDPageContentStream(doc, page).use { stream ->
                    val pdImage = if (img.extension == "jpg" || img.extension == "jpeg") {
                        JPEGFactory.createFromImage(doc, bitmap, 0.9f)
                    } else {
                        LosslessFactory.createFromImage(doc, bitmap)
                    }
                    stream.drawImage(pdImage, 0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
                }
                bitmap.recycle()
            }
            doc.save(targetPdf)
        }
    }

    fun mergePdfs(pdfs: List<File>, targetPdf: File, bookmarks: List<String>) {
        val merged = PDFMergerUtility()
        merged.destinationFileName = targetPdf.absolutePath
        pdfs.forEach { merged.addSource(it) }
        merged.mergeDocuments(null)

        // 添加书签
        PDDocument.load(targetPdf).use { doc ->
            val outline = PDDocumentOutline()
            doc.documentCatalog.documentOutline = outline
            bookmarks.forEachIndexed { index, title ->
                val item = PDOutlineItem()
                item.title = title
                val page = doc.getPage(index)
                item.destination = page
                outline.addLast(item)
            }
            doc.save(targetPdf)
        }
    }

    private fun decodeBitmap(file: File): Bitmap? {
        return try {
            BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
            BitmapFactory.decodeStream(FileInputStream(file))
        } catch (e: Exception) {
            null
        }
    }
}

// PDFMergerUtility 别名（PdfBox-Android 实际类路径）
private typealias PDFMergerUtility = com.tom_roush.pdfbox.multipdf.PDFMergerUtility
