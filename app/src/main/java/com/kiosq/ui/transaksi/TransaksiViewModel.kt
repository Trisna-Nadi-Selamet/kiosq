package com.kiosq.ui.transaksi

import android.app.Application
import androidx.lifecycle.*
import com.kiosq.data.database.KiosQDatabase
import com.kiosq.data.entity.Barang
import com.kiosq.data.entity.JenisTransaksi
import com.kiosq.data.entity.Transaksi
import com.kiosq.data.repository.BarangRepository
import com.kiosq.data.repository.TransaksiRepository
import com.kiosq.util.FileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class TransaksiViewModel(application: Application) : AndroidViewModel(application) {

    private val barangRepo: BarangRepository
    private val transaksiRepo: TransaksiRepository

    val allBarang: LiveData<List<Barang>>
    val allTransaksi: LiveData<List<Transaksi>>
    val totalPendapatan: LiveData<Long?>
    val totalTransaksi: LiveData<Int>
    val barangTerlaris: LiveData<String?>

    private val _operationResult = MutableLiveData<String?>()
    val operationResult: LiveData<String?> = _operationResult

    private val _exportFile = MutableLiveData<File?>()
    val exportFile: LiveData<File?> = _exportFile

    // Cart for multi-item transaction
    private val _cart = MutableLiveData<List<CartItem>>(emptyList())
    val cart: LiveData<List<CartItem>> = _cart

    val cartTotal: LiveData<Long> = _cart.map { items ->
        items.sumOf { it.subtotal }
    }

    init {
        val db = KiosQDatabase.getInstance(application)
        barangRepo = BarangRepository(db.barangDao())
        transaksiRepo = TransaksiRepository(db.transaksiDao())

        allBarang = barangRepo.allBarang
        allTransaksi = transaksiRepo.allTransaksi
        totalPendapatan = transaksiRepo.totalPendapatan
        totalTransaksi = transaksiRepo.totalTransaksiJual
        barangTerlaris = transaksiRepo.barangTerlaris
    }

    fun addToCart(barang: Barang, qty: Int, harga: Long) {
        val current = _cart.value?.toMutableList() ?: mutableListOf()
        val existing = current.indexOfFirst { it.barang.id == barang.id }
        if (existing >= 0) {
            current[existing] = current[existing].copy(
                qty = current[existing].qty + qty
            )
        } else {
            current.add(CartItem(barang, qty, harga))
        }
        _cart.value = current
    }

    fun removeFromCart(barangId: Long) {
        _cart.value = _cart.value?.filter { it.barang.id != barangId }
    }

    fun clearCart() { _cart.value = emptyList() }

    fun prosesJual() = viewModelScope.launch {
        val items = _cart.value ?: return@launch
        if (items.isEmpty()) {
            _operationResult.postValue("Keranjang kosong")
            return@launch
        }

        var success = true
        items.forEach { item ->
            val barang = barangRepo.getBarangById(item.barang.id)
            if (barang == null) {
                _operationResult.postValue("Barang ${item.barang.nama} tidak ditemukan")
                success = false
                return@forEach
            }
            if (barang.jumlah < item.qty) {
                _operationResult.postValue("Stok ${barang.nama} tidak cukup (tersisa ${barang.jumlah})")
                success = false
                return@forEach
            }
        }

        if (!success) return@launch

        items.forEach { item ->
            barangRepo.kurangiStok(item.barang.id, item.qty)
            transaksiRepo.insertTransaksi(
                Transaksi(
                    barangId = item.barang.id,
                    namaBarang = item.barang.nama,
                    jenis = JenisTransaksi.JUAL.name,
                    jumlah = item.qty,
                    hargaSatuan = item.harga,
                    total = item.subtotal,
                    catatan = ""
                )
            )
        }
        clearCart()
        _operationResult.postValue("Transaksi berhasil! Total: ${items.sumOf { it.subtotal }}")
    }

    fun prosesBeli(barang: Barang, qty: Int, hargaBeli: Long, catatan: String = "") =
        viewModelScope.launch {
            barangRepo.tambahStok(barang.id, qty)
            transaksiRepo.insertTransaksi(
                Transaksi(
                    barangId = barang.id,
                    namaBarang = barang.nama,
                    jenis = JenisTransaksi.BELI.name,
                    jumlah = qty,
                    hargaSatuan = hargaBeli,
                    total = qty * hargaBeli,
                    catatan = catatan
                )
            )
            _operationResult.postValue("Stok ${barang.nama} ditambah $qty")
        }

    fun exportCsv() = viewModelScope.launch(Dispatchers.IO) {
        val list = transaksiRepo.getAllTransaksiList()
        FileHelper.exportTransaksiCsv(getApplication(), list)
            .onSuccess { file ->
                _exportFile.postValue(file)
                _operationResult.postValue("Export berhasil: ${file.name}")
            }
            .onFailure { _operationResult.postValue("Export gagal: ${it.message}") }
    }

    fun clearResult() { _operationResult.value = null }
    fun clearExportFile() { _exportFile.value = null }
}

data class CartItem(
    val barang: Barang,
    val qty: Int,
    val harga: Long
) {
    val subtotal: Long get() = qty * harga
}
