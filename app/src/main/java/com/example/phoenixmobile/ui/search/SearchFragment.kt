package com.example.phoenixmobile.ui.search

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.phoenixmobile.databinding.FragmentSearchPageBinding
import com.example.phoenixmobile.model.Graphic
import com.example.phoenixmobile.model.States
import com.example.phoenixmobile.ui.search.adapter.PriceAdapter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.TreeMap
import kotlin.math.roundToInt


class SearchFragment : Fragment() {
    private val viewModel: SearchViewModel by activityViewModels()
    private var _binding: FragmentSearchPageBinding? = null
    private var nameFilter: String = ""
    private var venderFilter: String = ""
    private var priceList: TreeMap<String, Double> = TreeMap()
    private lateinit var adapter: PriceAdapter
    private val listener = PriceAdapter.OnClickListener { id ->
        setDataChart(id)
    }


    private val binding get() = _binding!!

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchPageBinding.inflate(inflater, container, false)
        val root: View = binding.root

        adapter = PriceAdapter(listener, priceList)
        binding.deviceCondPriceList.adapter = adapter
        /*binding.btnGetPrice.setOnClickListener {
            Snackbar.make(
                binding.root,
                "Update price information...",
                Snackbar.ANIMATION_MODE_SLIDE
            )
                .show()
        }*/

        viewModel.getPriceList().observe(viewLifecycleOwner) { data ->
            priceList = data
            binding.progressBar.visibility = View.INVISIBLE
            adapter.updateList(priceList)
        }

        binding.productName.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                nameFilter = s.toString()
                filter()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        binding.venderName.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                venderFilter = s.toString()
                filter()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
        return root
    }

    private fun setDataChart(id: Int) {
        binding.priceListContainer.visibility = View.INVISIBLE
        binding.charContainer.visibility = View.VISIBLE

        binding.backButton.setOnClickListener {
            binding.priceListContainer.visibility = View.VISIBLE
            binding.charContainer.visibility = View.INVISIBLE
        }

        viewModel.getGraphicsData(id).observe(viewLifecycleOwner) {
            setDataGraphic(it)
        }
    }

    private fun setDataGraphic(data: Graphic) {
        // calculating the maximum and minimum for plotting prices
        var STEP = 20
        var max = 0
        var min = 100000
        for (state in data.prices) {
            if (max < state.priceList.maxOf { it }.roundToInt())
                max = state.priceList.maxOf { it }.roundToInt()
            println(max)
            if (min > state.priceList.minOf { it }.roundToInt())
                min = state.priceList.minOf { it }.roundToInt() - 40
        }
        val size = (max - min) / STEP + 1
        val count = ArrayList<Int>(size)
        for (i in 0..size)
            count.add(0)

        // filling in data on possible conditions : excellent, good and fair
        val datasets = ArrayList<LineDataSet>();
        for (state in data.prices) {
            val data =
                LineDataSet(setDataFromStates(state, count, STEP, size, min, max), state.state)
            when (state.state) {
                "excellent" -> data.color = Color.BLUE
                "good" -> data.color = Color.MAGENTA
                else -> data.color = Color.CYAN
            }
            data.lineWidth = 2f
            data.setDrawCircles(false)
            data.formLineWidth = 1f
            data.formSize = 15f
            data.valueTextSize = 5f
            datasets.add(data)
        }
        val data = LineData(datasets[0], datasets[1], datasets[2])
        binding.chart.data = data
        setChartSettings(binding.chart, STEP, size, min, max)
    }

    private fun setDataFromStates(
        state: States,
        count: ArrayList<Int>,
        step: Int,
        size: Int,
        min: Int,
        max: Int
    ): ArrayList<Entry> {

        // counting the number of instances in the interval
        for (item in state.priceList) {
            count[((item - min) / step).roundToInt()]++;
        }
        val entries = ArrayList<Entry>()
        // building entities for graphs
        for (i in 0..size) {
            entries.add(Entry(i.toFloat(), count[i].toFloat()))
        }
        return entries
    }

    private fun setChartSettings(
        chart: LineChart, step: Int, size: Int, min: Int, max: Int
    ) {
        val axiss = ArrayList<String>()
        for (i in 0..size)
            axiss.add((i * step + min).toString())

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawLabels(true)
        xAxis.valueFormatter = IndexAxisValueFormatter(axiss)
        /*
                val yAxis = chart.axisLeft
                yAxis.setDrawLabels(true)

                xAxis.setDrawGridLines(true)
                yAxis.setDrawGridLines(false)*/
        val legend = chart.legend
        legend.isEnabled = true
        chart.description.text = ""
        chart.setPinchZoom(false);
        chart.invalidate()
    }


    private fun filter() {
        // filtering by words in the search
        val temp = TreeMap<String, Double>()
        for (item in priceList.keys) {
            if (item.lowercase().contains(venderFilter.lowercase()) &&
                item.lowercase().contains(nameFilter.lowercase())
            ) {
                temp[item] = priceList[item]!!
            }
        }
        adapter.updateList(temp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}