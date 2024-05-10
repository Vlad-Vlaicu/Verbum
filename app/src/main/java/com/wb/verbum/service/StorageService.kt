package com.wb.verbum.service

import android.content.Context
import java.io.File
import java.io.IOException

class StorageService(private val context: Context) {
    fun saveFileToStorage(data: ByteArray, fileName: String) {
        try {
            val outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            outputStream.write(data)
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun doesFileExistsInStorage(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return file.exists()
    }

    fun deleteFileFromStorage(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }
}