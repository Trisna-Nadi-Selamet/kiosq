package com.kiosq.ui.statistik

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kiosq.databinding.FragmentStatistikBinding
import com.kiosq.util.CurrencyFormatter

class StatistikFragment : Fragment() {

    private var _binding: FragmentStatistikBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatistikViewModel by viewModels()
    private lateinit var stokRendahAdapter: StokRendahAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatistikBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStokRendahList()
        observeData()
    }

    private fun setupStokRendahList() {
        stokRendahAdapter = StokRendahAdapter()
        binding.rvStokRendah.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = stokRendahAdapter
        }
    }

    private fun observeData() {
        viewModel.countBarang.observe(viewLifecycleOwner) {
            binding.tvJumlahBarang.text = it.toString()
        }

        viewModel.totalNilaiStok.observe(viewLifecycleOwner) {
            binding.tvNilaiStok.text = CurrencyFormatter.format(it ?: 0)
        }

        viewModel.totalPendapatan.observe(viewLifecycleOwner) {
            binding.tvTotalPendapatan.text = CurrencyFormatter.format(it ?: 0)
        }

        viewModel.pendapatanHariIni.observe(viewLifecycleOwner) {
            binding.tvPendapatanHariIni.text = CurrencyFormatter.format(it ?: 0)
        }

        viewModel.totalTransaksi.observe(viewLifecycleOwner) {
            binding.tvTotalTransaksi.text = it.toString()
        }

        viewModel.barangTerlaris.observe(viewLifecycleOwner) {
            binding.tvBarangTerlaris.text = it ?: "-"
        }

        viewModel.countStokRendah.observe(viewLifecycleOwner) { count ->
            binding.tvLabelStokRendah.text = "Stok Rendah ($count barang)"
            binding.cardStokRendah.visibility = if (count > 0) View.VISIBLE else View.GONE
        }

        viewModel.stokRendah.observe(viewLifecycleOwner) { list ->
            stokRendahAdapter.submitList(list)
        }

        viewModel.top5Terlaris.observe(viewLifecycleOwner) { list ->
            setupBarChart(list)
        }
    }

    private fun setupBarChart(data: List<Pair<String, Int>>) {
        if (data.isEmpty()) {
            binding.layoutChart.visibility = View.GONE
            return
        }
        binding.layoutChart.visibility = View.VISIBLE
        binding.chartContainer.removeAllViews()

        val maxVal = data.maxOfOrNull { it.second } ?: 1
        val ctx = requireContext()

        data.forEachIndexed { idx, (nama, total) ->
            val row = layoutInflater.inflate(com.kiosq.R.layout.item_chart_bar, binding.chartContainer, false)
            val tvNama = row.findViewById<android.widget.TextView>(com.kiosq.R.id.tvBarName)
            val tvVal = row.findViewById<android.widget.TextView>(com.kiosq.R.id.tvBarValue)
            val bar = row.findViewById<View>(com.kiosq.R.id.viewBar)

            tvNama.text = "${idx + 1}. $nama"
            tvVal.text = "$total terjual"

            val pct = (total.toFloat() / maxVal.toFloat())
            val params = bar.layoutParams
            val maxWidth = resources.displayMetrics.widthPixels - 200
            params.width = (maxWidth * pct).toInt().coerceAtLeast(40)
            bar.layoutParams = params

            binding.chartContainer.addView(row)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
