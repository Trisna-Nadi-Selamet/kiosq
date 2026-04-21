package com.kiosq.ui.barang

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.kiosq.data.entity.Barang
import com.kiosq.data.entity.Satuan
import com.kiosq.databinding.DialogBarangBinding
import com.kiosq.util.CurrencyFormatter

class BarangDialogFragment : DialogFragment() {

    private var _binding: DialogBarangBinding? = null
    private val binding get() = _binding!!

    var onSave: ((Barang) -> Unit)? = null

    private var existingBarang: Barang? = null

    companion object {
        private const val ARG_BARANG_ID = "barang_id"
        private const val ARG_NAMA = "nama"
        private const val ARG_KATEGORI = "kategori"
        private const val ARG_JUMLAH = "jumlah"
        private const val ARG_SATUAN = "satuan"
        private const val ARG_HARGA_JUAL = "harga_jual"
        private const val ARG_HARGA_MODAL = "harga_modal"

        fun newInstance(barang: Barang): BarangDialogFragment {
            return BarangDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_BARANG_ID, barang.id)
                    putString(ARG_NAMA, barang.nama)
                    putString(ARG_KATEGORI, barang.kategori)
                    putInt(ARG_JUMLAH, barang.jumlah)
                    putString(ARG_SATUAN, barang.satuan)
                    putLong(ARG_HARGA_JUAL, barang.hargaJual)
                    putLong(ARG_HARGA_MODAL, barang.hargaModal)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogBarangBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FIX: lebih aman enum → string
        val satuanList = Satuan.values().map { it.name.lowercase() }

        val satuanAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            satuanList
        )

        satuanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSatuan.adapter = satuanAdapter

        val args = arguments

        if (args != null) {

            existingBarang = Barang(
                id = args.getLong(ARG_BARANG_ID),
                nama = args.getString(ARG_NAMA, ""),
                kategori = args.getString(ARG_KATEGORI, ""),
                jumlah = args.getInt(ARG_JUMLAH, 0),
                satuan = args.getString(ARG_SATUAN, "pcs"),
                hargaJual = args.getLong(ARG_HARGA_JUAL, 0),
                hargaModal = args.getLong(ARG_HARGA_MODAL, 0)
            )

            binding.tvDialogTitle.text = "Edit Barang"

            // FIX null safety aman
            binding.etNama.setText(existingBarang?.nama.orEmpty())
            binding.etKategori.setText(existingBarang?.kategori.orEmpty())
            binding.etJumlah.setText(existingBarang?.jumlah?.toString() ?: "0")
            binding.etHargaJual.setText(existingBarang?.hargaJual?.toString() ?: "0")
            binding.etHargaModal.setText(existingBarang?.hargaModal?.toString() ?: "0")

            val idx = satuanList.indexOf(
                existingBarang?.satuan?.lowercase() ?: ""
            )

            if (idx >= 0) binding.spinnerSatuan.setSelection(idx)

        } else {
            binding.tvDialogTitle.text = "Tambah Barang"
        }

        binding.btnSimpan.setOnClickListener { saveBarang() }
        binding.btnBatal.setOnClickListener { dismiss() }
    }

    private fun saveBarang() {

        val nama = binding.etNama.text.toString().trim()
        val kategori = binding.etKategori.text.toString().trim()
        val jumlahStr = binding.etJumlah.text.toString().trim()
        val hargaJualStr = binding.etHargaJual.text.toString().trim()
        val hargaModalStr = binding.etHargaModal.text.toString().trim()
        val satuan = binding.spinnerSatuan.selectedItem.toString()

        if (nama.isBlank()) { binding.etNama.error = "Nama wajib diisi"; return }
        if (kategori.isBlank()) { binding.etKategori.error = "Kategori wajib diisi"; return }
        if (jumlahStr.isBlank()) { binding.etJumlah.error = "Jumlah wajib diisi"; return }
        if (hargaJualStr.isBlank()) { binding.etHargaJual.error = "Harga jual wajib diisi"; return }
        if (hargaModalStr.isBlank()) { binding.etHargaModal.error = "Harga modal wajib diisi"; return }

        val barang = Barang(
            id = existingBarang?.id ?: 0,
            nama = nama,
            kategori = kategori,
            jumlah = jumlahStr.toIntOrNull() ?: 0,
            satuan = satuan,
            hargaJual = hargaJualStr.toLongOrNull() ?: 0,
            hargaModal = hargaModalStr.toLongOrNull() ?: 0
        )

        onSave?.invoke(barang)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}