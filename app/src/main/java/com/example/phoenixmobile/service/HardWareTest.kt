package com.example.phoenixmobile.service

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.os.StatFs
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.phoenixmobile.data.Repository
import java.io.File
import kotlin.math.sqrt


class HardWareTest : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var flagGyroscope = false

    // sensitivity for gyroscope test
    private val STEP_MAX_MAGNITUDE_GYRO = 2.0

    // data for memory collection
    private var totalRam = 0L
    private var totalSpace = 0L
    private var avalSpace = 0L

    // for battery test
    private var batteryStatus = -1
    private var batteryCycleCount = -1

    //for gyro test
    private lateinit var observer: Observer<ArrayList<Float>>
    private val listen: MutableLiveData<ArrayList<Float>> = MutableLiveData()
    private val NS2S = 1.0f / 1000000000.0f
    private var timestamp: Float = 0f

    private fun checkBattery() {
        val receiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // getting battery status data
                batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                // if possible, check the number of battery charging cycles
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    batteryCycleCount = intent.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)
                }
                Log.d("BATTERY", getStringStatus(batteryStatus) + " " + batteryCycleCount)
                application.unregisterReceiver(this)
                Repository.setBatteryReport(batteryStatus)
            }
        }
        application.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

    }

    fun getStringStatus(status: Int): String {
        val statusStr = when (status) {
            BatteryManager.BATTERY_HEALTH_DEAD -> "BATTERY DEAD"
            BatteryManager.BATTERY_HEALTH_COLD -> "BATTERY COLD"
            BatteryManager.BATTERY_HEALTH_GOOD -> "BATTERY GOOD"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "BATTERY OVERHEAT"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "BATTERY OVER VOLTAGE"
            else -> "UNKNOWN"
        }
        return statusStr
    }

    private fun checkGyroscope() {
        //get sensors list
        sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //check gyroscope
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (sensor != null) {
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            listen.setValue(ArrayList())
            observer = Observer { data ->
                // if the delta of the values is greater than the specified accuracy
                if (data.size > 2) {
                    if (data.max() > STEP_MAX_MAGNITUDE_GYRO) {
                        flagGyroscope = true
                        Repository.setGyroscopeReport(flagGyroscope)
                        Log.d("GYRO", "OK")
                    }
                }
            }
            listen.observeForever(observer)
        } else {
            flagGyroscope = false
            Repository.setGyroscopeReport(flagGyroscope)
            Log.d("GYRO", "No gyroscope")
        }
    }

    //listening to data changes from the gyroscope
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            val dT = (event.timestamp - timestamp) * NS2S
            val axisX: Float = event.values[0]
            val axisY: Float = event.values[1]
            val axisZ: Float = event.values[2]
            val omegaMagnitude: Float = sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ)
            //val thetaOverTwo: Float = omegaMagnitude * dT / 2.0f
            listen.value!!.add(omegaMagnitude)
            listen.postValue(listen.value)
            if (flagGyroscope) {
                listen.removeObserver(observer)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //Log.d("AC", accuracy.)
    }

    private fun checkMemory() {
        val activityManager =
            application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo);
        totalRam = memoryInfo.totalMem / (1024 * 1024 * 1024);

        val iPath: File = Environment.getDataDirectory()
        val iStat = StatFs(iPath.path)
        val iBlockSize = iStat.blockSizeLong
        val iAvailableBlocks = iStat.availableBlocksLong
        val iTotalBlocks = iStat.blockCountLong

        // calculating the total space and free space
        avalSpace = iAvailableBlocks * iBlockSize / (1024 * 1024 * 1024)
        totalSpace = iTotalBlocks * iBlockSize / (1024 * 1024 * 1024)

        Log.d("MEMORY", "$totalRam GB $totalSpace TOTAL $avalSpace AVAILABLE \n\n")

        Repository.setMemoryReport(totalRam, totalSpace, avalSpace)
    }

    @SuppressLint("HardwareIds")
    private fun checkOS() {
        val bootloader = Build.BOOTLOADER
        val display = Build.DISPLAY
        val deviceId = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        val phoneModel = Build.MODEL
        val versionOS = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        val hardware = Build.HARDWARE;
        val board = Build.BOARD;

        Repository.setOSReport(
            "versionOS:$versionOS; sdk:$sdkVersion; " +
                    "model:$phoneModel; hardware:$hardware; boardModel:$board; " +
                    "bootloader:$bootloader; display:$display; imei:$deviceId"
        )
        Repository.setDeviceId(deviceId)

        Log.d(
            "SYSTEM",
            "versionOS:$versionOS; sdk:$sdkVersion; " +
                    "model:$phoneModel; hardware:$hardware; boardModel:$board; " +
                    "bootloader:$bootloader; display:$display; imei:$deviceId"
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Repository.getReportState().observeForever {
            if (it == Repository.REPORT_STARTED) {
                flagGyroscope = false
                checkBattery()
                checkOS()
                checkMemory()
                checkGyroscope()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d("HARDWARE", "Bind")
        return Binder()
    }
}