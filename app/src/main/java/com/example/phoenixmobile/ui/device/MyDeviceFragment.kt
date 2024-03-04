package com.example.phoenixmobile.ui.device

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.phoenixmobile.R
import com.example.phoenixmobile.data.Repository
import com.example.phoenixmobile.databinding.FragmentMyDeviceBinding
import com.example.phoenixmobile.ui.device.adapter.LoadingAdapter
import com.example.phoenixmobile.ui.device.adapter.ReportAdapter
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.TreeMap


class MyDeviceFragment : Fragment() {
    private val REQUEST_PERMISSIONS = 1
    var myDialog: AlertDialog? = null

    private val DISPLAY_ERROR_INT_STATE = -1
    private val DISPLAY_ERROR_FLOAT_STATE = 0f
    private lateinit var adapterTestChips: LoadingAdapter
    private lateinit var adapterReportText: ReportAdapter
    private var testList: TreeMap<String, Int> = TreeMap()
    private var reportList: TreeMap<String, String> = TreeMap()

    private var _binding: FragmentMyDeviceBinding? = null
    private val viewModel: MyDeviceViewModel by activityViewModels()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyDeviceBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //set model label
        binding.deviceName.text = viewModel.getDeviceName()

        //set adapter for recycler view with report text
        adapterReportText = ReportAdapter(reportList)
        val layoutManagerReport = LinearLayoutManager(context)
        binding.reportList.adapter = adapterReportText
        binding.reportList.layoutManager = layoutManagerReport

        //set adapter for view with chips about test
        adapterTestChips = LoadingAdapter(testList)
        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        binding.testList.layoutManager = layoutManager
        binding.testList.adapter = adapterTestChips

        //require permissions
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
        fragmentAudioTestRequestObserver()
        fragmentReportStateUpdateObserver()

        checkBluetoothConnection()

        binding.floatingActionButton.setOnClickListener {
            MaterialAlertDialogBuilder(
                requireContext(),
                com.google.android.material.R.style.Base_ThemeOverlay_Material3_Dialog
            ).setTitle("FAQ").setMessage(getString(R.string.FAQ_descr))
                .setPositiveButton("Cool") { dialog, which ->
                    dialog.cancel()
                }
                .create().show()
        }

        viewModel.report().observe(viewLifecycleOwner) { data ->
            binding.reportList.visibility = View.VISIBLE
            reportList = data
            adapterReportText.updateList(reportList)
        }
    }

    private fun fragmentReportStateUpdateObserver() {
        viewModel.reportState().observe(viewLifecycleOwner) {
            if (it == Repository.REPORT_DONE || it == Repository.REPORT_ERROR) {
                binding.testList.visibility = View.INVISIBLE
                binding.progressBar.isIndeterminate = false
                binding.btnGenReport.isEnabled = true
                binding.reportCardView.visibility = View.VISIBLE
            }
        }
    }

    private fun checkBluetoothConnection() {
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
    }

    private fun startGenerateReport() {
        binding.reportCardView.visibility = View.INVISIBLE
        binding.btnGenReport.isEnabled = false
        binding.progressBar.isIndeterminate = true
        binding.testList.visibility = View.VISIBLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            checkDisplayState()
        else {
            Log.d("SDK_STATE", "Display status cannot be checked")
            viewModel.setDisplayCheck(
                DISPLAY_ERROR_INT_STATE,
                DISPLAY_ERROR_INT_STATE,
                DISPLAY_ERROR_FLOAT_STATE
            )
        }
        viewModel.startCheck()
    }

    private fun fragmentAudioTestRequestObserver() {
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
                    if (myDialog == null || !(myDialog!!.isShowing)) {
                        myDialog = MaterialAlertDialogBuilder(
                            requireContext(),
                            com.google.android.material.R.style.Base_ThemeOverlay_Material3_Dialog
                        )
                            .setMessage("Does the recorded sound match the sample?")
                            .setNegativeButton("No") { dialog, which ->
                                viewModel.setAudioReply(false)
                            }
                            .setPositiveButton("Yes") { dialog, which ->
                                println("TYPE TRUE")
                                viewModel.setAudioReply(true)
                            }
                            .setCancelable(false)
                            .show()
                    }
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
            adapterTestChips.updateList(testList)
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