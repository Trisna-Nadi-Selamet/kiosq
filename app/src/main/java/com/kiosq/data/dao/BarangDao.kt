package com.kiosq.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kiosq.data.entity.Barang

@Dao
interface BarangDao {

    @Query("SELECT * FROM barang ORDER BY nama ASC")
    fun getAllBarang(): LiveData<List<Barang>>

    @Query("SELECT * FROM barang ORDER BY nama ASC")
    suspend fun getAllBarangList(): List<Barang>

    @Query("SELECT * FROM barang WHERE id = :id")
    suspend fun getBarangById(id: Long): Barang?

    @Query("SELECT * FROM barang WHERE nama LIKE '%' || :query || '%' OR kategori LIKE '%' || :query || '%' ORDER BY nama ASC")
    fun searchBarang(query: String): LiveData<List<Barang>>

    @Query("SELECT * FROM barang WHERE kategori = :kategori ORDER BY nama ASC")
    fun getBarangByKategori(kategori: String): LiveData<List<Barang>>

    @Query("SELECT DISTINCT kategori FROM barang ORDER BY kategori ASC")
    fun getAllKategori(): LiveData<List<String>>

    @Query("SELECT DISTINCT kategori FROM barang ORDER BY kategori ASC")
    suspend fun getAllKategoriList(): List<String>

    @Query("SELECT * FROM barang WHERE jumlah <= :batasMinimum ORDER BY jumlah ASC")
    fun getStokRendah(batasMinimum: Int = 5): LiveData<List<Barang>>

    @Query("SELECT COUNT(*) FROM barang WHERE jumlah <= :batasMinimum")
    fun countStokRendah(batasMinimum: Int = 5): LiveData<Int>

    @Query("SELECT COUNT(*) FROM barang")
    fun countAllBarang(): LiveData<Int>

    @Query("SELECT COALESCE(SUM(jumlah * hargaModal), 0) FROM barang")
    fun getTotalNilaiStok(): LiveData<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBarang(barang: Barang): Long

    @Update
    suspend fun updateBarang(barang: Barang)

    @Query("UPDATE barang SET jumlah = jumlah - :qty, updatedAt = :now WHERE id = :id")
    suspend fun kurangiStok(
        id: Long,
        qty: Int,
        now: Long = System.currentTimeMillis()
    )

    @Query("UPDATE barang SET jumlah = jumlah + :qty, updatedAt = :now WHERE id = :id")
    suspend fun tambahStok(
        id: Long,
        qty: Int,
        now: Long = System.currentTimeMillis()
    )

    @Delete
    suspend fun deleteBarang(barang: Barang)

    @Query("DELETE FROM barang WHERE id = :id")
    suspend fun deleteBarangById(id: Long)

    @Query("DELETE FROM barang")
    suspend fun deleteAllBarang()
}