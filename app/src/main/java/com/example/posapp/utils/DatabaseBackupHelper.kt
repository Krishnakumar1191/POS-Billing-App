package com.example.posapp.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class DatabaseBackupHelper(private val context: Context) {
    private val dbName = "pos_database"

    /**
     * Backs up the local Room database to Documents/POSAPP/ folder as a .zip file.
     */
    fun backupDatabase(): String? {
        return try {
            val dbFile = context.getDatabasePath(dbName)
            val shmFile = File(dbFile.parent, "$dbName-shm")
            val walFile = File(dbFile.parent, "$dbName-wal")

            // Create App Specific Backup Folder in public Documents directory
            val backupDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "POSAPP")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val zipFile = File(backupDir, "POSAPP_Backup_$timestamp.zip")

            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                listOf(dbFile, shmFile, walFile).forEach { file ->
                    if (file.exists()) {
                        FileInputStream(file).use { fis ->
                            val entry = ZipEntry(file.name)
                            zos.putNextEntry(entry)
                            fis.copyTo(zos)
                            zos.closeEntry()
                        }
                    }
                }
            }
            zipFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Restores the database from a given .zip Uri.
     */
    fun restoreDatabase(zipUri: Uri): Boolean {
        val dbFile = context.getDatabasePath(dbName)
        val shmFile = File(dbFile.parent, "$dbName-shm")
        val walFile = File(dbFile.parent, "$dbName-wal")

        return try {
            context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
                ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val outFile = when (entry.name) {
                            dbName -> dbFile
                            "$dbName-shm" -> shmFile
                            "$dbName-wal" -> walFile
                            else -> null
                        }
                        
                        outFile?.let {
                            FileOutputStream(it).use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}