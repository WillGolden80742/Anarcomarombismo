package com.example.anarcomarombismo.Controller.Util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.example.anarcomarombismo.R
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class ShareFiles {
    companion object {

        fun exportToFile(
            context: Context,
            fileName: String,
            content: String,
            onSuccess: () -> Unit,
            onError: () -> Unit
        ): Boolean {
            return try {
                val file = createFile(context, fileName)
                writeToFile(file,GZIP.compressText(content))
                shareFile(context, file)
                onSuccess()
                true
            } catch (e: IOException) {
                Toast.makeText(context, context.getString(R.string.file_export_error), Toast.LENGTH_SHORT).show()
                onError()
                false
            }
        }

        fun importFromFile(
            context: Context,
            uri: Uri,
            onSuccess: (String) -> Unit,
            onError: () -> Unit
        ) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val content = StringBuilder()
                    reader.forEachLine { content.append(it).append("\n") }
                    reader.close()
                    onSuccess(GZIP.decompressText(content.toString()))
                } else {
                    Toast.makeText(context, context.getString(R.string.error_file_empty), Toast.LENGTH_SHORT).show()
                    onError()
                }
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.error_reading_file), Toast.LENGTH_SHORT).show()
                onError()
            }
        }


        private fun createFile(context: Context, fileName: String): File {
            return File(context.filesDir, fileName).apply {
                if (!exists()) createNewFile()
            }
        }

        private fun writeToFile(file: File, content: String) {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
        }

        private fun shareFile(context: Context, file: File) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}