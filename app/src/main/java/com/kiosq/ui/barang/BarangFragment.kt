package com.kiosq.ui.barang

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.kiosq.R
import com.kiosq.data.entity.Barang
import com.kiosq.databinding.FragmentBarangBinding
import com.kiosq.util.FileHelper

class BarangFragment : Fragment() {

    private var _binding: FragmentBarangBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BarangViewModel by viewModels()
    private lateinit var adapter: BarangAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBarangBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupFab()
        setupMenu()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = BarangAdapter(
            onEdit = { barang -> showBarangDialog(barang) },
            onDelete = { barang -> confirmDelete(barang) },
            onTambahStok = { barang -> showTambahStokDialog(barang) }
        )
        binding.rvBarang.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@BarangFragment.adapter
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.search(newText ?: "")
                return true
            }
        })
    }

    private fun setupFab() {
        binding.fabTambah.setOnClickListener { showBarangDialog(null) }
    }

    private fun setupMenu() {
        binding.toolbar.inflateMenu(R.menu.menu_barang)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_export -> { viewModel.exportCsv(); true }
                R.id.action_import -> { viewModel.importCsv { }; true }
                R.id.action_filter -> { showKategoriFilter(); true }
                else -> false
            }
        }
    }

    private fun observeData() {
        viewModel.filteredBarang.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.tvJumlahBarang.text = "${list.size} barang"
        }

        viewModel.allKategori.observe(viewLifecycleOwner) { kategori ->
            setupKategoriChips(kategori)
        }

        viewModel.countStokRendah.observe(viewLifecycleOwner) { count ->
            binding.badgeStokRendah.visibility = if (count > 0) View.VISIBLE else View.GONE
            binding.badgeStokRendah.text = count.toString()
        }

        viewModel.operationResult.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearResult()
            }
        }

        viewModel.exportFile.observe(viewLifecycleOwner) { file ->
            file?.let {
                val shareIntent = FileHelper.shareFile(requireContext(), it, "text/csv")
                startActivity(android.content.Intent.createChooser(shareIntent, "Bagikan CSV"))
                viewModel.clearExportFile()
            }
        }
    }

    private fun setupKategoriChips(kategoriList: List<String>) {
        binding.chipGroupKategori.removeAllViews()

        val chipSemua = Chip(requireContext()).apply {
            text = "Semua"
            isCheckable = true
            isChecked = true
            setOnClickListener { viewModel.filterByKategori(null) }
        }
        binding.chipGroupKategori.addView(chipSemua)

        kategoriList.forEach { kategori ->
            val chip = Chip(requireContext()).apply {
                text = kategori
                isCheckable = true
                setOnClickListener { viewModel.filterByKategori(kategori) }
            }
            binding.chipGroupKategori.addView(chip)
        }
    }

    private fun showKategoriFilter() {
        val items = viewModel.allKategori.value?.toTypedArray() ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filter Kategori")
            .setItems(items) { _, idx -> viewModel.filterByKategori(items[idx]) }
            .setNeutralButton("Semua") { _, _ -> viewModel.filterByKategori(null) }
            .show()
    }

    private fun showBarangDialog(barang: Barang?) {
        val dialog = BarangDialogFragment.newInstance(barang)
        dialog.onSave = { newBarang ->
            if (barang == null) viewModel.insertBarang(newBarang)
            else viewModel.updateBarang(newBarang)
        }
        dialog.show(childFragmentManager, "BarangDialog")
    }

    private fun confirmDelete(barang: Barang) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Barang")
            .setMessage("Hapus \"${barang.nama}\"?")
            .setPositiveButton("Hapus") { _, _ -> viewModel.deleteBarang(barang) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showTambahStokDialog(barang: Barang) {
        val dialog = TambahStokDialogFragment.newInstance(barang)
        dialog.show(childFragmentManager, "TambahStok")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
