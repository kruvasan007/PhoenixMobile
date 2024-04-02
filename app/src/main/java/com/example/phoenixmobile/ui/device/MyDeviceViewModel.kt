package com.example.phoenixmobile.ui.device

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.phoenixmobile.data.Repository
import com.jaredrummler.android.device.DeviceName
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.TreeMap


class MyDeviceViewModel(application: Application) : AndroidViewModel(application) {
    private val TIMEOUT_DURATION: Long = 50
    private var testList = Repository.getTestList()
    private var audioTestState = Repository.getAudioTest()
    private var reportState = Repository.getReportState()
    private var reportText = Repository.getReportToText()
    private var bluetoothConnect = Repository.getBluetoothFlag()
    private val samsungNames = TreeMap<String, String>()

    fun startCheck() {
        Repository.setReportStarted()
        // setup time to generate report
        viewModelScope.launch {
            timerFlow(TIMEOUT_DURATION).collect { time ->
                // if time expired
                if (time == 0L) {
                    this.cancel()
                    Repository.setReportTimeExpired()
                    Log.d("TIMER", "Stopped auto")
                } else if (Repository.getReportState().value == Repository.REPORT_DONE) {
                    // if the report is generated before the end of the timer
                    this.cancel()
                    Log.d("TIMER", "Stopped with done")
                }
            }
        }
        viewModelScope.launch {
            // we stop the services if the report is ready
            reportState.asFlow().collect {
                if (it == Repository.REPORT_DONE || it == Repository.REPORT_ERROR) {
                    Log.d("VW", "Stop service")
                }
            }
        }
    }

    fun getDeviceName(): String? {
        return DeviceName.getDeviceName(Build.MODEL, Build.MODEL)
    }

    fun getTest() = testList
    fun audioTest() = audioTestState
    fun reportState() = reportState
    fun report() = reportText
    fun bluetoothConnect() = bluetoothConnect

    fun setDisplayCheck(screenWidth: Int, screenHeight: Int, density: Float) {
        Repository.setDisplayReport(screenWidth, screenHeight, density)
    }

    fun setAudioReply(reply: Boolean) {
        if (reply)
            Repository.setAudioResponse(Repository.AUDIO_CHECK_DONE)
        else
            Repository.setAudioResponse(Repository.REPORT_ERROR)
    }

    fun tryBluetoothAgain() {
        Repository.setBluetoothDisconnected()
    }


    fun downloadFile() {
        // download the report using the link from the server response
        val url = Repository.getReportPdf().value
        val download =
            getApplication<Application>().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val PdfUri: Uri = Uri.parse(url)
        val getPdf = DownloadManager.Request(PdfUri)
        getPdf.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        download.enqueue(getPdf)
        Toast.makeText(
            getApplication<Application>().applicationContext,
            "Download Started",
            Toast.LENGTH_LONG
        ).show()
    }

    fun timerFlow(duration: Long): Flow<Long> = flow {
        for (i in duration downTo 0) {
            emit(i)
            delay(1000)
        }
    }

}