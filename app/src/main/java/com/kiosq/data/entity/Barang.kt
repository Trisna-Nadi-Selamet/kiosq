@Entity(tableName = "barang")
data class Barang(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nama: String,
    val kategori: String,
    val jumlah: Int,
    val satuan: Satuan,
    val hargaJual: Long,
    val hargaModal: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val profit: Long get() = hargaJual - hargaModal
    val profitPersen: Double get() =
        if (hargaModal > 0) (profit.toDouble() / hargaModal.toDouble()) * 100 else 0.0

    val nilaiStok: Long get() = jumlah * hargaModal
    val nilaiJual: Long get() = jumlah * hargaJual
}