package com.example.phoenixmobile.ui.device

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.phoenixmobile.R
import com.example.phoenixmobile.data.Repository
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.util.TreeMap


class MyDeviceViewModel(application: Application) : AndroidViewModel(application) {
    private val TIMEOUT_DURATION: Long = 60
    private var testList = Repository.getTestList()
    private var audioTestState = Repository.getAudioTest()
    private var reportState = Repository.getReportState()
    private var reportText = Repository.getReportToText()
    private var bluetoothConnect = Repository.getBluetoothFlag()
    private val samsungNames = TreeMap<String, String>()

    init {
        loadNames()
    }

    fun getDeviceName(): String? {
        val phoneModel = Build.MODEL
        return if (samsungNames.contains(phoneModel)) {
            samsungNames[phoneModel]
        } else
            phoneModel
    }

    fun setDisplayCheck(screenWidth: Int, screenHeight: Int, density: Float) {
        Repository.setDisplayReport(screenWidth, screenHeight, density)
    }

    fun getTest() = testList
    fun audioTest() = audioTestState
    fun reportState() = reportState
    fun report() = reportText
    fun bluetoothConnect() = bluetoothConnect

    fun setAudioReply(reply: Boolean) {
        if (reply)
            Repository.setAudioResponse(Repository.AUDIO_CHECK_DONE)
        else
            Repository.setAudioResponse(Repository.REPORT_ERROR)
    }

    fun tryBluetoothAgain() {
        Repository.setBluetoothDisconnected()
    }

    @SuppressLint("ResourceType")
    private fun loadNames() {
        val inputStreamReader = InputStreamReader(
            getApplication<Application>().resources.openRawResource(
                R.raw.samsung_names
            )
        )
        for (lines in inputStreamReader.readLines()) {
            val line = lines.split(",")
            samsungNames[line[0]] = line[1]
        }
    }

    fun timerFlow(duration: Long): Flow<Long> = flow {
        for (i in duration downTo 0) {
            emit(i)
            delay(1000)
        }
    }

    fun startCheck() {
        Repository.setReportStarted()
        viewModelScope.launch {
            timerFlow(TIMEOUT_DURATION).collect { time ->
                if (time == 0L) {
                    this.cancel()
                    Repository.setReportTimeExpired()
                    Log.d("TIMER", "Stopped auto")
                } else if (Repository.getReportState().value == Repository.REPORT_DONE) {
                    this.cancel()
                    Log.d("TIMER", "Stopped with done")
                }
                println(time)
            }
        }
        viewModelScope.launch {
            reportState.asFlow().collect {
                if (it == Repository.REPORT_DONE || it == Repository.REPORT_ERROR) {
                    Log.d("VW", "Stop service")
                }
            }
        }
    }
}