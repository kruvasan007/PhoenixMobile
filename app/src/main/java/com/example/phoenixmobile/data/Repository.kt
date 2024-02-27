package com.example.phoenixmobile.data

import androidx.lifecycle.MutableLiveData
import com.example.phoenixmobile.App
import com.example.phoenixmobile.database.PriceDao
import com.example.phoenixmobile.database.ReportDao
import com.example.phoenixmobile.model.CPUReport
import com.example.phoenixmobile.model.DisplayReport
import com.example.phoenixmobile.model.HardWareReport
import com.example.phoenixmobile.model.NetworkReport
import com.example.phoenixmobile.model.Report
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.File
import java.io.InputStreamReader
import java.util.TreeMap


object Repository {
    private val testList = MutableLiveData<TreeMap<String, Boolean>>()
    private val audioTest = MutableLiveData<Int>()
    private val reportDone = MutableLiveData<Int>()
    private val report = MutableLiveData<Report>()
    private val reportText = MutableLiveData<String>()

    private val priceDao: PriceDao = App.getDatabase()!!.priceDao()

    private val CPUReport = MutableLiveData<CPUReport>()
    private val hardWareReport = MutableLiveData<HardWareReport>()
    private val displayReport = MutableLiveData<DisplayReport>()
    private val networkReport = MutableLiveData<NetworkReport>()

    val REPORT_STARTED = 1
    val REPORT_DONE = 2
    val REPORT_ERROR = 3
    private val REPORT_NULL = 0

    private val AUDIO_CHECK_NULL = 0
    val AUDIO_WAIT_ANSWER = 1
    val AUDIO_CHECK_DONE = 2
    val AUDIO_CHECK_ERROR = 3

    private val reportDao: ReportDao = App.getDatabase()!!.reportDao()

    //private val retrofitService = Common.retrofitService
    private var job: Job? = null
    private val loadError = MutableLiveData<String?>()
    private val loading = MutableLiveData<Boolean>()

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    init {
        loadingTest()
        reportDone.postValue(REPORT_NULL)
        audioTest.postValue(AUDIO_CHECK_NULL)
        hardWareReport.postValue(HardWareReport())
        testList.observeForever { data ->
            if (data != null) {
                if (!data.values.contains(false)) {
                    reportDone.value = REPORT_DONE
                    report.value = Report(
                        CPUReport.value,
                        displayReport.value,
                        hardWareReport.value,
                        networkReport.value,
                        audioTest.value == AUDIO_CHECK_DONE
                    )
                    reportText.value = Gson().toJson(
                        report.value
                    )
                    setUpTest()
                }

            }
        }
        loadError.value = null
        loading.value = false
    }

    private fun setUpTest() {
        testList.value = TreeMap(
            mapOf(
                Pair("CPU", false),
                Pair("Battery", false),
                Pair("Network", false),
                Pair("Gyroscope", false),
                Pair("Bluetooth", false),
                Pair("Memory", false),
                Pair("Display", false),
                Pair("Audio System", false),
                Pair("GPS", false)
            )
        )
    }

    fun setCPUReport(frequency: String, mark: String) {
        val report = CPUReport(frequency, mark)
        CPUReport.postValue(report)

        testList.value?.set("CPU", true)
        testList.postValue(testList.value)
    }

    fun setNetworkReport(level: Int, dataStatus: Int, simState: Int, GPS: Int, bluetooth: Boolean) {
        val report = NetworkReport(level, dataStatus, simState, GPS, bluetooth)
        networkReport.postValue(report)
        if (level != -1 && dataStatus != -1 && simState != -1) testList.value?.set("Network", true)
        if (bluetooth) testList.value?.set("Bluetooth", true)
        if (GPS != -1) testList.value?.set("GPS", true)
        testList.postValue(testList.value)
    }

    fun setMemoryReport(ram: Long, total: Long, aval: Long) {
        hardWareReport.value?.ram = ram
        hardWareReport.value?.totalSpace = total
        hardWareReport.value?.availabSpace = aval

        report.value?.hardWareReport = hardWareReport.value
        testList.value?.set("Memory", true)
        testList.postValue(testList.value)
    }

    fun setDisplayReport(screenWidth: Int, screenHeight: Int, density: Float) {
        val report = DisplayReport(screenWidth, screenHeight, density)
        if (screenHeight != -1 && screenWidth != -1 && density != 0f) displayReport.postValue(report)

        testList.value?.set("Display", true)
        testList.postValue(testList.value)
    }

    fun setGyroscopeReport(gyroState: Boolean) {
        hardWareReport.value?.gyroscope = gyroState.toString()
        report.value?.hardWareReport = hardWareReport.value
        testList.value?.set("Gyroscope", true)
        testList.postValue(testList.value)
    }

    fun setBatteryReport(batteryStatus: Int) {
        hardWareReport.value?.batteryState = batteryStatus
        report.value?.hardWareReport = hardWareReport.value
        testList.value?.set("Battery", true)
        testList.postValue(testList.value)
    }

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

    private fun setPriceTable() {
        try {
            // get JSONObject from JSON file
            val inputStreamReader =
                InputStreamReader(File("raw/configs_pattern.json").inputStream())
            println(inputStreamReader.readLines())
            /* val obj: JSONObject = JSONObject(JSON_STRING)
             // fetch JSONObject named employee
             val employee = obj.getJSONObject("employee")
             // get employee name and salary
             name = employee.getString("name")
             salary = employee.getString("salary")
             // set employee name and salary in TextView's
             employeeName.setText("Name: " + name)
             employeeSalary.setText("Salary: $salary")*/
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun getTestList() = testList
    fun getAudioTest() = audioTest
    fun getReportState() = reportDone

    fun getReportToText() = reportText

    fun setAudioResponse(state: Int) {
        audioTest.postValue(state)
        testList.value?.set(
            "Audio System",
            state == AUDIO_CHECK_DONE || state == AUDIO_CHECK_ERROR
        )
        testList.postValue(testList.value)
    }
}