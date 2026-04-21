package com.kiosq.ui.statistik

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kiosq.data.entity.Barang
import com.kiosq.databinding.ItemStokRendahBinding
import com.kiosq.util.CurrencyFormatter

class StokRendahAdapter : ListAdapter<Barang, StokRendahAdapter.VH>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<Barang>() {
        override fun areItemsTheSame(a: Barang, b: Barang) = a.id == b.id
        override fun areContentsTheSame(a: Barang, b: Barang) = a == b
    }

    inner class VH(private val b: ItemStokRendahBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(barang: Barang) {
            b.tvNama.text = barang.nama
            b.tvStok.text = "${barang.jumlah} ${barang.satuan}"
            b.tvHarga.text = CurrencyFormatter.format(barang.hargaJual)
            b.tvKategori.text = barang.kategori
            if (barang.jumlah == 0) {
                b.tvStok.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
                b.tvWarning.text = "HABIS"
            } else {
                b.tvStok.setTextColor(android.graphics.Color.parseColor("#F57F17"))
                b.tvWarning.text = "HAMPIR HABIS"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemStokRendahBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
