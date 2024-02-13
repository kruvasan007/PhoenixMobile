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
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.telephony.TelephonyManager
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
import java.io.File


class MyDeviceFragment : Fragment() {
    private val REQUEST_READ_PHONE_STATE = 1

    private lateinit var sensorManager: SensorManager
    private var result: String = ""

    private var _binding: FragmentMyDeviceBinding? = null
    private val viewModel: MyDeviceViewModel by activityViewModels()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val myDeviceViewModel = ViewModelProvider(this).get(MyDeviceViewModel::class.java)

        _binding = FragmentMyDeviceBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val phoneModel = Build.MODEL
        binding.deviceName.text = phoneModel

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
                result.plus("BATTERY: $status");
            }
        }
        requireContext().registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGenReport.setOnClickListener {
            result = ""
            binding.reportText.text = result
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                checkNetworkState()
                checkDisplayState()
            }
            checkOS()
            checkMemory()
            checkSensors()
        }

    }

    private fun checkMemory() {
        val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        var memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo);

        var totalRam = memoryInfo.totalMem / (1024 * 1024 * 1024);

        val iPath: File = Environment.getDataDirectory()
        val iStat = StatFs(iPath.path)
        val iBlockSize = iStat.blockSizeLong
        val iAvailableBlocks = iStat.availableBlocksLong
        val iTotalBlocks = iStat.blockCountLong
        val iAvailableSpace = iAvailableBlocks * iBlockSize / (1024 * 1024 * 1024)
        val iTotalSpace = iTotalBlocks * iBlockSize / (1024 * 1024 * 1024)

        result +="MEMORY: $totalRam GB $iTotalSpace TOTAL $iAvailableSpace AVAILABLE \n\n"

        binding.reportText.text = result
    }

    private fun checkSensors() {
        sensorManager = context?.getSystemService(SENSOR_SERVICE) as SensorManager
        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val dev = deviceSensors[0];
        result += "SENSORS:  $dev\n\n"

        binding.reportText.text = result
    }

    private fun checkOS() {
        val phoneModel = Build.MODEL
        val versionOS = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        val hardware = Build.HARDWARE;
        val board = Build.BOARD;

        result += "SYSTEM: $versionOS version OS, $sdkVersion sdk, $phoneModel, $hardware hardware, $board board model \n\n"
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkDisplayState() {
        val displayMetrics = activity?.windowManager?.currentWindowMetrics

        val screenWidth = displayMetrics!!.bounds.width()
        val screenHeight = displayMetrics.bounds.height()
        val density = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            displayMetrics.density
        } else {
            null
        }
        result += "DISPLAY: width: $screenWidth height: $screenHeight density: $density \n\n"
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkNetworkState() {
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

            result +=  "NETWORK: $levelStr  $dataStateStr $simStateStr\n\n"

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}