package com.kiosq.ui.barang

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.kiosq.data.entity.Barang
import com.kiosq.databinding.DialogTambahStokBinding

class TambahStokDialogFragment : DialogFragment() {

    private var _binding: DialogTambahStokBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BarangViewModel by viewModels({ requireParentFragment() })

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_NAMA = "nama"
        private const val ARG_STOK = "stok"
        private const val ARG_SATUAN = "satuan"

        fun newInstance(barang: Barang) = TambahStokDialogFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_ID, barang.id)
                putString(ARG_NAMA, barang.nama)
                putInt(ARG_STOK, barang.jumlah)
                putString(ARG_SATUAN, barang.satuan)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTambahStokBinding.inflate(inflater, container, false)
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

        val args = arguments ?: return

        val id = args.getLong(ARG_ID)
        val nama = args.getString(ARG_NAMA).orEmpty()
        val stok = args.getInt(ARG_STOK)
        val satuan = args.getString(ARG_SATUAN).orEmpty()

        binding.tvNamaBarang.text = nama
        binding.tvStokSaatIni.text = "Stok saat ini: $stok $satuan"

        binding.btnSimpan.setOnClickListener {
            val qty = binding.etJumlahTambah.text.toString().toIntOrNull()

            if (qty == null || qty <= 0) {
                binding.etJumlahTambah.error = "Masukkan jumlah valid"
                return@setOnClickListener
            }

            val updated = Barang(
                id = id,
                nama = nama,
                kategori = "",
                jumlah = stok + qty,
                satuan = satuan,
                hargaJual = 0,
                hargaModal = 0
            )

            viewModel.updateBarang(updated)
            dismiss()
        }

        binding.btnBatal.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}