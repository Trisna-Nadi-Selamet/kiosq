package com.kiosq.ui.transaksi

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.kiosq.R
import com.kiosq.data.entity.Barang
import com.kiosq.databinding.FragmentTransaksiBinding
import com.kiosq.util.CurrencyFormatter
import com.kiosq.util.FileHelper

class TransaksiFragment : Fragment() {

    private var _binding: FragmentTransaksiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransaksiViewModel by viewModels()
    private lateinit var transaksiAdapter: TransaksiAdapter
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransaksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        setupCart()
        setupTransaksiList()
        setupMenu()
        observeData()
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Jual"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Riwayat"))
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showJualPanel()
                    1 -> showRiwayatPanel()
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun showJualPanel() {
        binding.layoutJual.visibility = View.VISIBLE
        binding.layoutRiwayat.visibility = View.GONE
    }

    private fun showRiwayatPanel() {
        binding.layoutJual.visibility = View.GONE
        binding.layoutRiwayat.visibility = View.VISIBLE
    }

    private fun setupCart() {
        cartAdapter = CartAdapter(
            onRemove = { item -> viewModel.removeFromCart(item.barang.id) }
        )
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }

        binding.btnTambahBarang.setOnClickListener { showPilihBarangDialog() }
        binding.btnProses.setOnClickListener { viewModel.prosesJual() }
        binding.btnClearCart.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Kosongkan Keranjang?")
                .setPositiveButton("Ya") { _, _ -> viewModel.clearCart() }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun setupTransaksiList() {
        transaksiAdapter = TransaksiAdapter()
        binding.rvTransaksi.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transaksiAdapter
        }
    }

    private fun setupMenu() {
        binding.toolbar.inflateMenu(R.menu.menu_transaksi)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_export_transaksi -> { viewModel.exportCsv(); true }
                else -> false
            }
        }
    }

    private fun observeData() {
        viewModel.cart.observe(viewLifecycleOwner) { items ->
            cartAdapter.submitList(items)
            binding.tvCartEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.btnProses.isEnabled = items.isNotEmpty()
            binding.btnClearCart.isEnabled = items.isNotEmpty()
        }

        viewModel.cartTotal.observe(viewLifecycleOwner) { total ->
            binding.tvTotal.text = "Total: ${CurrencyFormatter.format(total)}"
        }

        viewModel.allTransaksi.observe(viewLifecycleOwner) { list ->
            transaksiAdapter.submitList(list)
            binding.tvRiwayatEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.totalPendapatan.observe(viewLifecycleOwner) { total ->
            binding.tvTotalPendapatan.text = "Total Pendapatan: ${CurrencyFormatter.format(total ?: 0)}"
        }

        viewModel.operationResult.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearResult()
            }
        }

        viewModel.exportFile.observe(viewLifecycleOwner) { file ->
            file?.let {
                val intent = FileHelper.shareFile(requireContext(), it, "text/csv")
                startActivity(android.content.Intent.createChooser(intent, "Bagikan CSV Transaksi"))
                viewModel.clearExportFile()
            }
        }
    }

    private fun showPilihBarangDialog() {
        val barangList = viewModel.allBarang.value ?: emptyList()
        if (barangList.isEmpty()) {
            Snackbar.make(binding.root, "Belum ada barang. Tambah barang terlebih dahulu.", Snackbar.LENGTH_SHORT).show()
            return
        }
        val namaList = barangList.map { "${it.nama} (Stok: ${it.jumlah} ${it.satuan})" }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Pilih Barang")
            .setItems(namaList) { _, idx ->
                showInputQtyDialog(barangList[idx])
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showInputQtyDialog(barang: Barang) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input_qty, null)
        val etQty = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etQty)
        val tvInfo = dialogView.findViewById<android.widget.TextView>(R.id.tvBarangInfo)
        tvInfo.text = "${barang.nama}\nHarga: ${CurrencyFormatter.format(barang.hargaJual)}\nStok: ${barang.jumlah} ${barang.satuan}"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Jumlah")
            .setView(dialogView)
            .setPositiveButton("Tambah") { _, _ ->
                val qty = etQty.text.toString().toIntOrNull() ?: 1
                if (qty > barang.jumlah) {
                    Snackbar.make(binding.root, "Stok tidak cukup!", Snackbar.LENGTH_SHORT).show()
                } else {
                    viewModel.addToCart(barang, qty, barang.hargaJual)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
