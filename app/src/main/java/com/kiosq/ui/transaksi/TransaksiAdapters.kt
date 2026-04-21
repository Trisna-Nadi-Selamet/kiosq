package com.kiosq.ui.transaksi

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kiosq.data.entity.Transaksi
import com.kiosq.databinding.ItemCartBinding
import com.kiosq.databinding.ItemTransaksiBinding
import com.kiosq.util.CurrencyFormatter
import com.kiosq.util.DateFormatter

// ================= CART ADAPTER =================
class CartAdapter(
    private val onRemove: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.VH>(CartDiff) {

    companion object CartDiff : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(a: CartItem, b: CartItem) =
            a.barang.id == b.barang.id

        override fun areContentsTheSame(a: CartItem, b: CartItem) =
            a == b
    }

    inner class VH(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.tvNamaBarang.text = item.barang.nama
            binding.tvQty.text = "${item.qty} x ${CurrencyFormatter.format(item.harga)}"
            binding.tvSubtotal.text = CurrencyFormatter.format(item.subtotal)

            binding.btnHapus.setOnClickListener {
                onRemove(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}

// ================= TRANSAKSI ADAPTER =================
class TransaksiAdapter :
    ListAdapter<Transaksi, TransaksiAdapter.VH>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<Transaksi>() {
        override fun areItemsTheSame(a: Transaksi, b: Transaksi) =
            a.id == b.id

        override fun areContentsTheSame(a: Transaksi, b: Transaksi) =
            a == b
    }

    inner class VH(private val binding: ItemTransaksiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(t: Transaksi) {

            binding.tvNamaBarang.text = t.namaBarang
            binding.tvJenis.text = t.jenis
            binding.tvJumlah.text = "x${t.jumlah}"
            binding.tvTotal.text = CurrencyFormatter.format(t.total)
            binding.tvTanggal.text = DateFormatter.formatFull(t.createdAt)
            binding.tvCatatan.text =
                if (t.catatan.isNotBlank()) t.catatan else "-"

            val color = when (t.jenis) {
                "JUAL" -> Color.parseColor("#388E3C")
                "BELI" -> Color.parseColor("#1565C0")
                else -> Color.parseColor("#F57F17")
            }

            binding.tvJenis.setTextColor(color)
            binding.chipJenis.text = t.jenis
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTransaksiBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}