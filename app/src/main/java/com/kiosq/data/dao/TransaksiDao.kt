package com.kiosq.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kiosq.data.entity.Transaksi

@Dao
interface TransaksiDao {

    @Query("SELECT * FROM transaksi ORDER BY createdAt DESC")
    fun getAllTransaksi(): LiveData<List<Transaksi>>

    @Query("SELECT * FROM transaksi ORDER BY createdAt DESC")
    suspend fun getAllTransaksiList(): List<Transaksi>

    @Query("SELECT * FROM transaksi WHERE id = :id")
    suspend fun getTransaksiById(id: Long): Transaksi

    @Query("SELECT * FROM transaksi WHERE jenis = 'JUAL' ORDER BY createdAt DESC")
    fun getTransaksiJual(): LiveData<List<Transaksi>>

    @Query("""
        SELECT * FROM transaksi 
        WHERE createdAt >= :startMillis AND createdAt <= :endMillis 
        ORDER BY createdAt DESC
    """)
    fun getTransaksiByRange(startMillis: Long, endMillis: Long): LiveData<List<Transaksi>>

    @Query("""
        SELECT * FROM transaksi 
        WHERE createdAt >= :startMillis AND createdAt <= :endMillis 
        ORDER BY createdAt DESC
    """)
    suspend fun getTransaksiByRangeList(startMillis: Long, endMillis: Long): List<Transaksi>

    // =========================
    // STATISTIK
    // =========================

    @Query("SELECT SUM(total) FROM transaksi WHERE jenis = 'JUAL'")
    fun getTotalPendapatan(): LiveData<Long?>

    @Query("SELECT SUM(total) FROM transaksi WHERE jenis = 'JUAL' AND createdAt >= :startMillis")
    fun getTotalPendapatanSince(startMillis: Long): LiveData<Long?>

    @Query("SELECT COUNT(*) FROM transaksi WHERE jenis = 'JUAL'")
    fun getTotalTransaksiJual(): LiveData<Int>

    // =========================
    // TOP BARANG TERLARIS
    // =========================

    @Query("""
        SELECT namaBarang, SUM(jumlah) AS totalJual
        FROM transaksi
        WHERE jenis = 'JUAL'
        GROUP BY namaBarang
        ORDER BY totalJual DESC
        LIMIT 1
    """)
    fun getBarangTerlaris(): LiveData<List<NamaTotal>>

    @Query("""
        SELECT namaBarang, SUM(jumlah) AS totalJual
        FROM transaksi
        WHERE jenis = 'JUAL'
        GROUP BY namaBarang
        ORDER BY totalJual DESC
        LIMIT 5
    """)
    suspend fun getTop5Terlaris(): List<NamaTotal>

    // =========================
    // CRUD
    // =========================

    @Query("SELECT * FROM transaksi WHERE barangId = :barangId ORDER BY createdAt DESC")
    fun getTransaksiByBarang(barangId: Long): LiveData<List<Transaksi>>

    @Query("SELECT COUNT(*) FROM transaksi")
    fun countAllTransaksi(): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaksi(transaksi: Transaksi): Long

    @Delete
    suspend fun deleteTransaksi(transaksi: Transaksi)

    @Query("DELETE FROM transaksi WHERE id = :id")
    suspend fun deleteTransaksiById(id: Long)

    @Query("DELETE FROM transaksi")
    suspend fun deleteAllTransaksi()
}

// =========================
// DATA CLASS UNTUK QUERY
// =========================

data class NamaTotal(
    val namaBarang: String,
    val totalJual: Int
)