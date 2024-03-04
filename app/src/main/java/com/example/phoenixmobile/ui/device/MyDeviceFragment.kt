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
import com.example.phoenixmobile.data.Repository
import com.example.phoenixmobile.databinding.FragmentMyDeviceBinding
import com.example.phoenixmobile.ui.device.adapter.LoadingAdapter
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.TreeMap


class MyDeviceFragment : Fragment() {
    private val REQUEST_PERMISSIONS = 1;

    private lateinit var adapter: LoadingAdapter
    private var testList: TreeMap<String, Int> = TreeMap()

    private var _binding: FragmentMyDeviceBinding? = null
    private val viewModel: MyDeviceViewModel by activityViewModels()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyDeviceBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.deviceName.text = viewModel.getDeviceName()
        return root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = LoadingAdapter(testList)

        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        binding.testList.layoutManager = layoutManager

        binding.testList.adapter = adapter

        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            REQUEST_PERMISSIONS
        )

        binding.btnGenReport.setOnClickListener {
            startGenerateReport()
        }

        fragmentTextUpdateObserver()
        fragmentAudioTestRequest()

        viewModel.reportState().observe(viewLifecycleOwner) {
            if (it == Repository.REPORT_DONE || it == Repository.REPORT_ERROR) {
                binding.testList.visibility = View.INVISIBLE
                binding.progressBar.isIndeterminate = false
                binding.btnGenReport.isEnabled = true
                binding.reportCardView.visibility = View.VISIBLE
            }
        }

        viewModel.bluetoothConnect().observe(viewLifecycleOwner) { bluetoothFlag ->
            if (!bluetoothFlag && Repository.getAudioTest().value == Repository.AUDIO_CHECK_STARTED) {
                Snackbar.make(
                    binding.root,
                    "Please, disconnect with bluetooth device",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Yes, i disable bluetooth") {
                    viewModel.tryBluetoothAgain()
                }.show()
            }
        }

        viewModel.report().observe(viewLifecycleOwner) {
            binding.reportText.text = it
        }
    }


    private fun startGenerateReport() {
        binding.reportCardView.visibility = View.INVISIBLE
        binding.btnGenReport.isEnabled = false
        binding.progressBar.isIndeterminate = true
        binding.testList.visibility = View.VISIBLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            checkDisplayState()
        viewModel.startCheck()
    }

    private fun fragmentAudioTestRequest() {
        viewModel.audioTest().observe(viewLifecycleOwner) { data ->
            when (data) {
                Repository.AUDIO_CHECK_START_PLAYING -> {
                    Snackbar.make(
                        binding.root,
                        "Start play audio sample...",
                        Snackbar.ANIMATION_MODE_SLIDE
                    ).show()
                }

                Repository.AUDIO_WAIT_ANSWER -> {
                    MaterialAlertDialogBuilder(
                        requireContext(),
                        com.google.android.material.R.style.Base_ThemeOverlay_Material3_Dialog
                    )
                        .setMessage("Does the recorded sound match the sample?")
                        .setNegativeButton("No") { dialog, which ->
                            viewModel.setAudioReply(false)
                        }
                        .setPositiveButton("Yes") { dialog, which ->
                            viewModel.setAudioReply(true)
                        }
                        .setCancelable(false)
                        .show()
                }

                Repository.AUDIO_DONE_PLAY -> {
                    Snackbar.make(
                        binding.root,
                        "Start play audio recording...",
                        Snackbar.ANIMATION_MODE_SLIDE
                    ).show()
                }
            }
        }
    }


    private fun fragmentTextUpdateObserver() {
        viewModel.getTest().observe(viewLifecycleOwner) { data ->
            testList = data
            adapter.updateList(testList)
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
            0f
        }
        Log.d("DISPLAY", "width: $screenWidth height: $screenHeight density: $density \n\n")
        viewModel.setDisplayCheck(screenWidth, screenHeight, density)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}