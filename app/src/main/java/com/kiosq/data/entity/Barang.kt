package com.kiosq.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Satuan {
    PCS, SACHET, BUNGKUS, BOTOL, BIJI, BUAH, KG, GRAM, ONS, DUS, BAL, PAK, LUSIN
}

@Entity(tableName = "barang")
data class Barang(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nama: String,
    val kategori: String,
    val jumlah: Int,
    val satuan: String,       // stored as string from Satuan enum
    val hargaJual: Long,
    val hargaModal: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val profit: Long get() = hargaJual - hargaModal
    val profitPersen: Double get() = if (hargaModal > 0) (profit.toDouble() / hargaModal.toDouble()) * 100.0 else 0.0
    val nilaiStok: Long get() = jumlah * hargaModal
    val nilaiJual: Long get() = jumlah * hargaJual
}
