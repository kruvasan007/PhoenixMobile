package com.example.phoenixmobile.ui.device

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.phoenixmobile.data.Repository
import com.example.phoenixmobile.service.AudioTest
import com.example.phoenixmobile.service.CPUTest
import com.example.phoenixmobile.service.HardWareCheck
import com.example.phoenixmobile.service.NetworkTest
import kotlinx.coroutines.launch


class MyDeviceViewModel : ViewModel() {

    private var testList = Repository.getTestList()
    private var audioTestState = Repository.getAudioTest()
    private var reportState = Repository.getReportState()
    private var reportText = Repository.getReportToText()
    private lateinit var HWintent: Intent
    private lateinit var CPUintent: Intent
    private lateinit var AUDIOintent: Intent
    private lateinit var NETWORKintent: Intent

    private fun startService(context: Context) {
        HWintent = Intent(context, HardWareCheck::class.java);
        NETWORKintent = Intent(context, NetworkTest::class.java);
        CPUintent = Intent(context, CPUTest::class.java);
        AUDIOintent = Intent(context, AudioTest::class.java);
        context.startService(HWintent)
        context.startService(NETWORKintent)
        context.startService(CPUintent)
        context.startService(AUDIOintent)
    }

    private fun stopService(context: Context) {
        context.stopService(HWintent)
        context.stopService(NETWORKintent)
        context.stopService(CPUintent)
        context.stopService(AUDIOintent)
    }

    fun setDisplayCheck(screenWidth: Int, screenHeight: Int, density: Float) {
        Repository.setDisplayReport(screenWidth, screenHeight, density)
    }

    fun getTest() = testList
    fun audioTest() = audioTestState
    fun reportState() = reportState

    fun report() = reportText

    fun setAudioReply(reply: Boolean) {
        if (reply)
            Repository.setAudioResponse(Repository.AUDIO_CHECK_DONE)
        else
            Repository.setAudioResponse(Repository.AUDIO_CHECK_ERROR)
    }

    fun startCheck(context: Context) {
        startService(context)
        viewModelScope.launch {
            reportState.asFlow().collect {
                println(it)
                if (it == Repository.REPORT_DONE) {
                    stopService(context)
                }
            }
        }
    }
}