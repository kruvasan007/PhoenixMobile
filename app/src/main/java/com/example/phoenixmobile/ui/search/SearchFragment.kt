package com.example.phoenixmobile.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.phoenixmobile.databinding.FragmentSearchPageBinding
import com.example.phoenixmobile.ui.device.MyDeviceViewModel
import com.example.phoenixmobile.ui.search.adapter.PriceAdapter
import com.google.android.material.snackbar.Snackbar
import java.util.TreeMap

class SearchFragment : Fragment() {
    private val viewModel: MyDeviceViewModel by activityViewModels()
    private var _binding: FragmentSearchPageBinding? = null
    private var nameFilter: String = ""
    private var venderFilter: String = ""
    private var priceList: TreeMap<String, Double> = TreeMap()
    private lateinit var adapter: PriceAdapter;


    private val binding get() = _binding!!

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val searchViewModel =
            ViewModelProvider(this).get(SearchViewModel::class.java)
        _binding = FragmentSearchPageBinding.inflate(inflater, container, false)
        val root: View = binding.root

        adapter = PriceAdapter(priceList)
        binding.deviceCondPriceList.adapter = adapter
        binding.btnGetPrice.setOnClickListener {
            Snackbar.make(
                binding.root,
                "Update price information...",
                Snackbar.ANIMATION_MODE_SLIDE
            )
                .show()
        }

        searchViewModel.getPriceList().observe(viewLifecycleOwner) { data ->
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

    fun filter() {
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