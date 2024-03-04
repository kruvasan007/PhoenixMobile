package com.example.phoenixmobile.data

import androidx.lifecycle.MutableLiveData
import com.example.phoenixmobile.App
import com.example.phoenixmobile.database.PriceDao
import com.example.phoenixmobile.database.PriceDto
import com.example.phoenixmobile.model.CPUReport
import com.example.phoenixmobile.model.DisplayReport
import com.example.phoenixmobile.model.HardWareReport
import com.example.phoenixmobile.model.NetworkReport
import com.example.phoenixmobile.model.Report
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.TreeMap


object Repository {
    private val testList = MutableLiveData<TreeMap<String, Int>>()
    private val audioTest = MutableLiveData<Int>()
    private val reportDone = MutableLiveData<Int>()
    private val report = MutableLiveData<Report>()
    private val reportText = MutableLiveData<TreeMap<String, String>>()
    private var aboutdeviceText: String = ""
    private var deviceId: String = ""

    private val bluetoothFlag = MutableLiveData<Boolean>()

    private val priceList = MutableLiveData<List<PriceDto>>()
    private val priceDao: PriceDao = App.getDatabase()!!.priceDao()

    private val CPUReport = MutableLiveData<CPUReport>()
    private val hardWareReport = MutableLiveData<HardWareReport>()
    private val displayReport = MutableLiveData<DisplayReport>()
    private val networkReport = MutableLiveData<NetworkReport>()

    private val REPORT_NULL = 0
    val REPORT_STARTED = 1
    val REPORT_DONE = 2
    val REPORT_ERROR = 3
    val REPORT_IN_PROCESS = 4

    val AUDIO_CHECK_NULL = 0
    val AUDIO_CHECK_STARTED = 1
    val AUDIO_CHECK_DONE = 777
    val AUDIO_CHECK_ERROR = 888
    val AUDIO_WAIT_ANSWER = 4
    val AUDIO_DONE_PLAY = 5
    val AUDIO_CHECK_START_PLAYING = 6

    //private val retrofitService = Common.retrofitService
    private var job: Job? = null
    private val loadError = MutableLiveData<String?>()
    private val loading = MutableLiveData<Boolean>()

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    init {
        loadingTest()
        bluetoothFlag.postValue(false)
        reportDone.postValue(REPORT_NULL)
        audioTest.postValue(AUDIO_CHECK_NULL)
        hardWareReport.postValue(HardWareReport())
        testList.observeForever { data ->
            if (data != null) {
                if (!data.values.contains(REPORT_NULL) && (data.values.contains(AUDIO_CHECK_DONE) || data.values.contains(
                        REPORT_ERROR
                    ))
                ) {
                    setUpReportToSend()
                }
            }
        }
        loadError.value = null
        loading.value = false
    }

    fun setDeviceId(value: String) {
        deviceId = value
    }

    fun setReportStarted() {
        reportDone.postValue(REPORT_STARTED)
    }

    fun setBluetoothConnected() {
        bluetoothFlag.postValue(false)
    }

    fun setBluetoothDisconnected() {
        bluetoothFlag.postValue(true)
    }

    fun getBluetoothFlag() = bluetoothFlag

    private fun loadingTest() {
        loading.value = true
        job = CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                setUpTest()
            }
        }
        loadError.value = ""
        loading.value = false
    }

    private fun setUpReportToSend() {
        reportDone.value = REPORT_DONE
        report.value = Report(
            deviceId,
            aboutdeviceText,
            CPUReport.value,
            displayReport.value,
            hardWareReport.value,
            networkReport.value,
            audioTest.value == AUDIO_CHECK_DONE
        )
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()
        val jObject = JSONObject(gsonPretty.toJson(report.value))
        val reportMap = TreeMap<String, String>()
        for (testName in jObject.keys()) {
            println(jObject.getString(testName) + "\n")
            reportMap[testName] = jObject.getString(testName)
        }
        reportText.postValue(reportMap)
        setUpTest()
    }

    private fun setUpTest() {
        testList.value = TreeMap(
            mapOf(
                Pair("CPU", REPORT_NULL),
                Pair("Battery", REPORT_NULL),
                Pair("Network", REPORT_NULL),
                Pair("Gyroscope", REPORT_NULL),
                Pair("Bluetooth", REPORT_NULL),
                Pair("Memory", REPORT_NULL),
                Pair("Display", REPORT_NULL),
                Pair("Audio System", REPORT_NULL),
                Pair("GPS", REPORT_NULL)
            )
        )
        bluetoothFlag.postValue(false)
        audioTest.postValue(AUDIO_CHECK_NULL)
    }

    fun setCPUReport(frequency: String, mark: String) {
        testList.value?.set("CPU", REPORT_DONE)
        testList.postValue(testList.value)

        val report = CPUReport(frequency, mark)
        CPUReport.postValue(report)
    }

    fun setNetworkReport(
        level: Int,
        dataStatus: Int,
        simState: Int,
        GPS: Boolean,
        bluetooth: Boolean
    ) {
        if (level != -1 && dataStatus != -1 && simState != -1) {
            testList.value?.set("Network", REPORT_DONE)
        } else {
            testList.value?.set("Network", REPORT_ERROR)
        }
        if (bluetooth) {
            testList.value?.set("Bluetooth", REPORT_DONE)
        } else {
            testList.value?.set("Bluetooth", REPORT_ERROR)
        }
        if (GPS) {
            testList.value?.set("GPS", REPORT_DONE)
        } else {
            testList.value?.set("GPS", REPORT_ERROR)
        }
        testList.postValue(testList.value)

        val report = NetworkReport(level, dataStatus, simState, GPS, bluetooth)
        networkReport.postValue(report)
    }

    fun setMemoryReport(ram: Long, total: Long, aval: Long) {
        testList.value?.set("Memory", REPORT_DONE)
        testList.postValue(testList.value)

        hardWareReport.value?.ram = ram
        hardWareReport.value?.totalSpace = total
        hardWareReport.value?.availabSpace = aval
        report.value?.hardWareReport = hardWareReport.value
    }

    fun setDisplayReport(screenWidth: Int, screenHeight: Int, density: Float) {
        if (screenHeight != -1 && screenWidth != -1) {
            testList.value?.set("Display", REPORT_DONE)
        } else {
            testList.value?.set("Display", REPORT_ERROR)
        }
        testList.postValue(testList.value)

        val report = DisplayReport(screenWidth, screenHeight, density)
        displayReport.postValue(report)
    }

    fun setGyroscopeReport(gyroState: Boolean) {
        testList.value?.set("Gyroscope", REPORT_DONE)
        testList.postValue(testList.value)

        hardWareReport.value?.gyroscope = gyroState.toString()
        report.value?.hardWareReport = hardWareReport.value
    }

    fun setBatteryReport(batteryStatus: Int) {
        testList.value?.set("Battery", REPORT_DONE)
        testList.postValue(testList.value)

        hardWareReport.value?.batteryState = batteryStatus
        report.value?.hardWareReport = hardWareReport.value
    }

    fun setReportTimeExpired() {
        reportDone.postValue(REPORT_ERROR)
        reportText.postValue(TreeMap(mapOf(Pair("", "Error while receiving report!"))))
    }

    fun getTestList() = testList
    fun getAudioTest() = audioTest
    fun getReportState() = reportDone

    fun getReportToText() = reportText

    fun setAudioResponse(state: Int) {
        testList.value?.set(
            "Audio System",
            state
        )
        testList.postValue(testList.value)

        audioTest.postValue(state)
    }

    fun insertPriceTable(priceItem: PriceDto) {
        priceDao.insertModel(priceItem)
    }

    fun loadPriceTable() {
        job = CoroutineScope(Dispatchers.Main).launch {
            priceList.postValue(priceDao.getAll())
        }
    }

    fun getPriceList() = priceList
    fun setOSReport(report: String) {
        aboutdeviceText = report
    }
}