package com.example.phoenixmobile.ui.device

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.phoenixmobile.databinding.FragmentMyDeviceBinding


class MyDeviceFragment : Fragment() {
    private val REQUEST_PERMISSIONS = 1;

    private var _binding: FragmentMyDeviceBinding? = null
    private val viewModel: MyDeviceViewModel by activityViewModels()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyDeviceBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val phoneModel = Build.MODEL
        binding.deviceName.text = phoneModel
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE
            ),
            REQUEST_PERMISSIONS
        )

        binding.btnGenReport.setOnClickListener {
            viewModel.startCheck(requireContext())
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkDisplayState() {
        val displayMetrics = activity?.windowManager?.currentWindowMetrics
        val screenWidth = displayMetrics!!.bounds.width()
        val screenHeight = displayMetrics.bounds.height()
        val density = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            displayMetrics.density
        } else {
            0
        }
        Log.d("DISPLAY", "width: $screenWidth height: $screenHeight density: $density \n\n")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}