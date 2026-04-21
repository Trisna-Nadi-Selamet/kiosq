package com.kiosq.data.repository

import androidx.lifecycle.LiveData
import com.kiosq.data.dao.NamaTotal
import com.kiosq.data.dao.TransaksiDao
import com.kiosq.data.entity.Transaksi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TransaksiRepository(private val transaksiDao: TransaksiDao) {

    val allTransaksi: LiveData<List<Transaksi>> = transaksiDao.getAllTransaksi()
    val transaksiJual: LiveData<List<Transaksi>> = transaksiDao.getTransaksiJual()
    val totalPendapatan: LiveData<Long?> = transaksiDao.getTotalPendapatan()
    val totalTransaksiJual: LiveData<Int> = transaksiDao.getTotalTransaksiJual()
    val barangTerlaris: LiveData<String?> = transaksiDao.getBarangTerlaris()
    val countAllTransaksi: LiveData<Int> = transaksiDao.countAllTransaksi()

    fun getTransaksiByRange(start: Long, end: Long): LiveData<List<Transaksi>> =
        transaksiDao.getTransaksiByRange(start, end)

    fun getTotalPendapatanSince(startMillis: Long): LiveData<Long?> =
        transaksiDao.getTotalPendapatanSince(startMillis)

    fun getTransaksiByBarang(barangId: Long): LiveData<List<Transaksi>> =
        transaksiDao.getTransaksiByBarang(barangId)

    suspend fun getAllTransaksiList(): List<Transaksi> = withContext(Dispatchers.IO) {
        transaksiDao.getAllTransaksiList()
    }

    suspend fun getTransaksiByRangeList(start: Long, end: Long): List<Transaksi> =
        withContext(Dispatchers.IO) {
            transaksiDao.getTransaksiByRangeList(start, end)
        }

    suspend fun insertTransaksi(transaksi: Transaksi): Long = withContext(Dispatchers.IO) {
        transaksiDao.insertTransaksi(transaksi)
    }

    suspend fun deleteTransaksi(transaksi: Transaksi) = withContext(Dispatchers.IO) {
        transaksiDao.deleteTransaksi(transaksi)
    }

    suspend fun deleteAllTransaksi() = withContext(Dispatchers.IO) {
        transaksiDao.deleteAllTransaksi()
    }

    suspend fun getTop5Terlaris(): List<NamaTotal> = withContext(Dispatchers.IO) {
        transaksiDao.getTop5Terlaris()
    }
}
