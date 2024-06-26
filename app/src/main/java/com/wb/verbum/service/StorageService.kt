package com.wb.verbum.service

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class StorageService(private val context: Context) {

    @Synchronized
    fun saveFileToStorage(data: ByteArray, fileName: String) {
        try {
            // Get the directory path from the fileName
            val directoryPath = fileName.substringBeforeLast(File.separator)

            // Create the directory if it doesn't exist
            val directory = File(context.filesDir, directoryPath)
            if (!directory.exists()) {
                directory.mkdirs()
            }

            // Get the file name without the directory path
            val fileNameWithoutPath = fileName.substringAfterLast(File.separator)

            //Check if file does not exists so we avoid duplicates
            if (doesFileExistsInStorage(fileName)) {
                return
            }

            // Save the file to the specified location
            val file = File(directory, fileNameWithoutPath)
            val outputStream = FileOutputStream(file)
            outputStream.write(data)
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun retrieveFile(fileName: String): File? {
        return try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                file
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun doesFileExistsInStorage(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return file.exists()
    }

    @Synchronized
    fun deleteFileFromStorage(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }
}