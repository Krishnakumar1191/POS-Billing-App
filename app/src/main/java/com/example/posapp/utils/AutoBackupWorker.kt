package com.example.posapp.utils

import android.content.Context
import android.os.Environment
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.example.posapp.data.AppDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class AutoBackupWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            createDatabaseZipBackup(context)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        fun createDatabaseZipBackup(context: Context): File? {
            // Checkpoint Room WAL database so all latest memory transactions are written to disk
            try {
                val db = AppDatabase.getDatabase(context)
                db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Target Folder: Documents/POSAPP/Autobackup
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val backupFolder = File(documentsDir, "POSAPP/Autobackup")
            if (!backupFolder.exists()) {
                backupFolder.mkdirs()
            }

            // Database source files
            val dbFile = context.getDatabasePath("pos_database")
            val shmFile = File(dbFile.path + "-shm")
            val walFile = File(dbFile.path + "-wal")

            if (!dbFile.exists()) return null

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val zipFile = File(backupFolder, "POSAPP_Backup_$timestamp.zip")

            val filesToZip = listOfNotNull(
                dbFile.takeIf { it.exists() },
                shmFile.takeIf { it.exists() },
                walFile.takeIf { it.exists() }
            )

            ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                for (file in filesToZip) {
                    FileInputStream(file).use { fis ->
                        val zipEntry = ZipEntry(file.name)
                        zipOut.putNextEntry(zipEntry)
                        fis.copyTo(zipOut)
                        zipOut.closeEntry()
                    }
                }
            }

            // Keep only the 30 most recent backups to manage storage space
            val existingBackups = backupFolder.listFiles { _, name -> name.startsWith("POSAPP_Backup_") && name.endsWith(".zip") }
            if (existingBackups != null && existingBackups.size > 30) {
                existingBackups.sortBy { it.lastModified() }
                existingBackups.take(existingBackups.size - 30).forEach { it.delete() }
            }

            return zipFile
        }
    }
}