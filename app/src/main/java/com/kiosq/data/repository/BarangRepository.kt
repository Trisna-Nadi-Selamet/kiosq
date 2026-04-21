package com.kiosq.data.repository

import androidx.lifecycle.LiveData
import com.kiosq.data.dao.BarangDao
import com.kiosq.data.entity.Barang
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BarangRepository(
    private val barangDao: BarangDao
) {

    val allBarang: LiveData<List<Barang>> = barangDao.getAllBarang()
    val allKategori: LiveData<List<String>> = barangDao.getAllKategori()
    val countBarang: LiveData<Int> = barangDao.countAllBarang()

    // FIX: wajib kirim parameter (batas default)
    val countStokRendah: LiveData<Int> = barangDao.countStokRendah(10)

    val stokRendah: LiveData<List<Barang>> = barangDao.getStokRendah(10)

    val totalNilaiStok: LiveData<Long> = barangDao.getTotalNilaiStok()

    fun searchBarang(query: String): LiveData<List<Barang>> {
        return barangDao.searchBarang(query)
    }

    fun getByKategori(kategori: String): LiveData<List<Barang>> {
        return barangDao.getBarangByKategori(kategori)
    }

    suspend fun getAllBarangList(): List<Barang> = withContext(Dispatchers.IO) {
        barangDao.getAllBarangList()
    }

    // FIX: nullable safety (DAO bisa null)
    suspend fun getBarangById(id: Long): Barang? =
    barangDao.getBarangById(id)

    suspend fun insertBarang(barang: Barang): Long = withContext(Dispatchers.IO) {
        barangDao.insertBarang(barang)
    }

    suspend fun updateBarang(barang: Barang) = withContext(Dispatchers.IO) {
        barangDao.updateBarang(
            barang.copy(updatedAt = System.currentTimeMillis())
        )
    }

    suspend fun deleteBarang(barang: Barang) = withContext(Dispatchers.IO) {
        barangDao.deleteBarang(barang)
    }

    suspend fun kurangiStok(id: Long, qty: Int) = withContext(Dispatchers.IO) {
        barangDao.kurangiStok(
            id = id,
            qty = qty,
            now = System.currentTimeMillis()
        )
    }

    suspend fun tambahStok(id: Long, qty: Int) = withContext(Dispatchers.IO) {
        barangDao.tambahStok(
            id = id,
            qty = qty,
            now = System.currentTimeMillis()
        )
    }

    suspend fun deleteAllBarang() = withContext(Dispatchers.IO) {
        barangDao.deleteAllBarang()
    }

    suspend fun getAllKategoriList(): List<String> = withContext(Dispatchers.IO) {
        barangDao.getAllKategoriList()
    }
}