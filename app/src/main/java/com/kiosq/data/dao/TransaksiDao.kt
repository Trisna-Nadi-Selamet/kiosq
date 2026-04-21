package com.kiosq.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kiosq.data.entity.Transaksi

@Dao
interface TransaksiDao {

    @Query("SELECT * FROM transaksi ORDER BY createdAt DESC")
    fun getAllTransaksi(): LiveData<List<Transaksi>>

    @Insert
    suspend fun insertTransaksi(transaksi: Transaksi): Long

    @Query("DELETE FROM transaksi")
    suspend fun deleteAllTransaksi()

    @Query("""
        SELECT namaBarang, SUM(jumlah) as totalJual
        FROM transaksi
        WHERE jenis = 'JUAL'
        GROUP BY namaBarang
        ORDER BY totalJual DESC
        LIMIT 1
    """)
    fun getBarangTerlaris(): LiveData<NamaTotal?>
}

data class NamaTotal(
    val namaBarang: String,
    val totalJual: Int
)