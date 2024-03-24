package com.example.phoenixmobile.ui.device

import android.Manifest
import android.annotation.SuppressLint
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

    @SuppressLint("PrivateResource")
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
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            REQUEST_PERMISSIONS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                REQUEST_PERMISSIONS
            )
        }



        binding.btnGenReport.setOnClickListener {
            // create info dialog
            myDialog = MaterialAlertDialogBuilder(
                requireContext(),
                com.google.android.material.R.style.Base_ThemeOverlay_Material3_Dialog
            )
                .setMessage(getString(R.string.startMsg))
                .setPositiveButton("Let's go!") { dialog, which ->
                    startGenerateReport()
                }
                .setCancelable(false)
                .show()
        }

        // setUp all of observers

        // chips about report generate process
        fragmentTextUpdateObserver()
        // audio test done or not
        fragmentAudioTestRequestObserver()
        // listener end of the report
        fragmentReportStateUpdateObserver()
        // print test report from server
        fragmentTextReportDoneObserver()

        checkBluetoothConnection()

        // FAQ button
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

        binding.btnSave.setOnClickListener {

            // download report like pdf
            viewModel.downloadFile()

            /*
            // get report text
            val text = reportList.toList().toString()

            //print reprot like QR
            if (reportList.size != 0) {
                val writer = MultiFormatWriter()
                try {
                    val matrix = writer.encode(text, BarcodeFormat.QR_CODE, 600, 600)
                    val encoder = BarcodeEncoder()
                    val bitmap = encoder.createBitmap(matrix)
                    //set data image to imageview
                    createQrPopup(bitmap)
                } catch (e: WriterException) {
                    e.printStackTrace()
                }
            }*/
        }

    }

    private fun fragmentTextReportDoneObserver() {
        viewModel.report().observe(viewLifecycleOwner) { data ->
            binding.reportList.visibility = View.VISIBLE
            reportList = data

            //TODO: CHANGE TO WAIT SERVER RESPONSE
            adapterReportText.updateList(reportList)
        }
    }

    private fun fragmentReportStateUpdateObserver() {
        viewModel.reportState().observe(viewLifecycleOwner) {
            if (it == Repository.REPORT_DONE || it == Repository.REPORT_ERROR) {
                binding.testList.visibility = View.INVISIBLE
                binding.progressBar.isIndeterminate = false
                binding.btnGenReport.isEnabled = true
                binding.btnSave.isEnabled = true
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
        binding.btnSave.isEnabled = false
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

    @SuppressLint("PrivateResource")
    private fun fragmentAudioTestRequestObserver() {
        // the order of changing the states of the audio test

        // START PLAYING -> WAIT USER RESPONSE -> START PLAYING RECORDING -> DONE AUDIO CHECK AND START GYRO
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

                Repository.AUDIO_CHECK_DONE -> {
                    Snackbar.make(
                        binding.root,
                        "Please, shake your phone...",
                        Snackbar.ANIMATION_MODE_SLIDE
                    ).show()
                }

                Repository.AUDIO_CHECK_ERROR -> {
                    Snackbar.make(
                        binding.root,
                        "Please, shake your phone...",
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

    /*private fun createQrPopup(bitmap: Bitmap) {
        val alertadd = AlertDialog.Builder(requireContext())
        val factory = LayoutInflater.from(requireContext())
        val view: View = factory.inflate(R.layout.qr_popup, null)
        alertadd.setView(view)
        val imageView: ImageView = view.findViewById(R.id.qr_code)
        imageView.setImageBitmap(bitmap)
        alertadd.setNeutralButton(
            "Ok"
        ) { dlg, sumthin -> dlg.cancel() }

        alertadd.show()
    }*/


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}