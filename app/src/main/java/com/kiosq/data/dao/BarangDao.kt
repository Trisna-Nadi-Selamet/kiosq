package com.kiosq.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kiosq.data.entity.Barang

@Dao
interface BarangDao {

    @Query("SELECT * FROM barang ORDER BY nama ASC")
    fun getAllBarang(): LiveData<List<Barang>>

    @Query("SELECT * FROM barang WHERE id = :id")
    suspend fun getBarangById(id: Long): Barang?

    @Query("SELECT DISTINCT kategori FROM barang")
    fun getAllKategori(): LiveData<List<String>>

    @Query("SELECT COALESCE(SUM(jumlah * hargaModal), 0) FROM barang")
    fun getTotalNilaiStok(): LiveData<Long>

    @Query("SELECT * FROM barang WHERE jumlah <= :min")
    fun getStokRendah(min: Int = 10): LiveData<List<Barang>>

    @Query("SELECT COUNT(*) FROM barang WHERE jumlah <= :min")
    fun countStokRendah(min: Int = 10): LiveData<Int>

    @Insert
    suspend fun insertBarang(barang: Barang): Long

    @Update
    suspend fun updateBarang(barang: Barang)

    @Delete
    suspend fun deleteBarang(barang: Barang)

    @Query("DELETE FROM barang")
    suspend fun deleteAllBarang()
}