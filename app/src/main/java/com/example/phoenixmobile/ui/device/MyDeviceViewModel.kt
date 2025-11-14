package com.example.phoenixmobile.ui.device

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.example.phoenixmobile.data.AudioStatus
import com.example.phoenixmobile.data.ReportStatus
import com.example.phoenixmobile.data.*
import com.example.phoenixmobile.model.BodyCondition
import com.example.phoenixmobile.model.ScreenCondition
import com.example.phoenixmobile.utils.CheckBluetoothConnected
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class MyDeviceViewModel(application: Application) : AndroidViewModel(application) {

    private val TIMEOUT_DURATION = 15L
    private var timerJob: Job? = null

    private val testList = TestManager.testList
    private val audioTestState = TestManager.audioStatus
    private val reportState = ReportManager.reportStatus
    private val reportContent = ReportManager.reportLiveText

    private val _bodyCondition = MutableLiveData<BodyCondition>()
    val bodyCondition: LiveData<BodyCondition> = _bodyCondition

    private val _screenCondition = MutableLiveData<ScreenCondition>()
    val screenCondition: LiveData<ScreenCondition> = _screenCondition
    private val reportUrl = MutableLiveData<String?>()

    init {
        observeReportState()
    }

    fun startCheck() {
        ReportManager.setReportInProgress()
        Log.d("REPORT_FLOW", "Report status set to STARTED, beginning timer")

        Log.d("TIMER", "Timer start")
        timerJob = viewModelScope.launch {
            timerFlow(TIMEOUT_DURATION).collect { time ->
                if (time == 0L) {
                    cancel()
                    ReportManager.setReportError()
                    Log.d("TIMER", "Time expired. Report sent with error.")
                }
            }
        }
    }

    // Остановка таймера извне
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        Log.d("TIMER", "Timer manually stopped.")
    }

    private fun observeReportState() {
        viewModelScope.launch {
            reportState.asFlow().collect { status ->
                Log.d("REPORT_FLOW", "Observed report status change: $status")
                if (status == ReportStatus.DONE || status == ReportStatus.ERROR) {
                    Log.d("REPORT_FLOW", "Terminal status reached ($status), triggering sendReport()")
                    ReportApi.sendReport()
                    TestManager.resetTests()
                    stopTimer()
                    Log.d("VW", "Stop service")
                }
            }
        }
    }

    fun setBodyCondition(condition: BodyCondition) {
        _bodyCondition.value = condition
    }

    fun setScreenCondition(condition: ScreenCondition) {
        _screenCondition.value = condition
    }

    fun getTestResults() = testList

    fun getAudioTestState() = audioTestState

    fun getReportState() = reportState

    fun getReportText() = reportContent

    fun getBluetoothFlag() = CheckBluetoothConnected.bluetoothConnectedStatus

    fun setDisplayCheck(screenWidth: Int, screenHeight: Int, density: Float) {
        if(screenCondition.value != null && bodyCondition.value != null) {
            TestManager.setDisplayReport(
                screenCondition.value!!,
                bodyCondition.value!!,
                screenWidth,
                screenHeight,
                density
            )
        } else{
            Log.e("Display Test", "Hasn't required value")
        }
    }

    fun setAudioReply(userHeardSound: Boolean) {
        val status = if (userHeardSound) AudioStatus.DONE else AudioStatus.ERROR
        TestManager.setAudioReport(status)
    }

    fun tryBluetoothAgain() {
        CheckBluetoothConnected.skipStatus()
    }

    fun downloadFile() {
        val url = reportUrl.value ?: ReportManager.reportLiveText.value?.get("downloadUrl")
        if (url == null) {
            Toast.makeText(
                getApplication(),
                "No report URL available.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val appContext = getApplication<Application>().applicationContext
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("Report PDF")
            setDescription("Downloading report...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }

        val manager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)

        Toast.makeText(appContext, "Download started", Toast.LENGTH_LONG).show()
    }

    private fun timerFlow(duration: Long): Flow<Long> = flow {
        for (i in duration downTo 0) {
            emit(i)
            delay(1000)
        }
    }
}