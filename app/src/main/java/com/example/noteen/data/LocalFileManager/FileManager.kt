package com.example.noteen.data.LocalFileManager

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.util.UUID

@SuppressLint("StaticFieldLeak")
object FileManager {
    private lateinit var context: Context

    fun init(appContext: Context) {
        context = appContext.applicationContext
    }

    fun saveImage(uri: Uri): File? {
        return saveToAppFolder(
            uri = uri,
            subFolder = "images",
            mimeTypePrefix = "image"
        )
    }

    fun saveBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG): File? {
        val folder = File(context.getExternalFilesDir(null), "images")
        if (!folder.exists()) folder.mkdirs()

        val ext = when (format) {
            Bitmap.CompressFormat.JPEG -> "jpg"
            Bitmap.CompressFormat.PNG -> "png"
            Bitmap.CompressFormat.WEBP -> "webp"
            else -> "png"
        }
        val fileName = "${UUID.randomUUID()}.$ext"
        val destFile = File(folder, fileName)

        return try {
            destFile.outputStream().use { out ->
                bitmap.compress(format, 100, out)
            }
            destFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveAudio(uri: Uri): File? {
        return saveToAppFolder(
            uri = uri,
            subFolder = "audio",
            mimeTypePrefix = "audio"
        )
    }

    fun deleteFile(file: File): Boolean {
        return file.exists() && file.delete()
    }

    fun clearFolder(folderName: String): Boolean {
        val dir = File(context.getExternalFilesDir(null), folderName)
        if (!dir.exists() || !dir.isDirectory) return false
        dir.listFiles()?.forEach { it.delete() }
        return true
    }

    fun listFilesIn(folderName: String): List<File> {
        val dir = File(context.getExternalFilesDir(null), folderName)
        if (!dir.exists()) return emptyList()
        return dir.listFiles()?.toList() ?: emptyList()
    }

    private fun saveToAppFolder(
        uri: Uri,
        subFolder: String,
        mimeTypePrefix: String
    ): File? {
        val resolver = context.contentResolver

        val mimeType = resolver.getType(uri)
        if (mimeType == null || !mimeType.startsWith(mimeTypePrefix)) return null

        val folder = File(context.getExternalFilesDir(null), subFolder)
        if (!folder.exists()) folder.mkdirs()

        val fileName = getSafeFileName(uri) ?: generateRandomFileName(mimeType)
        val destFile = File(folder, fileName)

        return try {
            resolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            destFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getSafeFileName(uri: Uri): String? {
        val resolver = context.contentResolver
        var name: String? = null

        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            resolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    name = cursor.getString(nameIndex)
                }
            }
        } else if (uri.scheme == ContentResolver.SCHEME_FILE) {
            name = File(uri.path ?: return null).name
        }

        return if (name != null && isSafeFileName(name!!)) name else null
    }

    private fun isSafeFileName(name: String): Boolean {
        val regex = Regex("^[a-zA-Z0-9._-]+\\.(jpg|jpeg|png|webp|mp3|wav|m4a)$", RegexOption.IGNORE_CASE)
        return regex.matches(name)
    }

    private fun generateRandomFileName(mimeType: String): String {
        val ext = when {
            mimeType.contains("jpeg") -> "jpg"
            mimeType.contains("png") -> "png"
            mimeType.contains("webp") -> "webp"
            mimeType.contains("mp3") -> "mp3"
            mimeType.contains("wav") -> "wav"
            mimeType.contains("m4a") -> "m4a"
            else -> "bin"
        }
        return "${UUID.randomUUID()}.$ext"
    }
}
