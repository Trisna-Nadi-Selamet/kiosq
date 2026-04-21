package com.kiosq.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kiosq.data.entity.Barang
import com.kiosq.data.entity.Transaksi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileHelper {

    private val TAG = "FileHelper"
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    // ─── CSV EXPORT BARANG ────────────────────────────────────────────────
    suspend fun exportBarangCsv(context: Context, barangList: List<Barang>): Result<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = getOutputDir(context)
                val file = File(dir, "kiosq_barang.csv")
                FileWriter(file).use { writer ->
                    writer.appendLine("id,nama,kategori,jumlah,satuan,harga_jual,harga_modal,created_at")
                    barangList.forEach { b ->
                        writer.appendLine(
                            "${b.id},\"${b.nama}\",\"${b.kategori}\",${b.jumlah},${b.satuan},${b.hargaJual},${b.hargaModal},${
                                formatDate(b.createdAt)
                            }"
                        )
                    }
                }
                file
            }
        }

    // ─── CSV EXPORT TRANSAKSI ─────────────────────────────────────────────
    suspend fun exportTransaksiCsv(context: Context, transaksiList: List<Transaksi>): Result<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = getOutputDir(context)
                val file = File(dir, "kiosq_transaksi.csv")
                FileWriter(file).use { writer ->
                    writer.appendLine("id,barang_id,nama_barang,jenis,jumlah,harga_satuan,total,catatan,tanggal")
                    transaksiList.forEach { t ->
                        writer.appendLine(
                            "${t.id},${t.barangId},\"${t.namaBarang}\",${t.jenis},${t.jumlah},${t.hargaSatuan},${t.total},\"${t.catatan}\",${
                                formatDate(t.createdAt)
                            }"
                        )
                    }
                }
                file
            }
        }

    // ─── CSV IMPORT BARANG ────────────────────────────────────────────────
    suspend fun importBarangCsv(context: Context): Result<List<Barang>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = File(getOutputDir(context), "kiosq_barang.csv")
                if (!file.exists()) throw Exception("File kiosq_barang.csv tidak ditemukan")

                val result = mutableListOf<Barang>()
                var lineNumber = 0
                FileReader(file).buffered().use { reader ->
                    reader.lineSequence().forEach { line ->
                        lineNumber++
                        if (lineNumber == 1) return@forEach // skip header
                        try {
                            val cols = parseCsvLine(line)
                            if (cols.size >= 7) {
                                result.add(
                                    Barang(
                                        nama = cols[1].trim('"'),
                                        kategori = cols[2].trim('"'),
                                        jumlah = cols[3].toInt(),
                                        satuan = cols[4],
                                        hargaJual = cols[5].toLong(),
                                        hargaModal = cols[6].toLong()
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Skip baris invalid #$lineNumber: ${e.message}")
                            // skip baris rusak, jangan crash
                        }
                    }
                }
                result
            }
        }

    // ─── BACKUP JSON ──────────────────────────────────────────────────────
    suspend fun backupJson(
        context: Context,
        barangList: List<Barang>,
        transaksiList: List<Transaksi>
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = getOutputDir(context)
            val file = File(dir, "backup.json")
            val backup = BackupData(
                version = 1,
                tanggal = formatDate(System.currentTimeMillis()),
                barang = barangList,
                transaksi = transaksiList
            )
            FileWriter(file).use { writer ->
                gson.toJson(backup, writer)
            }
            file
        }
    }

    // ─── RESTORE JSON ─────────────────────────────────────────────────────
    suspend fun restoreJson(context: Context): Result<BackupData> =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = File(getOutputDir(context), "backup.json")
                if (!file.exists()) throw Exception("File backup.json tidak ditemukan")
                FileReader(file).use { reader ->
                    gson.fromJson(reader, BackupData::class.java)
                        ?: throw Exception("Format backup tidak valid")
                }
            }
        }

    // ─── SHARE FILE ───────────────────────────────────────────────────────
    fun shareFile(context: Context, file: File, mimeType: String = "text/csv"): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Export Kios Q - ${file.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun getFileInfo(context: Context, fileName: String): FileInfo? {
        val file = File(getOutputDir(context), fileName)
        return if (file.exists()) {
            FileInfo(
                nama = file.name,
                ukuran = formatFileSize(file.length()),
                tanggal = formatDate(file.lastModified()),
                path = file.absolutePath
            )
        } else null
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────
    private fun getOutputDir(context: Context): File {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun formatDate(millis: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(millis))
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var inQuotes = false
        val current = StringBuilder()
        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        result.add(current.toString())
        return result
    }
}

data class BackupData(
    val version: Int,
    val tanggal: String,
    val barang: List<Barang>,
    val transaksi: List<Transaksi>
)

data class FileInfo(
    val nama: String,
    val ukuran: String,
    val tanggal: String,
    val path: String
)
