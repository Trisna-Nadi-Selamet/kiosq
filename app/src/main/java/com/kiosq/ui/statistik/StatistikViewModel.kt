package com.kiosq.ui.statistik

import android.app.Application
import androidx.lifecycle.*
import com.kiosq.data.database.KiosQDatabase
import com.kiosq.data.entity.Barang
import com.kiosq.data.entity.Transaksi
import com.kiosq.data.repository.BarangRepository
import com.kiosq.data.repository.TransaksiRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class StatistikViewModel(application: Application) : AndroidViewModel(application) {

    private val barangRepo: BarangRepository
    private val transaksiRepo: TransaksiRepository

    val countBarang: LiveData<Int>
    val totalNilaiStok: LiveData<Long>
    val totalPendapatan: LiveData<Long>
    val totalTransaksi: LiveData<Int>
    val barangTerlaris: LiveData<String>
    val stokRendah: LiveData<List<Barang>>
    val countStokRendah: LiveData<Int>

    // Pendapatan hari ini
    val pendapatanHariIni: LiveData<Long>

    private val _top5Terlaris = MutableLiveData<List<Pair<String, Int>>>()
    val top5Terlaris: LiveData<List<Pair<String, Int>>> = _top5Terlaris

    init {
        val db = KiosQDatabase.getInstance(application)
        barangRepo = BarangRepository(db.barangDao())
        transaksiRepo = TransaksiRepository(db.transaksiDao())

        countBarang = barangRepo.countBarang
        totalNilaiStok = barangRepo.totalNilaiStok
        totalPendapatan = transaksiRepo.totalPendapatan
        totalTransaksi = transaksiRepo.totalTransaksiJual
        barangTerlaris = transaksiRepo.barangTerlaris
        stokRendah = barangRepo.stokRendah
        countStokRendah = barangRepo.countStokRendah

        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        pendapatanHariIni = transaksiRepo.getTotalPendapatanSince(todayStart)

        loadTop5()
    }

    fun loadTop5() = viewModelScope.launch {
        val list = transaksiRepo.getTop5Terlaris()
        _top5Terlaris.postValue(list.map { it.namaBarang to it.totalJual })
    }
}
