package com.example.phoenixmobile.ui.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.phoenixmobile.databinding.FragmentMyDeviceBinding

class MyDeviceFragment : Fragment() {

    private var _binding: FragmentMyDeviceBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val myDeviceViewModel =
                ViewModelProvider(this).get(MyDeviceViewModel::class.java)

        _binding = FragmentMyDeviceBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.deviceName
        myDeviceViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}