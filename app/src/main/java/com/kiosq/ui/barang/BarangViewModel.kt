package com.kiosq.ui.barang

import android.app.Application
import androidx.lifecycle.*
import com.kiosq.data.database.KiosQDatabase
import com.kiosq.data.entity.Barang
import com.kiosq.data.repository.BarangRepository
import com.kiosq.util.FileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class BarangViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BarangRepository
    private val _searchQuery = MutableLiveData<String>("")
    private val _selectedKategori = MutableLiveData<String?>()

    val allBarang: LiveData<List<Barang>>
    val allKategori: LiveData<List<String>>
    val countBarang: LiveData<Int>
    val countStokRendah: LiveData<Int>
    val stokRendah: LiveData<List<Barang>>
    val totalNilaiStok: LiveData<Long?>

    val filteredBarang: LiveData<List<Barang>> = MediatorLiveData<List<Barang>>().apply {
        var allList = emptyList<Barang>()
        var query = ""
        var kategori: String? = null

        fun filterAndPost() {
            val filtered = allList.filter { b ->
                val matchQuery = if (query.isBlank()) true
                else b.nama.contains(query, true) || b.kategori.contains(query, true)
                val matchKategori = if (kategori == null) true else b.kategori == kategori
                matchQuery && matchKategori
            }
            postValue(filtered)
        }
    }

    private val _operationResult = MutableLiveData<String?>()
    val operationResult: LiveData<String?> = _operationResult

    private val _exportFile = MutableLiveData<File?>()
    val exportFile: LiveData<File?> = _exportFile

    init {
        val db = KiosQDatabase.getInstance(application)
        repository = BarangRepository(db.barangDao())

        allBarang = repository.allBarang
        allKategori = repository.allKategori
        countBarang = repository.countBarang
        countStokRendah = repository.countStokRendah
        stokRendah = repository.stokRendah
        totalNilaiStok = repository.totalNilaiStok

        (filteredBarang as MediatorLiveData).apply {
            var allList = emptyList<Barang>()
            var query = ""
            var kategori: String? = null

            fun filterAndPost() {
                val filtered = allList.filter { b ->
                    val mQ = if (query.isBlank()) true
                    else b.nama.contains(query, true) || b.kategori.contains(query, true)
                    val mK = if (kategori == null) true else b.kategori == kategori
                    mQ && mK
                }
                postValue(filtered)
            }

            addSource(allBarang) { list -> allList = list; filterAndPost() }
            addSource(_searchQuery) { q -> query = q; filterAndPost() }
            addSource(_selectedKategori) { k -> kategori = k; filterAndPost() }
        }
    }

    fun search(query: String) { _searchQuery.value = query }
    fun filterByKategori(kategori: String?) { _selectedKategori.value = kategori }

    fun insertBarang(barang: Barang) = viewModelScope.launch {
        repository.insertBarang(barang)
        _operationResult.postValue("Barang berhasil ditambahkan")
    }

    fun updateBarang(barang: Barang) = viewModelScope.launch {
        repository.updateBarang(barang)
        _operationResult.postValue("Barang berhasil diupdate")
    }

    fun deleteBarang(barang: Barang) = viewModelScope.launch {
        repository.deleteBarang(barang)
        _operationResult.postValue("Barang berhasil dihapus")
    }

    fun exportCsv() = viewModelScope.launch(Dispatchers.IO) {
        val list = repository.getAllBarangList()
        val result = FileHelper.exportBarangCsv(getApplication(), list)
        result.onSuccess { file ->
            _exportFile.postValue(file)
            _operationResult.postValue("Export berhasil: ${file.name}")
        }.onFailure {
            _operationResult.postValue("Export gagal: ${it.message}")
        }
    }

    fun importCsv(onResult: (List<Barang>) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        val result = FileHelper.importBarangCsv(getApplication())
        result.onSuccess { list ->
            list.forEach { repository.insertBarang(it) }
            _operationResult.postValue("Import ${list.size} barang berhasil")
            onResult(list)
        }.onFailure {
            _operationResult.postValue("Import gagal: ${it.message}")
            onResult(emptyList())
        }
    }

    fun clearResult() { _operationResult.value = null }
    fun clearExportFile() { _exportFile.value = null }
}
