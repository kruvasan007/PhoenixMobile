package com.example.phoenixmobile.ui.device

import android.Manifest
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.phoenixmobile.databinding.FragmentMyDeviceBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File


class MyDeviceFragment : Fragment(), SensorEventListener {
    private val REQUEST_READ_PHONE_STATE = 1

    private lateinit var sensorManager: SensorManager
    private lateinit var loadingDialog: LoadingDialog;
    private var result: String = ""

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
        loadingDialog = LoadingDialog(requireActivity())
        binding.btnGenReport.setOnClickListener {
            loadingDialog.startLoadingDialog()
            checkMemory()
        }

    }

    private fun checkProcessor() {
        TODO("pro")
    }

    private fun checkMemory() {
        loadingDialog.setMessage("Checking memory...")

        val activityManager =
            context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo);
        val totalRam = memoryInfo.totalMem / (1024 * 1024 * 1024);
        val iPath: File = Environment.getDataDirectory()
        val iStat = StatFs(iPath.path)
        val iBlockSize = iStat.blockSizeLong
        val iAvailableBlocks = iStat.availableBlocksLong
        val iTotalBlocks = iStat.blockCountLong
        val iAvailableSpace = iAvailableBlocks * iBlockSize / (1024 * 1024 * 1024)
        val iTotalSpace = iTotalBlocks * iBlockSize / (1024 * 1024 * 1024)

        viewModel.setMemoryParams(totalRam, iTotalSpace, iAvailableSpace);

        Log.d("MEMORY", "$totalRam GB $iTotalSpace TOTAL $iAvailableSpace AVAILABLE \n\n")

        checkBattery()
    }

    private fun checkBattery() {
        loadingDialog.setMessage("Checking battery...")

        val receiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val status = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                val statusStr = when (status) {
                    BatteryManager.BATTERY_HEALTH_DEAD -> "BATTERY DEAD"
                    BatteryManager.BATTERY_HEALTH_COLD -> "BATTERY COLD"
                    BatteryManager.BATTERY_HEALTH_GOOD -> "BATTERY GOOD"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "BATTERY OVERHEAT"
                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "BATTERY OVER VOLTAGE"
                    else -> "UNKNOWN"
                }
                viewModel.setBatteryParams(status)
                result += "BATTERY: $statusStr"
                checkSensors()
                unregisterForContextMenu(view!!)
            }
        }
        requireContext().registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }


    private fun checkSensors() {
        loadingDialog.setMessage("Checking sensors...")
        //get sensors list
        sensorManager = context?.getSystemService(SENSOR_SERVICE) as SensorManager
        // val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE)
        //check gyroscope
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (sensor != null) {
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } else {
            Log.d("Sensors", "No gyroscope")
        }

        //viewModel.setSensorsParams(deviceSensors)

        checkOS()
    }

    private fun checkOS() {
        loadingDialog.setMessage("Checking OS...")
        val phoneModel = Build.MODEL
        val versionOS = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        val hardware = Build.HARDWARE;
        val board = Build.BOARD;

        viewModel.setSystemParams(versionOS, sdkVersion, phoneModel, hardware, board)

        Log.d(
            "SYSTEM",
            "$versionOS version OS, $sdkVersion sdk, $phoneModel, $hardware hardware, $board board model \n\n"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            checkDisplayState()
        } else {
            loadingDialog.dismissDialog()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkDisplayState() {
        loadingDialog.setMessage("Checking display...")

        val displayMetrics = activity?.windowManager?.currentWindowMetrics
        val screenWidth = displayMetrics!!.bounds.width()
        val screenHeight = displayMetrics.bounds.height()
        val density = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            displayMetrics.density
        } else {
            0
        }
        viewModel.setDisplayParams(screenHeight, screenWidth, density.toInt())

        Log.d("DISPLAY", "width: $screenWidth height: $screenHeight density: $density \n\n")

        checkNetworkState()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkNetworkState() {
        loadingDialog.setMessage("Checking network...")
        val permissionCheck = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.READ_PHONE_STATE
        )
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                REQUEST_READ_PHONE_STATE
            )
        } else {
            val telephonyManager =
                requireContext().getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager

            val level = telephonyManager.signalStrength!!.level
            viewModel.setNetworkParams(
                level,
                telephonyManager.dataState,
                telephonyManager.simState
            )

            val levelStr = when (telephonyManager.signalStrength?.level) {
                0 -> "NO_SIGNAL"

                1 -> "BAD_SIGNAL"

                2 -> "OK_SIGNAL"

                3 -> "GOOD_SIGNAL"
                4 -> "BEST_SIGNAL"

                else -> "UNKNOWN"
            }


            val simStateStr = when (telephonyManager.simState) {
                TelephonyManager.SIM_STATE_READY -> "SIM_STATE_READY"

                TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "SIM_STATE_NETWORK_LOCKED"

                TelephonyManager.SIM_STATE_NOT_READY -> "SIM_STATE_NOT_READY"

                TelephonyManager.SIM_STATE_UNKNOWN -> "SIM_STATE_UNKNOWN"

                else -> "UNKNOWN"
            }
            val dataStateStr = when (telephonyManager.dataState) {
                TelephonyManager.DATA_CONNECTED -> "DATA_CONNECTED"

                TelephonyManager.DATA_CONNECTING -> "DATA_CONNECTING"

                TelephonyManager.DATA_DISCONNECTED -> "DATA_DISCONNECTED"

                TelephonyManager.DATA_SUSPENDED -> "DATA_SUSPENDED"

                else -> "UNKNOWN"
            }

            Log.d("NETWORK", "$levelStr  $dataStateStr $simStateStr\n\n")

        }

        loadingDialog.dismissDialog()

        viewModel.pushReport()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d("AC", event!!.values.get(1).toString())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //Log.d("AC", accuracy.)
    }
}