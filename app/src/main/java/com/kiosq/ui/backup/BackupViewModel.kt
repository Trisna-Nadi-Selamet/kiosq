package com.kiosq.ui.backup

import android.app.Application
import androidx.lifecycle.*
import com.kiosq.data.database.KiosQDatabase
import com.kiosq.data.repository.BarangRepository
import com.kiosq.data.repository.TransaksiRepository
import com.kiosq.util.FileHelper
import com.kiosq.util.FileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class BackupViewModel(application: Application) : AndroidViewModel(application) {

    private val barangRepo: BarangRepository
    private val transaksiRepo: TransaksiRepository

    private val _operationResult = MutableLiveData<String>()
    val operationResult: LiveData<String> = _operationResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _backupInfo = MutableLiveData<FileInfo>()
    val backupInfo: LiveData<FileInfo> = _backupInfo

    private val _csvBarangInfo = MutableLiveData<FileInfo>()
    val csvBarangInfo: LiveData<FileInfo> = _csvBarangInfo

    private val _csvTransaksiInfo = MutableLiveData<FileInfo>()
    val csvTransaksiInfo: LiveData<FileInfo> = _csvTransaksiInfo

    private val _shareFile = MutableLiveData<Pair<File, String>>()
    val shareFile: LiveData<Pair<File, String>> = _shareFile

    init {
        val db = KiosQDatabase.getInstance(application)
        barangRepo = BarangRepository(db.barangDao())
        transaksiRepo = TransaksiRepository(db.transaksiDao())
        refreshFileInfo()
    }

    fun refreshFileInfo() {
        _backupInfo.value = FileHelper.getFileInfo(getApplication(), "backup.json")
        _csvBarangInfo.value = FileHelper.getFileInfo(getApplication(), "kiosq_barang.csv")
        _csvTransaksiInfo.value = FileHelper.getFileInfo(getApplication(), "kiosq_transaksi.csv")
    }

    fun backupData() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)

        try {
            val barang = barangRepo.getAllBarangList()
            val transaksi = transaksiRepo.getAllTransaksiList()

            FileHelper.backupJson(getApplication(), barang, transaksi)

            _operationResult.postValue(
                "Backup berhasil! ${barang.size} barang, ${transaksi.size} transaksi"
            )
            refreshFileInfo()

        } catch (e: Exception) {
            _operationResult.postValue("Backup gagal: ${e.message}")
        }

        _isLoading.postValue(false)
    }

    fun restoreData() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)

        try {
            val backup = FileHelper.restoreJson(getApplication())

            barangRepo.deleteAllBarang()
            transaksiRepo.deleteAllTransaksi()

            backup.barang.forEach { barangRepo.insertBarang(it) }
            backup.transaksi.forEach { transaksiRepo.insertTransaksi(it) }

            _operationResult.postValue(
                "Restore berhasil! ${backup.barang.size} barang, ${backup.transaksi.size} transaksi"
            )

        } catch (e: Exception) {
            _operationResult.postValue("Restore gagal: ${e.message}")
        }

        _isLoading.postValue(false)
    }

    fun exportBarangCsv() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)

        try {
            val list = barangRepo.getAllBarangList()
            val file = FileHelper.exportBarangCsv(getApplication(), list)

            _operationResult.postValue("Export ${list.size} barang berhasil")
            refreshFileInfo()
            _shareFile.postValue(Pair(file, "text/csv"))

        } catch (e: Exception) {
            _operationResult.postValue("Export gagal: ${e.message}")
        }

        _isLoading.postValue(false)
    }

    fun exportTransaksiCsv() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)

        try {
            val list = transaksiRepo.getAllTransaksiList()
            val file = FileHelper.exportTransaksiCsv(getApplication(), list)

            _operationResult.postValue("Export ${list.size} transaksi berhasil")
            refreshFileInfo()
            _shareFile.postValue(Pair(file, "text/csv"))

        } catch (e: Exception) {
            _operationResult.postValue("Export gagal: ${e.message}")
        }

        _isLoading.postValue(false)
    }

    fun importBarangCsv() = viewModelScope.launch(Dispatchers.IO) {
        _isLoading.postValue(true)

        try {
            val list = FileHelper.importBarangCsv(getApplication())
            list.forEach { barangRepo.insertBarang(it) }

            _operationResult.postValue("Import ${list.size} barang berhasil")

        } catch (e: Exception) {
            _operationResult.postValue("Import gagal: ${e.message}")
        }

        _isLoading.postValue(false)
    }

    fun shareBackupFile() {
        val info = FileHelper.getFileInfo(getApplication(), "backup.json")

        if (info != null) {
            val file = File(info.path)
            _shareFile.value = Pair(file, "application/json")
        } else {
            _operationResult.value = "File backup tidak ada. Buat backup dulu."
        }
    }

    fun clearResult() {
        _operationResult.value = null
    }

    fun clearShareFile() {
        _shareFile.value = null
    }
}