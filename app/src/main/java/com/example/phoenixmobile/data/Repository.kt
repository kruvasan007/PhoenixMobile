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

    private val reportUrl = MutableLiveData<String>()

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

    //handler for server
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    init {
        loadingTest()
        bluetoothFlag.postValue(false)
        reportDone.postValue(REPORT_NULL)
        audioTest.postValue(AUDIO_CHECK_NULL)
        hardWareReport.postValue(HardWareReport())

        //checking state of report
        testList.observeForever { data ->
            if (data != null) {
                // If all the data is filled in correctly or there are errors
                if (!data.values.contains(REPORT_NULL) && (data.values.contains(AUDIO_CHECK_DONE) ||
                            data.values.contains(REPORT_ERROR))
                ) {
                    setUpReportToSend()
                }
            }
        }
        loadError.value = null
        loading.value = false
    }

    // getters
    fun getBluetoothFlag() = bluetoothFlag
    fun getTestList() = testList
    fun getAudioTest() = audioTest
    fun getReportState() = reportDone

    fun getReportToText() = reportText


    fun setDeviceId(value: String) {
        deviceId = value
    }

    // if report process start
    fun setReportStarted() {
        reportDone.postValue(REPORT_STARTED)
    }

    fun setBluetoothConnected() {
        bluetoothFlag.postValue(false)
    }

    fun setBluetoothDisconnected() {
        bluetoothFlag.postValue(true)
    }

    // reloadTest in start app
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
        // Preparing data to be sent to the server from display to the user
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

        // TODO: send report to server
        sendReportToServer()

        setUpTest()
    }


    private fun setUpTest() {
        // Updating data for a new test
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
        if (frequency != "") {
            testList.value?.set("CPU", REPORT_DONE)
        }
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
        if (ram > 0 && total > 0 && aval > 0)
            testList.value?.set("Memory", REPORT_DONE)
        else
            testList.value?.set("Memory", REPORT_ERROR)
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
        if (gyroState)
            testList.value?.set("Gyroscope", REPORT_DONE)
        else
            testList.value?.set("Gyroscope", REPORT_ERROR)
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
        setUpReportToSend()
    }

    fun setAudioResponse(state: Int) {
        testList.value?.set(
            "Audio System",
            state
        )
        audioTest.postValue(state)
        testList.postValue(testList.value)
    }

    fun setOSReport(report: String) {
        aboutdeviceText = report
    }


    // working with databases
    fun insertPriceTable(priceItem: PriceDto) {
        priceDao.insertModel(priceItem)
    }

    fun loadPriceTable() {
        job = CoroutineScope(Dispatchers.Main).launch {
            //TODO: get data from server
            priceList.postValue(priceDao.getAll())
        }
    }

    fun getPriceList() = priceList

    fun getReportPdf() = reportUrl

    // working with server (sandbox)
    private fun sendReportToServer() {
        // TODO GET ANSWER FROM SERVER

        /* test */
        val reportMap = TreeMap<String, String>()
        reportMap["Condition"] = "Good"
        reportMap["Price"] = "400"
        getReportPdfFromServer() // for test
        reportMap["Report"] =
            "https://drive.google.com/uc?export=download&confirm=no_antivirus&id=1LLTcuQEmHSLTQ6HY9vKfW2VNqDe8t232"
        /* test */

        //TODO: get data from server like ReportAnwer =
        reportText.postValue(reportMap)
    }

    private fun getReportPdfFromServer() {
        reportUrl.postValue("https://drive.google.com/uc?export=download&confirm=no_antivirus&id=1LLTcuQEmHSLTQ6HY9vKfW2VNqDe8t232")
    }

}