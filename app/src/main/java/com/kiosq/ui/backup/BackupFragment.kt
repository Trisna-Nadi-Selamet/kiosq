package com.kiosq.ui.backup

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.kiosq.databinding.FragmentBackupBinding
import com.kiosq.util.FileHelper

class BackupFragment : Fragment() {

    private var _binding: FragmentBackupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BackupViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBackupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
        observeData()
    }

    private fun setupButtons() {
        binding.btnBackup.setOnClickListener { viewModel.backupData() }

        binding.btnRestore.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Restore Data")
                .setMessage("Semua data saat ini akan diganti dengan data backup. Lanjutkan?")
                .setPositiveButton("Restore") { _, _ -> viewModel.restoreData() }
                .setNegativeButton("Batal", null)
                .show()
        }

        binding.btnExportBarang.setOnClickListener { viewModel.exportBarangCsv() }
        binding.btnExportTransaksi.setOnClickListener { viewModel.exportTransaksiCsv() }
        binding.btnImportBarang.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Import Barang dari CSV")
                .setMessage("Pastikan file 'kiosq_barang.csv' sudah ada di folder Download/KiosQ. Import akan menambahkan data baru (tidak menghapus yang ada).")
                .setPositiveButton("Import") { _, _ -> viewModel.importBarangCsv() }
                .setNegativeButton("Batal", null)
                .show()
        }
        binding.btnShareBackup.setOnClickListener { viewModel.shareBackupFile() }
        binding.btnRefresh.setOnClickListener { viewModel.refreshFileInfo() }
    }

    private fun observeData() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnBackup.isEnabled = !loading
            binding.btnRestore.isEnabled = !loading
            binding.btnExportBarang.isEnabled = !loading
            binding.btnExportTransaksi.isEnabled = !loading
        }

        viewModel.operationResult.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearResult()
            }
        }

        viewModel.backupInfo.observe(viewLifecycleOwner) { info ->
            if (info != null) {
                binding.tvBackupInfo.text = "📁 backup.json\nUkuran: ${info.ukuran}\nTerakhir: ${info.tanggal}"
                binding.tvBackupInfo.visibility = View.VISIBLE
                binding.tvBackupEmpty.visibility = View.GONE
            } else {
                binding.tvBackupInfo.visibility = View.GONE
                binding.tvBackupEmpty.visibility = View.VISIBLE
            }
        }

        viewModel.csvBarangInfo.observe(viewLifecycleOwner) { info ->
            binding.tvCsvBarangInfo.text = if (info != null) {
                "📄 ${info.nama} (${info.ukuran})\n${info.tanggal}"
            } else {
                "Belum ada file"
            }
        }

        viewModel.csvTransaksiInfo.observe(viewLifecycleOwner) { info ->
            binding.tvCsvTransaksiInfo.text = if (info != null) {
                "📄 ${info.nama} (${info.ukuran})\n${info.tanggal}"
            } else {
                "Belum ada file"
            }
        }

        viewModel.shareFile.observe(viewLifecycleOwner) { pair ->
            pair?.let { (file, mime) ->
                val intent = FileHelper.shareFile(requireContext(), file, mime)
                startActivity(android.content.Intent.createChooser(intent, "Bagikan file via..."))
                viewModel.clearShareFile()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
