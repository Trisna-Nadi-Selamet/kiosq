package com.kiosq.ui.barang

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kiosq.R
import com.kiosq.data.entity.Barang
import com.kiosq.databinding.ItemBarangBinding
import com.kiosq.util.CurrencyFormatter

class BarangAdapter(
    private val onEdit: (Barang) -> Unit,
    private val onDelete: (Barang) -> Unit,
    private val onTambahStok: (Barang) -> Unit
) : ListAdapter<Barang, BarangAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Barang>() {
        override fun areItemsTheSame(a: Barang, b: Barang) = a.id == b.id
        override fun areContentsTheSame(a: Barang, b: Barang) = a == b
    }

    inner class ViewHolder(private val binding: ItemBarangBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(barang: Barang) {
            binding.apply {
                tvNama.text = barang.nama
                tvKategori.text = barang.kategori
                tvStok.text = "${barang.jumlah} ${barang.satuan}"
                tvHargaJual.text = CurrencyFormatter.format(barang.hargaJual)
                tvHargaModal.text = "Modal: ${CurrencyFormatter.format(barang.hargaModal)}"
                tvProfit.text = "Profit: ${CurrencyFormatter.format(barang.profit)} (${
                    String.format("%.0f", barang.profitPersen)
                }%)"

                // Warna stok rendah
                val ctx = root.context
                if (barang.jumlah <= 5) {
                    tvStok.setTextColor(ContextCompat.getColor(ctx, R.color.red_500))
                    chipStokRendah.visibility = android.view.View.VISIBLE
                } else {
                    tvStok.setTextColor(ContextCompat.getColor(ctx, R.color.green_600))
                    chipStokRendah.visibility = android.view.View.GONE
                }

                btnEdit.setOnClickListener { onEdit(barang) }
                btnDelete.setOnClickListener { onDelete(barang) }
                btnTambahStok.setOnClickListener { onTambahStok(barang) }
                root.setOnClickListener { onEdit(barang) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBarangBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
