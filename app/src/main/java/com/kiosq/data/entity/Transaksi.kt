package com.kiosq.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class JenisTransaksi {
    JUAL, BELI, KOREKSI
}

@Entity(tableName = "transaksi")
data class Transaksi(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val barangId: Long,
    val namaBarang: String,      // snapshot saat transaksi
    val jenis: String,           // JUAL / BELI / KOREKSI
    val jumlah: Int,
    val hargaSatuan: Long,
    val total: Long,
    val catatan: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class TransaksiWithBarang(
    val transaksi: Transaksi,
    val barang: Barang
)

data class StatistikHarian(
    val tanggal: String,
    val totalPenjualan: Long,
    val totalTransaksi: Int,
    val totalProfit: Long
)

data class RingkasanStatistik(
    val totalPendapatan: Long,
    val totalModal: Long,
    val totalProfit: Long,
    val jumlahTransaksi: Int,
    val barangTerlaris: String,
    val stokRendah: Int
)
