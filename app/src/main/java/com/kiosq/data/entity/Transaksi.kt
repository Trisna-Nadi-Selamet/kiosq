package com.kiosq.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.TypeConverter
import androidx.room.TypeConverters

// =========================
// ENUM
// =========================
enum class JenisTransaksi {
    JUAL, BELI, KOREKSI
}

// =========================
// ENTITY TRANSAKSI
// =========================
@Entity(tableName = "transaksi")
data class Transaksi(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val barangId: Long,
    val namaBarang: String,

    val jenis: JenisTransaksi,

    val jumlah: Int,
    val hargaSatuan: Long,
    val total: Long,
    val catatan: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// =========================
// RELATION MODEL
// =========================
data class TransaksiWithBarang(

    @Embedded
    val transaksi: Transaksi,

    @Relation(
        parentColumn = "barangId",
        entityColumn = "id"
    )
    val barang: Barang
)

// =========================
// TYPE CONVERTER (ENUM)
// =========================
class TransaksiConverter {

    @TypeConverter
    fun fromJenis(value: JenisTransaksi): String {
        return value.name
    }

    @TypeConverter
    fun toJenis(value: String): JenisTransaksi {
        return JenisTransaksi.valueOf(value)
    }
}